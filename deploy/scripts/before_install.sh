#!/bin/bash
# ============================================================
# BeforeInstall Hook
# - 실행 중인 Docker 컨테이너 중지 및 제거
# - 기존 JAR 백업
# ============================================================
set -euxo pipefail

APP_DIR="/opt/ants-camp"
CONTAINER_NAME="ants-camp-app"
BACKUP_DIR="/opt/ants-camp-backup"

echo "=== BeforeInstall 시작: $(date) ==="

mkdir -p "$BACKUP_DIR"

# ── 실행 중인 컨테이너 중지 및 제거 ──────────────────────
if docker ps -q --filter "name=$CONTAINER_NAME" | grep -q .; then
  echo "컨테이너 중지: $CONTAINER_NAME"
  docker stop "$CONTAINER_NAME"
fi

if docker ps -aq --filter "name=$CONTAINER_NAME" | grep -q .; then
  echo "컨테이너 제거: $CONTAINER_NAME"
  docker rm "$CONTAINER_NAME"
fi

# ── 기존 JAR 백업 (롤백 대비) ────────────────────────────
if [ -f "$APP_DIR/app.jar" ]; then
  BACKUP_FILE="$BACKUP_DIR/app.jar.$(date +%Y%m%d_%H%M%S).bak"
  cp "$APP_DIR/app.jar" "$BACKUP_FILE"
  echo "JAR 백업 완료: $BACKUP_FILE"

  # 최근 3개만 보관
  ls -t "$BACKUP_DIR"/*.bak 2>/dev/null | tail -n +4 | xargs -r rm -f
fi

echo "=== BeforeInstall 완료: $(date) ==="
