#!/bin/bash
# ============================================================
# ValidateService Hook
# - Docker 컨테이너 실행 상태 확인
# - Spring Boot Actuator /actuator/health 헬스체크
# - Aurora DB 연결 상태 확인
# - 실패 시 CodeDeploy가 자동 롤백
# ============================================================
set -euo pipefail

APP_PORT=8080
CONTAINER_NAME="ants-camp-app"
HEALTH_ENDPOINT="http://localhost:${APP_PORT}/actuator/health"
MAX_RETRIES=12
RETRY_INTERVAL=10

echo "=== ValidateService 시작: $(date) ==="

# ── 1. Docker 컨테이너 실행 여부 확인 ─────────────────────
if ! docker ps --filter "name=$CONTAINER_NAME" --filter "status=running" | grep -q "$CONTAINER_NAME"; then
  echo "✗ 컨테이너 미실행: $CONTAINER_NAME"
  docker ps -a --filter "name=$CONTAINER_NAME"
  exit 1
fi
echo "✓ 컨테이너 실행 확인: $CONTAINER_NAME"

# ── 2. Actuator 헬스체크 ───────────────────────────────────
echo "헬스체크 엔드포인트: $HEALTH_ENDPOINT"
echo "최대 ${MAX_RETRIES}회 시도, ${RETRY_INTERVAL}초 간격"

for i in $(seq 1 $MAX_RETRIES); do
  HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    --max-time 5 \
    "$HEALTH_ENDPOINT" 2>/dev/null || echo "000")

  if [ "$HTTP_STATUS" = "200" ]; then
    echo "✓ 헬스체크 성공 (시도 $i/$MAX_RETRIES, HTTP $HTTP_STATUS)"

    HEALTH_BODY=$(curl -s --max-time 5 "$HEALTH_ENDPOINT" 2>/dev/null || echo "{}")
    echo "헬스 응답: $HEALTH_BODY"

    # ── 3. Aurora DB 연결 확인 ──────────────────────────────
    DB_STATUS=$(echo "$HEALTH_BODY" | jq -r '.components.db.status // "unknown"' 2>/dev/null || echo "unknown")
    if [ "$DB_STATUS" = "DOWN" ]; then
      echo "✗ Aurora DB 연결 실패 (db.status = DOWN)"
      echo "컨테이너 로그 (최근 30줄):"
      docker logs --tail 30 "$CONTAINER_NAME" 2>/dev/null || true
      exit 1
    fi

    echo "✓ Aurora DB 연결 확인 (db.status = $DB_STATUS)"
    echo "=== ValidateService 완료: $(date) ==="
    exit 0
  fi

  echo "✗ 헬스체크 대기 중 (시도 $i/$MAX_RETRIES, HTTP $HTTP_STATUS)"
  sleep $RETRY_INTERVAL
done

echo "=== ValidateService 실패: 최대 시도 횟수 초과 ==="
echo "컨테이너 로그 (최근 50줄):"
docker logs --tail 50 "$CONTAINER_NAME" 2>/dev/null || true
exit 1
