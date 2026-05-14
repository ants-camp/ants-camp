# ============================================================
# Security Groups (규칙 없이 정의)
# 순환 참조 방지를 위해 cross-SG 규칙은 security_group_rules.tf에서 관리
# ============================================================

# ── gateway-ec2 (nginx + bastion) ──────────────────────────
resource "aws_security_group" "gateway" {
  name        = "${var.project_name}-sg-gateway"
  description = "gateway-ec2: nginx reverse proxy + bastion"
  vpc_id      = aws_vpc.main.id

  # HTTP from internet
  ingress {
    description = "HTTP from internet"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTPS from internet
  ingress {
    description = "HTTPS from internet"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # SSH from admin IP only
  ingress {
    description = "SSH from admin"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.admin_cidr]
  }

  # 아웃바운드 전체 허용 (nginx → 내부 서비스)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project_name}-sg-gateway" }
}

# ── kafka-ec2 (KRaft) ──────────────────────────────────────
resource "aws_security_group" "kafka" {
  name        = "${var.project_name}-sg-kafka"
  description = "kafka-ec2: Kafka KRaft broker"
  vpc_id      = aws_vpc.main.id

  # KRaft controller - self
  ingress {
    description = "KRaft controller self"
    from_port   = var.ports["kafka_controller"]
    to_port     = var.ports["kafka_controller"]
    protocol    = "tcp"
    self        = true
  }

  # SSH from gateway (bastion)  → rule in security_group_rules.tf

  # Loki push (Promtail → monitoring)  → rule in security_group_rules.tf

  # 아웃바운드: VPC 내부 + S3
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project_name}-sg-kafka" }
}

# ── infra-ec2 (config, eureka, kafka-ui) ───────────────────
resource "aws_security_group" "infra" {
  name        = "${var.project_name}-sg-infra"
  description = "infra-ec2: Config Server + Eureka + Kafka UI (internal only)"
  vpc_id      = aws_vpc.main.id

  # 아웃바운드: kafka, S3, internet (apt-get 등)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project_name}-sg-infra" }
}

# ── domain-ec2 (user, asset, ranking) ─────────────────────
resource "aws_security_group" "domain" {
  name        = "${var.project_name}-sg-domain"
  description = "domain-ec2: user(8081) asset(8082) ranking(1400)"
  vpc_id      = aws_vpc.main.id

  # 아웃바운드: infra + db + kafka + loki + S3
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project_name}-sg-domain" }
}

# ── domain2-ec2 (trade, competition) ──────────────────────
resource "aws_security_group" "domain2" {
  name        = "${var.project_name}-sg-domain2"
  description = "domain2-ec2: trade(1050) competition(1051)"
  vpc_id      = aws_vpc.main.id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project_name}-sg-domain2" }
}

# ── notification-ec2 (notification, assistant) ─────────────
resource "aws_security_group" "notification" {
  name        = "${var.project_name}-sg-notification"
  description = "notification-ec2: notification(1200) assistant(1201) - public HTTPS"
  vpc_id      = aws_vpc.main.id

  # HTTPS from internet (외부 webhook, push 수신)
  ingress {
    description = "HTTPS from internet"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTP from internet"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project_name}-sg-notification" }
}

# ── monitoring-ec2 (prometheus, grafana, loki) ─────────────
resource "aws_security_group" "monitoring" {
  name        = "${var.project_name}-sg-monitoring"
  description = "monitoring-ec2: Prometheus(9090) Grafana(3000) Loki(3100)"
  vpc_id      = aws_vpc.main.id

  # Grafana: 전체 공개
  ingress {
    description = "Grafana from internet"
    from_port   = var.ports["grafana"]
    to_port     = var.ports["grafana"]
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Prometheus: VPC 내부에서만
  ingress {
    description = "Prometheus internal"
    from_port   = var.ports["prometheus"]
    to_port     = var.ports["prometheus"]
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }

  # SSH: 관리자 IP에서만
  ingress {
    description = "SSH from admin"
    from_port   = var.ports["ssh"]
    to_port     = var.ports["ssh"]
    protocol    = "tcp"
    cidr_blocks = [var.admin_cidr]
  }

  # SSH from gateway
  # → security_group_rules.tf

  # Loki (Promtail push) from app EC2s
  # → security_group_rules.tf

  # 아웃바운드: 메트릭 스크랩(VPC 내부) + S3 + internet
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project_name}-sg-monitoring" }
}

# ── db-ec2 (postgres, redis) ───────────────────────────────
resource "aws_security_group" "db" {
  name        = "${var.project_name}-sg-db"
  description = "db-ec2: Postgres(5432) Redis(6379) - internal only, no public access"
  vpc_id      = aws_vpc.main.id

  # 인바운드 규칙은 모두 security_group_rules.tf에서 정의

  # 아웃바운드: Loki push + S3 (OS 업데이트)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project_name}-sg-db" }
}
