#!/bin/bash
set -euo pipefail

# ────────────────────────────────────────────────
# 롤백 대상 컨테이너 (비즈니스 서비스만)
# ────────────────────────────────────────────────
ROLLBACK_CONTAINERS=(
  antcamp-user
  antcamp-trade
  antcamp-asset
  antcamp-stock
  antcamp-portfolio
  antcamp-competition
  antcamp-ranking
  antcamp-assistant
)

# 로컬 테스트용 서비스 (notification 스택만)
LOCAL_SERVICES=(
  postgres
  redis
  eureka-server
  prometheus
  alertmanager
  loki
  promtail
  grafana
  notification-service
)

# ────────────────────────────────────────────────
# 배포 전 현재 이미지를 :rollback 태그로 보존
# ────────────────────────────────────────────────
tag_rollback() {
  echo "[1/3] 롤백 이미지 태그 보존"
  for container in "${ROLLBACK_CONTAINERS[@]}"; do
    if docker inspect "$container" &>/dev/null; then
      image=$(docker inspect "$container" --format '{{.Config.Image}}')
      docker tag "$image" "${container}:rollback"
      echo "  ✓ ${container}:rollback 저장"
    else
      echo "  - ${container}: 실행 중 아님, 스킵"
    fi
  done
}

# ────────────────────────────────────────────────
# 전체 배포
# ────────────────────────────────────────────────
deploy_all() {
  tag_rollback

  echo "[2/3] 전체 이미지 빌드"
  docker compose build

  echo "[3/3] 컨테이너 재시작"
  docker compose up -d

  echo "배포 완료"
}

# ────────────────────────────────────────────────
# 로컬 테스트 (notification 스택)
# ────────────────────────────────────────────────
deploy_local() {
  echo "[1/1] 로컬 테스트 스택 시작"
  docker compose up -d --build "${LOCAL_SERVICES[@]}"

  # .env에서 포트 읽기
  if [ -f .env ]; then
    set -a
    # shellcheck disable=SC1091
    source .env
    set +a
  fi

  echo ""
  echo "스택 시작 완료"
  echo "  Grafana      : http://localhost:${GRAFANA_PORT:-3000}"
  echo "  Prometheus   : http://localhost:${PROMETHEUS_PORT:-9090}"
  echo "  Alertmanager : http://localhost:${ALERTMANAGER_PORT:-9093}"
  echo "  Notification : http://localhost:${NOTIFICATION_SERVER_PORT:-8098}"
}

# ────────────────────────────────────────────────
# 전체 중지
# ────────────────────────────────────────────────
stop_all() {
  echo "전체 컨테이너 중지"
  docker compose down
}

# ────────────────────────────────────────────────
# 엔트리포인트
# ────────────────────────────────────────────────
case "${1:-all}" in
  all)
    deploy_all
    ;;
  local)
    deploy_local
    ;;
  down)
    stop_all
    ;;
  *)
    echo "Usage: $0 [all|local|down]"
    echo ""
    echo "  all   : 롤백 태그 보존 후 전체 빌드 & 배포 (EC2 운영)"
    echo "  local : notification 스택만 시작 (로컬 테스트)"
    echo "  down  : 전체 컨테이너 중지"
    exit 1
    ;;
esac