# ============================================================
# RDS PostgreSQL 변수
# ============================================================

variable "rds_database_name" {
  description = "RDS database name"
  type        = string
  default     = "ants_camp"
}

variable "rds_username" {
  description = "RDS master username"
  type        = string
  default     = "postgres"
}

variable "rds_password" {
  description = "RDS master password (no / @ or spaces)"
  type        = string
  sensitive   = true
}
