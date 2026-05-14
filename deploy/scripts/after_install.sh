#!/bin/bash
# ============================================================
# AfterInstall Hook
# - .env 파일 재생성 (Secrets Manager에서 Aurora 자격증명 최신화)
# - systemd 서비스가 docker run을 실행하도록 유닛 파일 생성
# ============================================================
set -euxo pipefail

APP_DIR="/opt/ants-camp"
LOG_DIR="/var/log/ants-camp"
SERVICE_NAME="ants-camp-app"
CONTAINER_NAME="ants-camp-app"

echo "=== AfterInstall 시작: $(date) ==="

# ── 디렉토리 권한 설정 ────────────────────────────────────
mkdir -p "$LOG_DIR"
chown ubuntu:ubuntu "$LOG_DIR"
chown -R ubuntu:ubuntu "$APP_DIR"

# ── Secrets Manager에서 Aurora 자격증명 최신화 ────────────
SECRET_ARN_FILE="$APP_DIR/secret_arn.txt"

if [ -f "$SECRET_ARN_FILE" ]; then
  SECRET_ARN=$(cat "$SECRET_ARN_FILE")
  AWS_REGION=$(curl -s --max-time 3 \
    "http://169.254.169.254/latest/meta-data/placement/region" \
    || echo "ap-northeast-2")

  echo "Secrets Manager에서 Aurora 자격증명 로드: $SECRET_ARN"
  SECRET_VALUE=$(aws secretsmanager get-secret-value \
    --secret-id "$SECRET_ARN" \
    --region "$AWS_REGION" \
    --query SecretString \
    --output text)

  # docker run --env-file 에서 읽을 .env 생성
  cat > "$APP_DIR/.env" <<EOF
# Aurora PostgreSQL 접속 정보 (자동 생성 - $(date))
DB_HOST=$(echo "$SECRET_VALUE" | jq -r '.host')
DB_PORT=$(echo "$SECRET_VALUE" | jq -r '.port')
DB_NAME=$(echo "$SECRET_VALUE" | jq -r '.dbname')
DB_USERNAME=$(echo "$SECRET_VALUE" | jq -r '.username')
DB_PASSWORD=$(echo "$SECRET_VALUE" | jq -r '.password')
DB_READER_HOST=$(echo "$SECRET_VALUE" | jq -r '.reader_host')

# 앱 설정
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
EOF

  chmod 600 "$APP_DIR/.env"
  chown ubuntu:ubuntu "$APP_DIR/.env"
  echo "Aurora 자격증명 업데이트 완료"
else
  echo "경고: secret_arn.txt 없음 - 기존 .env 파일 사용"
fi

# ── 사용할 Docker 이미지 확인 ────────────────────────────
DOCKER_IMAGE=$(cat "$APP_DIR/docker_image.txt" 2>/dev/null || echo "eclipse-temurin:17-jre-jammy")
echo "Docker 이미지: $DOCKER_IMAGE"

# 이미지 pull (최신 버전 보장)
docker pull "$DOCKER_IMAGE"

# ── systemd 서비스 파일 생성 (docker run 실행) ───────────
cat > "/etc/systemd/system/$SERVICE_NAME.service" <<EOF
[Unit]
Description=Ants Camp Application (Docker)
After=docker.service network.target
Requires=docker.service

[Service]
Type=simple
User=ubuntu
Restart=on-failure
RestartSec=10
StartLimitIntervalSec=60
StartLimitBurst=3

# 혹시 남아있는 컨테이너 정리
ExecStartPre=-/usr/bin/docker stop $CONTAINER_NAME
ExecStartPre=-/usr/bin/docker rm $CONTAINER_NAME

ExecStart=/usr/bin/docker run --rm \\
  --name $CONTAINER_NAME \\
  -p 8080:8080 \\
  --env-file $APP_DIR/.env \\
  -v $APP_DIR/app.jar:/app/app.jar:ro \\
  -v $LOG_DIR:/var/log/app \\
  --memory=512m \\
  --memory-swap=512m \\
  --cpus=1 \\
  $DOCKER_IMAGE \\
  java \\
    -Xms256m -Xmx512m \\
    -XX:+UseG1GC \\
    -XX:+HeapDumpOnOutOfMemoryError \\
    -XX:HeapDumpPath=/var/log/app/heapdump.hprof \\
    -jar /app/app.jar \\
    --spring.datasource.url=jdbc:postgresql://\$\${DB_HOST}:\$\${DB_PORT}/\$\${DB_NAME} \\
    --spring.datasource.username=\$\${DB_USERNAME} \\
    --spring.datasource.password=\$\${DB_PASSWORD} \\
    --spring.datasource.hikari.maximum-pool-size=5 \\
    --server.port=8080

ExecStop=/usr/bin/docker stop $CONTAINER_NAME

StandardOutput=append:$LOG_DIR/app.log
StandardError=append:$LOG_DIR/app-error.log

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
echo "systemd 서비스 파일 생성 완료 (docker run 방식)"

echo "=== AfterInstall 완료: $(date) ==="
