# ============================================================
# S3 Bucket - JAR 배포용
# ============================================================

resource "aws_s3_bucket" "jars" {
  bucket        = var.s3_bucket_name
  force_destroy = false   # 운영 중 실수 삭제 방지. 삭제 필요 시 true로 변경 후 apply

  tags = { Name = "${var.project_name}-jar-bucket" }
}

# ── 퍼블릭 접근 완전 차단 ─────────────────────────────────────
resource "aws_s3_bucket_public_access_block" "jars" {
  bucket                  = aws_s3_bucket.jars.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# ── 버전 관리 (롤백 대비) ─────────────────────────────────────
resource "aws_s3_bucket_versioning" "jars" {
  bucket = aws_s3_bucket.jars.id
  versioning_configuration {
    status = "Enabled"
  }
}

# ── 서버 사이드 암호화 (AES-256) ─────────────────────────────
resource "aws_s3_bucket_server_side_encryption_configuration" "jars" {
  bucket = aws_s3_bucket.jars.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
    bucket_key_enabled = true
  }
}

# ── 라이프사이클: 구버전 JAR 자동 정리 ───────────────────────
resource "aws_s3_bucket_lifecycle_configuration" "jars" {
  bucket = aws_s3_bucket.jars.id

  # 현재 버전: 무기한 보관
  # 비현재(이전) 버전: 30일 후 삭제
  rule {
    id     = "expire-old-jar-versions"
    status = "Enabled"

    filter {
      prefix = "jars/"
    }

    noncurrent_version_expiration {
      noncurrent_days = 30
    }

    # 불완전한 멀티파트 업로드 정리 (7일)
    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }
  }
}

# ── 버킷 정책: EC2 IAM Role만 접근 허용 ──────────────────────
resource "aws_s3_bucket_policy" "jars" {
  bucket = aws_s3_bucket.jars.id
  policy = data.aws_iam_policy_document.s3_bucket_policy.json

  depends_on = [aws_s3_bucket_public_access_block.jars]
}

data "aws_iam_policy_document" "s3_bucket_policy" {
  # EC2 인스턴스 프로파일을 통한 접근만 허용
  statement {
    sid    = "AllowEC2RoleAccess"
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = [aws_iam_role.ec2_s3.arn]
    }
    actions = [
      "s3:GetObject",
      "s3:ListBucket",
      "s3:GetObjectVersion",
    ]
    resources = [
      aws_s3_bucket.jars.arn,
      "${aws_s3_bucket.jars.arn}/*",
    ]
  }

  # CI/CD 파이프라인용 업로드 권한 (선택적 - 별도 Role ARN 변수로 제어)
  dynamic "statement" {
    for_each = var.cicd_role_arn != "" ? [1] : []
    content {
      sid    = "AllowCICDUpload"
      effect = "Allow"
      principals {
        type        = "AWS"
        identifiers = [var.cicd_role_arn]
      }
      actions = [
        "s3:PutObject",
        "s3:DeleteObject",
        "s3:GetObject",
        "s3:ListBucket",
      ]
      resources = [
        aws_s3_bucket.jars.arn,
        "${aws_s3_bucket.jars.arn}/*",
      ]
    }
  }

  # SSL 강제 (HTTP 접근 거부)
  statement {
    sid    = "DenyNonSSL"
    effect = "Deny"
    principals {
      type        = "*"
      identifiers = ["*"]
    }
    actions   = ["s3:*"]
    resources = [
      aws_s3_bucket.jars.arn,
      "${aws_s3_bucket.jars.arn}/*",
    ]
    condition {
      test     = "Bool"
      variable = "aws:SecureTransport"
      values   = ["false"]
    }
  }
}

# ── 알림용 버킷 이벤트 (선택적 - SNS/SQS 연동 시 활성화) ─────
# resource "aws_s3_bucket_notification" "jars" {
#   bucket = aws_s3_bucket.jars.id
#   ...
# }

# ============================================================
# Outputs
# ============================================================

output "s3_bucket_name" {
  description = "JAR 배포용 S3 버킷 이름"
  value       = aws_s3_bucket.jars.bucket
}

output "s3_bucket_arn" {
  description = "JAR 배포용 S3 버킷 ARN"
  value       = aws_s3_bucket.jars.arn
}

output "s3_jar_upload_example" {
  description = "JAR 업로드 예시 명령어"
  value       = "aws s3 cp target/my-service.jar s3://${aws_s3_bucket.jars.bucket}/jars/my-service.jar"
}
