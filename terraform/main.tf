terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # 원격 state 사용 시 아래 주석 해제
  # backend "s3" {
  #   bucket = "your-terraform-state-bucket"
  #   key    = "ants-camp/terraform.tfstate"
  #   region = "ap-northeast-2"
  # }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = var.project_name
      ManagedBy   = "Terraform"
    }
  }
}

data "aws_caller_identity" "current" {}
