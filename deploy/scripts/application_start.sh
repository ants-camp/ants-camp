#!/bin/bash
# ============================================================
# ApplicationStart Hook
# - systemd 서비스 기동 (내부적으로 docker run 실행)
# ============================================================
set -euxo pipefail

SERVICE_NAME="ants-camp-app"
CONTAINER_NAME="ants-camp-app"

echo "=== ApplicationStart 시작: $(date) ==="

systemctl enable "$SERVICE_NAME"
systemctl start "$SERVICE_NAME"

# 컨테이너 기동 안정화 대기
sleep 15

# systemd 서비스 상태 확인
if ! systemctl is-active --quiet "$SERVICE_NAME"; then
  echo "서비스 기동 실패: $SERVICE_NAME"
  journalctl -u "$SERVICE_NAME" --no-pager -n 50
  exit 1
fi

# Docker 컨테이너 실행 여부 확인
if ! docker ps --filter "name=$CONTAINER_NAME" --filter "status=running" | grep -q "$CONTAINER_NAME"; then
  echo "컨테이너 미실행: $CONTAINER_NAME"
  docker ps -a --filter "name=$CONTAINER_NAME"
  exit 1
fi

echo "컨테이너 실행 중: $(docker ps --filter name=$CONTAINER_NAME --format '{{.Names}} ({{.Status}})')"
echo "=== ApplicationStart 완료: $(date) ==="
