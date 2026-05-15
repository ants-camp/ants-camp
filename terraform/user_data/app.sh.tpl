#!/bin/bash
# ============================================================
# CodeDeploy 대상 앱 EC2 초기화 스크립트
# - Docker 설치
# - CodeDeploy 에이전트 설치 및 기동
# - Secrets Manager에서 Aurora 자격증명 로드
# ============================================================
set -euxo pipefail

# ── 로그 파일 설정 ─────────────────────────────────────────
exec > >(tee /var/log/user-data.log | logger -t user-data) 2>&1
echo "=== user_data 시작: $(date) ==="

# ── 시스템 패키지 업데이트 ─────────────────────────────────
apt-get update -y
apt-get install -y \
  ruby-full \
  wget \
  curl \
  jq \
  awscli \
  unzip \
  ca-certificates \
  gnupg \
  lsb-release

# ── Docker 설치 ───────────────────────────────────────────
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
  > /etc/apt/sources.list.d/docker.list

apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin

systemctl enable docker
systemctl start docker

# ubuntu 유저가 sudo 없이 docker 사용 가능하도록
usermod -aG docker ubuntu
echo "Docker 버전: $(docker --version)"

# ── CodeDeploy 에이전트 설치 ──────────────────────────────
cd /tmp
wget -O install \
  "https://aws-codedeploy-${aws_region}.s3.${aws_region}.amazonaws.com/latest/install"
chmod +x ./install
./install auto

systemctl enable codedeploy-agent
systemctl start codedeploy-agent
echo "CodeDeploy 에이전트 상태: $(systemctl is-active codedeploy-agent)"

# ── 배포 디렉토리 생성 ─────────────────────────────────────
APP_DIR="/opt/${project_name}"
LOG_DIR="/var/log/${project_name}"

mkdir -p "$APP_DIR"
mkdir -p "$LOG_DIR"
chown ubuntu:ubuntu "$APP_DIR" "$LOG_DIR"

# Secret ARN 파일 저장 (after_install.sh에서 참조)
echo "${secret_arn}" > "$APP_DIR/secret_arn.txt"
chmod 644 "$APP_DIR/secret_arn.txt"

# Docker 이미지 태그 파일 저장 (after_install.sh에서 참조)
echo "${docker_image}" > "$APP_DIR/docker_image.txt"
chmod 644 "$APP_DIR/docker_image.txt"

# ── Secrets Manager에서 Aurora 자격증명 로드 ──────────────
echo "Secrets Manager에서 Aurora 자격증명 로드 중..."
SECRET_VALUE=$(aws secretsmanager get-secret-value \
  --secret-id "${secret_arn}" \
  --region "${aws_region}" \
  --query SecretString \
  --output text)

# .env 파일 생성 (docker run --env-file 에서 사용)
cat > "$APP_DIR/.env" <<EOF
# Aurora PostgreSQL 접속 정보 (Secrets Manager에서 로드)
DB_HOST=$(echo "$SECRET_VALUE" | jq -r '.host')
DB_PORT=$(echo "$SECRET_VALUE" | jq -r '.port')
DB_NAME=$(echo "$SECRET_VALUE" | jq -r '.dbname')
DB_USERNAME=$(echo "$SECRET_VALUE" | jq -r '.username')
DB_PASSWORD=$(echo "$SECRET_VALUE" | jq -r '.password')
DB_READER_HOST=$(echo "$SECRET_VALUE" | jq -r '.reader_host')

# 앱 설정
SERVER_PORT=${app_port}
SPRING_PROFILES_ACTIVE=prod
EOF

chmod 600 "$APP_DIR/.env"
chown ubuntu:ubuntu "$APP_DIR/.env"

# ── Docker 기본 이미지 미리 pull (배포 시간 단축) ────────
docker pull "${docker_image}" || true

echo "=== user_data 완료: $(date) ==="
