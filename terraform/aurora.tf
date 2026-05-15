# ============================================================
# RDS PostgreSQL (Free Tier: db.t3.micro, 20GB, 단일 AZ)
# ============================================================

# RDS 전용 Security Group
resource "aws_security_group" "rds" {
  name        = "${var.project_name}-sg-rds"
  description = "RDS PostgreSQL - internal access only (5432)"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "PostgreSQL from domain-ec2"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.domain.id]
  }

  ingress {
    description     = "PostgreSQL from domain2-ec2"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.domain2.id]
  }

  ingress {
    description     = "PostgreSQL from notification-ec2"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.notification.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project_name}-sg-rds" }
}

# DB 서브넷 그룹 (AWS 요구사항: 2개 AZ 서브넷 필수)
resource "aws_subnet" "private_az2" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.3.0/24"
  availability_zone = "ap-northeast-2c"

  tags = { Name = "${var.project_name}-subnet-private-az2" }
}

resource "aws_route_table_association" "private_az2" {
  subnet_id      = aws_subnet.private_az2.id
  route_table_id = aws_route_table.private.id
}

resource "aws_db_subnet_group" "main" {
  name        = "${var.project_name}-rds-subnet-group"
  description = "RDS PostgreSQL subnet group"
  subnet_ids  = [
    aws_subnet.private.id,
    aws_subnet.private_az2.id,
  ]

  tags = { Name = "${var.project_name}-rds-subnet-group" }
}

# RDS PostgreSQL 인스턴스
resource "aws_db_instance" "main" {
  identifier        = "${var.project_name}-rds"
  engine            = "postgres"
  engine_version    = "15"
  instance_class    = "db.t3.micro"
  allocated_storage = 20
  storage_type      = "gp2"
  storage_encrypted = true

  db_name  = var.rds_database_name
  username = var.rds_username
  password = var.rds_password

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  multi_az            = false
  publicly_accessible = false

  backup_retention_period   = 1
  backup_window             = "18:00-19:00"
  maintenance_window        = "sun:19:00-sun:20:00"

  deletion_protection       = false
  skip_final_snapshot       = true

  tags = { Name = "${var.project_name}-rds" }
}

output "rds_endpoint" {
  description = "RDS PostgreSQL endpoint"
  value       = aws_db_instance.main.address
}

output "rds_port" {
  description = "RDS PostgreSQL port"
  value       = aws_db_instance.main.port
}
