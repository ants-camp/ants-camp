# ============================================================
# 기본 설정
# ============================================================

variable "aws_region" {
  description = "AWS 리전"
  type        = string
  default     = "ap-northeast-2"
}

variable "project_name" {
  description = "리소스 이름 접두사"
  type        = string
  default     = "ants-camp"
}

variable "availability_zone" {
  description = "가용 영역"
  type        = string
  default     = "ap-northeast-2a"
}

# ============================================================
# 네트워크
# ============================================================

variable "vpc_cidr" {
  description = "VPC CIDR 블록"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  description = "퍼블릭 서브넷 CIDR (gateway, notification)"
  type        = string
  default     = "10.0.1.0/24"
}

variable "private_subnet_cidr" {
  description = "프라이빗 서브넷 CIDR (infra, kafka, domain, db, monitoring)"
  type        = string
  default     = "10.0.2.0/24"
}

# ============================================================
# 접근 제어
# ============================================================

variable "admin_cidr" {
  description = "관리자 IP CIDR (SSH 및 Grafana 접근용). 예: 203.0.113.10/32"
  type        = string
}

# ============================================================
# EC2 키 페어
# ============================================================

variable "key_name" {
  description = "EC2 Key Pair 이름 (create_key_pair = false 일 때 기존 키 이름 사용)"
  type        = string
}

variable "create_key_pair" {
  description = "Terraform에서 키 페어를 새로 생성할지 여부"
  type        = bool
  default     = false
}

variable "public_key_path" {
  description = "공개키 파일 경로 (create_key_pair = true 일 때 필요). 예: ~/.ssh/id_rsa.pub"
  type        = string
  default     = ""
}

# ============================================================
# AMI
# ============================================================

variable "ami_id" {
  description = "EC2 인스턴스 AMI ID (Ubuntu 22.04 LTS ap-northeast-2)"
  type        = string
  default     = "ami-0c9c942bd7bf113a2"  # 최신 Ubuntu 22.04 AMI로 교체 권장
}

# ============================================================
# S3 JAR 배포
# ============================================================

variable "s3_bucket_name" {
  description = "JAR 파일이 저장된 S3 버킷 이름 (Terraform이 직접 생성)"
  type        = string
}

variable "cicd_role_arn" {
  description = "S3에 JAR를 업로드할 CI/CD 파이프라인 IAM Role ARN. 비워두면 EC2만 접근 허용"
  type        = string
  default     = ""
}

variable "jar_keys" {
  description = "서비스별 S3 JAR 경로 맵"
  type        = map(string)
  default = {
    config_server = "jars/config-server.jar"
    eureka_server = "jars/eureka-server.jar"
    kafka_ui      = "jars/kafka-ui.jar"
    user          = "jars/user-service.jar"
    asset         = "jars/asset-service.jar"
    ranking       = "jars/ranking-service.jar"
    trade         = "jars/trade-service.jar"
    competition   = "jars/competition-service.jar"
    notification  = "jars/notification-service.jar"
    assistant     = "jars/assistant-service.jar"
  }
}

# ============================================================
# 서비스 포트
# ============================================================

variable "ports" {
  description = "서비스별 포트 맵"
  type        = map(number)
  default = {
    # infra-ec2
    config_server = 8888
    eureka        = 8761
    kafka_ui      = 9000

    # kafka-ec2
    kafka_broker     = 9092
    kafka_controller = 9093

    # domain-ec2
    user    = 8081
    asset   = 8082
    ranking = 8094

    # domain2-ec2
    trade       = 8084
    competition = 8092

    # notification-ec2
    notification = 8098
    assistant    = 8096

    # monitoring-ec2
    prometheus = 9090
    grafana    = 3000
    loki       = 3100
    promtail   = 9080

    # observability
    node_exporter = 9100

    # db-ec2
    postgres = 5432
    redis    = 6379

    # nginx
    http  = 80
    https = 443
    ssh   = 22
  }
}

# ============================================================
# Kafka
# ============================================================

variable "kafka_heap_size" {
  description = "Kafka JVM Heap 크기"
  type        = string
  default     = "256m"
}

# ============================================================
# Config Server
# ============================================================

variable "config_git_uri" {
  description = "Config Server가 바라볼 Git 저장소 URI"
  type        = string
  default     = "https://github.com/ants-camp/ants-camp-config"
}

variable "config_git_username" {
  description = "Config Server Git 인증 사용자명 (private repo)"
  type        = string
  default     = ""
  sensitive   = true
}

variable "config_git_password" {
  description = "Config Server Git 토큰 (private repo)"
  type        = string
  default     = ""
  sensitive   = true
}

# ============================================================
# Monitoring EC2 고정 사설 IP
# 순환 참조 방지: 다른 EC2의 Promtail loki_ip에서 미리 참조
# public_subnet_cidr(10.0.1.0/24) 범위 내 미사용 IP를 지정
# (monitoring-ec2는 퍼블릭 서브넷에 배치 + EIP 할당)
# ============================================================

variable "monitoring_private_ip" {
  description = "monitoring-ec2에 부여할 고정 사설 IP (퍼블릭 서브넷 범위 내, Promtail loki_ip 참조용)"
  type        = string
  default     = "10.0.1.100"
}

# ============================================================
# Notification EC2 (공개 HTTPS)
# ============================================================

variable "notification_domain" {
  description = "notification-ec2에 연결할 도메인 (ACM 인증서 발급용)"
  type        = string
  default     = ""
}

# ============================================================
# Docker 컨테이너 설정
# ============================================================

variable "docker_java_image" {
  description = "Spring Boot 서비스 실행에 사용할 Docker Java 이미지"
  type        = string
  default     = "eclipse-temurin:17-jre-jammy"
}

variable "docker_memory_limit" {
  description = "각 서비스 컨테이너의 메모리 상한 (--memory / --memory-swap 동일 설정)"
  type        = string
  default     = "512m"
}

# ============================================================
# ECR (Elastic Container Registry)
# ============================================================

variable "ecr_image_retention_count" {
  description = "ECR 리포지토리별 보관할 최대 이미지 수 (초과분 자동 삭제)"
  type        = number
  default     = 10
}

# ============================================================
# JVM Heap 설정 (서비스별 -Xmx)
# ============================================================

variable "jvm_heap" {
  description = "서비스별 JVM 최대 힙 (-Xmx). 키: 서비스명, 값: heap 크기"
  type        = map(string)
  default = {
    assistant   = "512m"   # notification-ec2 내 assistant 서비스
    eureka      = "256m"   # infra-ec2 내 eureka-server
    gateway_svc = "384m"   # gateway-ec2 내 Spring Cloud Gateway (선택)
    default     = "256m"   # 나머지 모든 서비스
  }
}

# ============================================================
# Tomcat / HikariCP 튜닝
# ============================================================

variable "tomcat_max_threads" {
  description = "Spring Boot 내장 Tomcat 최대 워커 스레드 수"
  type        = number
  default     = 20
}

variable "hikari_max_pool_size" {
  description = "HikariCP 최대 커넥션 풀 크기 (spring.datasource.hikari.maximum-pool-size)"
  type        = number
  default     = 3
}
