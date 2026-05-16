# ============================================================
# ECR (Elastic Container Registry)
#
# 각 서비스의 멀티 스테이지 Dockerfile 빌드 결과를 저장하는 리포지토리
# 리포 명명 규칙: ants-camp/<service-name>
#
# IAM 정책 구조:
#   EC2 인스턴스 → ecr_pull  (기존 ec2_s3 Role에 인라인 정책 추가)
#   GitHub Actions → ecr_push 매니지드 정책 (cicd_role_arn 설정 시 자동 연결)
# ============================================================

locals {
  ecr_services = toset([
    "api-gateway",
    "assistant-service",
    "asset-service",
    "competition-service",
    "config-server",
    "eureka-server",
    "notification-service",
    "ranking-service",
    "trade-service",
    "user-service",
  ])

  # cicd_role_arn 에서 role 이름만 추출
  # e.g. arn:aws:iam::123456789012:role/path/my-role → my-role
  cicd_role_name = var.cicd_role_arn != "" ? element(
    split("/", var.cicd_role_arn),
    length(split("/", var.cicd_role_arn)) - 1
  ) : ""
}

# ── ECR 리포지토리 ────────────────────────────────────────────
resource "aws_ecr_repository" "services" {
  for_each = local.ecr_services

  name                 = "${var.project_name}/${each.key}"
  image_tag_mutability = "MUTABLE" # sha 태그 + latest 태그 덮어쓰기 허용

  image_scanning_configuration {
    scan_on_push = true # 푸시 시 자동 취약점 스캔
  }

  tags = { Name = "${var.project_name}/${each.key}" }
}

# ── 라이프사이클 정책: 오래된 이미지 자동 정리 ───────────────
resource "aws_ecr_lifecycle_policy" "services" {
  for_each = aws_ecr_repository.services

  repository = each.value.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "최근 ${var.ecr_image_retention_count}개 이미지만 보관, 초과분 자동 삭제"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = var.ecr_image_retention_count
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}

# ============================================================
# IAM - EC2 인스턴스 → ECR Pull
# 기존 ec2_s3 Role에 인라인 정책으로 추가
# ============================================================

data "aws_iam_policy_document" "ecr_pull" {
  # GetAuthorizationToken 은 리소스 수준 권한이 지원되지 않아 * 필요
  statement {
    sid       = "ECRAuthToken"
    effect    = "Allow"
    actions   = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }

  statement {
    sid    = "ECRPullImages"
    effect = "Allow"
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchGetImage",
    ]
    resources = [for repo in aws_ecr_repository.services : repo.arn]
  }
}

resource "aws_iam_role_policy" "ec2_ecr_pull" {
  name   = "${var.project_name}-ec2-ecr-pull"
  role   = aws_iam_role.ec2_s3.id
  policy = data.aws_iam_policy_document.ecr_pull.json
}

# ============================================================
# IAM - GitHub Actions → ECR Push (Managed Policy)
# cicd_role_arn 변수가 설정된 경우 해당 Role에 자동 연결
# ============================================================

resource "aws_iam_policy" "ecr_push" {
  name        = "${var.project_name}-ecr-push-policy"
  description = "GitHub Actions CD에서 ECR 이미지 빌드 & 푸시에 필요한 권한"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid      = "ECRAuthToken"
        Effect   = "Allow"
        Action   = ["ecr:GetAuthorizationToken"]
        Resource = "*"
      },
      {
        Sid    = "ECRPushPullImages"
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload",
          "ecr:PutImage",
          "ecr:DescribeRepositories",
          "ecr:ListImages",
        ]
        Resource = [for repo in aws_ecr_repository.services : repo.arn]
      }
    ]
  })

  tags = { Name = "${var.project_name}-ecr-push-policy" }
}

# cicd_role_arn 이 설정된 경우 ECR Push 정책 자동 연결
resource "aws_iam_role_policy_attachment" "cicd_ecr_push" {
  count = var.cicd_role_arn != "" ? 1 : 0

  role       = local.cicd_role_name
  policy_arn = aws_iam_policy.ecr_push.arn
}

# ============================================================
# Outputs
# ============================================================

output "ecr_registry_url" {
  description = "ECR 레지스트리 URL → GitHub Variables의 ECR_REGISTRY 값으로 설정"
  value       = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com"
}

output "ecr_repository_urls" {
  description = "서비스별 ECR 리포지토리 URL"
  value       = { for k, v in aws_ecr_repository.services : k => v.repository_url }
}

output "ecr_push_policy_arn" {
  description = "GitHub Actions에 부여할 ECR Push 정책 ARN (cicd_role_arn 미설정 시 수동 연결 필요)"
  value       = aws_iam_policy.ecr_push.arn
}
