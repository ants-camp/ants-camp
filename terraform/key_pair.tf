# ============================================================
# Key Pair
# create_key_pair = true  → Terraform이 공개키로 AWS Key Pair 생성
# create_key_pair = false → 기존 키 이름(var.key_name)을 그대로 참조
# ============================================================

resource "aws_key_pair" "main" {
  count      = var.create_key_pair ? 1 : 0
  key_name   = var.key_name
  public_key = file(var.public_key_path)
  tags       = { Name = "${var.project_name}-keypair" }
}

locals {
  key_name = var.create_key_pair ? aws_key_pair.main[0].key_name : var.key_name
}
