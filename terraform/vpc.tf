# ============================================================
# VPC
# ============================================================

resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = { Name = "${var.project_name}-vpc" }
}

# ============================================================
# Internet Gateway
# ============================================================

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id
  tags   = { Name = "${var.project_name}-igw" }
}

# ============================================================
# 서브넷
# ============================================================

# 퍼블릭 서브넷: gateway-ec2, notification-ec2
resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_cidr
  availability_zone       = var.availability_zone
  map_public_ip_on_launch = false  # EIP 또는 명시적 할당으로 제어

  tags = { Name = "${var.project_name}-subnet-public" }
}

# 프라이빗 서브넷: infra, kafka, domain, domain2, monitoring, db
resource "aws_subnet" "private" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_subnet_cidr
  availability_zone = var.availability_zone

  tags = { Name = "${var.project_name}-subnet-private" }
}

# ============================================================
# NAT Gateway (프라이빗 서브넷 → 인터넷 아웃바운드)
# apt-get, Promtail 바이너리 다운로드 등 필요
# 비용: ~$30/월. 불필요시 주석 처리 후 AMI에 사전 설치 권장.
# ============================================================

resource "aws_eip" "nat" {
  domain = "vpc"
  tags   = { Name = "${var.project_name}-eip-nat" }
}

resource "aws_nat_gateway" "main" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public.id
  depends_on    = [aws_internet_gateway.main]
  tags          = { Name = "${var.project_name}-nat-gw" }
}

# ============================================================
# 라우트 테이블
# ============================================================

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = { Name = "${var.project_name}-rt-public" }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.main.id
  }

  tags = { Name = "${var.project_name}-rt-private" }
}

resource "aws_route_table_association" "private" {
  subnet_id      = aws_subnet.private.id
  route_table_id = aws_route_table.private.id
}

# ============================================================
# VPC Endpoint (S3 Gateway) - JAR 다운로드 비용 절감
# ============================================================

resource "aws_vpc_endpoint" "s3" {
  vpc_id            = aws_vpc.main.id
  service_name      = "com.amazonaws.${var.aws_region}.s3"
  vpc_endpoint_type = "Gateway"

  route_table_ids = [
    aws_route_table.public.id,
    aws_route_table.private.id,
  ]

  tags = { Name = "${var.project_name}-vpce-s3" }
}
