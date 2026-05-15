# ============================================================
# EC2 → S3 접근용 IAM Role & Instance Profile
# ============================================================

data "aws_iam_policy_document" "ec2_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ec2_s3" {
  name               = "${var.project_name}-ec2-s3-role"
  assume_role_policy = data.aws_iam_policy_document.ec2_assume_role.json
  tags               = { Name = "${var.project_name}-ec2-s3-role" }
}

data "aws_iam_policy_document" "s3_jar_read" {
  statement {
    sid    = "ReadJARsFromS3"
    effect = "Allow"
    actions = [
      "s3:GetObject",
      "s3:GetObjectVersion",
      "s3:ListBucket",
    ]
    resources = [
      aws_s3_bucket.jars.arn,
      "${aws_s3_bucket.jars.arn}/*",
    ]
  }

  # CloudWatch Logs (선택적 - 필요 시 사용)
  statement {
    sid    = "CloudWatchLogs"
    effect = "Allow"
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents",
      "logs:DescribeLogStreams",
    ]
    resources = ["arn:aws:logs:*:*:*"]
  }
}

resource "aws_iam_role_policy" "ec2_s3_policy" {
  name   = "${var.project_name}-ec2-s3-policy"
  role   = aws_iam_role.ec2_s3.id
  policy = data.aws_iam_policy_document.s3_jar_read.json
}

# SSM Session Manager (선택적 - bastion 없이 접속 가능)
resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.ec2_s3.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "ec2_s3" {
  name = "${var.project_name}-ec2-instance-profile"
  role = aws_iam_role.ec2_s3.name
}
