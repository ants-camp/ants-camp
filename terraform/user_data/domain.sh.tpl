#!/bin/bash
set -euo pipefail
exec > >(tee /var/log/user-data.log | logger -t user-data -s 2>/dev/console) 2>&1

echo "===== [domain-ec2] START: $(date) ====="

wait_for_http() {
  local url=$1 label=$2
  echo "Waiting for $label ($url)..."
  for i in $(seq 1 90); do
    if curl -sf "$url" > /dev/null 2>&1; then echo "$label is ready!"; return 0; fi
    echo "  attempt $i/90 ..."; sleep 10
  done
  echo "ERROR: $label did not respond" >&2; exit 1
}

# ── 기본 패키지 + Docker ──────────────────────────────────────
apt-get update -y
apt-get install -y curl wget unzip jq netcat-openbsd awscli
curl -fsSL https://get.docker.com | sh
systemctl enable docker
systemctl start docker
docker pull ${docker_image}

mkdir -p /opt/services /opt/logs

# ── S3에서 JAR 다운로드 ───────────────────────────────────────
echo "Downloading JARs from S3..."
aws s3 cp "s3://${s3_bucket}/${user_jar}"    /opt/services/user-service.jar
aws s3 cp "s3://${s3_bucket}/${asset_jar}"   /opt/services/asset-service.jar
aws s3 cp "s3://${s3_bucket}/${ranking_jar}" /opt/services/ranking-service.jar

# ── 의존 서비스 대기 (Config → Eureka) ───────────────────────
wait_for_http "http://${infra_ip}:${config_server_port}/actuator/health" "Config Server"
wait_for_http "http://${infra_ip}:${eureka_port}/actuator/health"        "Eureka Server"

# ── 공통 Spring 환경변수 (모든 서비스 동일) ───────────────────
COMMON_SPRING="-Dspring.config.import=configserver:http://${infra_ip}:${config_server_port} \
  -Deureka.client.service-url.defaultZone=http://${infra_ip}:${eureka_port}/eureka/ \
  -Dspring.kafka.bootstrap-servers=${kafka_ip}:${kafka_port} \
  -Dserver.tomcat.threads.max=${tomcat_threads} \
  -Dspring.datasource.hikari.maximum-pool-size=${hikari_pool}"

# ============================================================
# user-service  heap: ${heap_default} / mem: ${docker_mem_limit}
# ============================================================
cat > /etc/systemd/system/user-service.service <<EOF
[Unit]
Description=User Service (Docker)
After=docker.service
Requires=docker.service

[Service]
Restart=on-failure
RestartSec=15
ExecStartPre=-/usr/bin/docker stop user-service
ExecStartPre=-/usr/bin/docker rm   user-service
ExecStart=/usr/bin/docker run --name user-service \\
  --memory=${docker_mem_limit} --memory-swap=${docker_mem_limit} \\
  -p ${user_port}:${user_port} \\
  -v /opt/services/user-service.jar:/app/app.jar \\
  -v /opt/logs:/opt/logs \\
  -e JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport" \\
  ${docker_image} \\
  java \\
    -Xmx${heap_default} -Xms128m \\
    -Dserver.port=${user_port} \\
    -Dspring.config.import=configserver:http://${infra_ip}:${config_server_port} \\
    -Deureka.client.service-url.defaultZone=http://${infra_ip}:${eureka_port}/eureka/ \\
    -Dspring.kafka.bootstrap-servers=${kafka_ip}:${kafka_port} \\
    -Dserver.tomcat.threads.max=${tomcat_threads} \\
    -Dspring.datasource.hikari.maximum-pool-size=${hikari_pool} \\
    -Dlogging.file.name=/opt/logs/user-service.log \\
    -jar /app/app.jar
ExecStop=/usr/bin/docker stop user-service

[Install]
WantedBy=multi-user.target
EOF

# ============================================================
# asset-service  heap: ${heap_default} / mem: ${docker_mem_limit}
# ============================================================
cat > /etc/systemd/system/asset-service.service <<EOF
[Unit]
Description=Asset Service (Docker)
After=docker.service
Requires=docker.service

[Service]
Restart=on-failure
RestartSec=15
ExecStartPre=-/usr/bin/docker stop asset-service
ExecStartPre=-/usr/bin/docker rm   asset-service
ExecStart=/usr/bin/docker run --name asset-service \\
  --memory=${docker_mem_limit} --memory-swap=${docker_mem_limit} \\
  -p ${asset_port}:${asset_port} \\
  -v /opt/services/asset-service.jar:/app/app.jar \\
  -v /opt/logs:/opt/logs \\
  -e JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport" \\
  ${docker_image} \\
  java \\
    -Xmx${heap_default} -Xms128m \\
    -Dserver.port=${asset_port} \\
    -Dspring.config.import=configserver:http://${infra_ip}:${config_server_port} \\
    -Deureka.client.service-url.defaultZone=http://${infra_ip}:${eureka_port}/eureka/ \\
    -Dspring.kafka.bootstrap-servers=${kafka_ip}:${kafka_port} \\
    -Dserver.tomcat.threads.max=${tomcat_threads} \\
    -Dspring.datasource.hikari.maximum-pool-size=${hikari_pool} \\
    -Dlogging.file.name=/opt/logs/asset-service.log \\
    -jar /app/app.jar
ExecStop=/usr/bin/docker stop asset-service

[Install]
WantedBy=multi-user.target
EOF

# ============================================================
# ranking-service  heap: ${heap_default} / mem: ${docker_mem_limit}
# ============================================================
cat > /etc/systemd/system/ranking-service.service <<EOF
[Unit]
Description=Ranking Service (Docker)
After=docker.service
Requires=docker.service

[Service]
Restart=on-failure
RestartSec=15
ExecStartPre=-/usr/bin/docker stop ranking-service
ExecStartPre=-/usr/bin/docker rm   ranking-service
ExecStart=/usr/bin/docker run --name ranking-service \\
  --memory=${docker_mem_limit} --memory-swap=${docker_mem_limit} \\
  -p ${ranking_port}:${ranking_port} \\
  -v /opt/services/ranking-service.jar:/app/app.jar \\
  -v /opt/logs:/opt/logs \\
  -e JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport" \\
  ${docker_image} \\
  java \\
    -Xmx${heap_default} -Xms128m \\
    -Dserver.port=${ranking_port} \\
    -Dspring.config.import=configserver:http://${infra_ip}:${config_server_port} \\
    -Deureka.client.service-url.defaultZone=http://${infra_ip}:${eureka_port}/eureka/ \\
    -Dspring.kafka.bootstrap-servers=${kafka_ip}:${kafka_port} \\
    -Dserver.tomcat.threads.max=${tomcat_threads} \\
    -Dspring.datasource.hikari.maximum-pool-size=${hikari_pool} \\
    -Dlogging.file.name=/opt/logs/ranking-service.log \\
    -jar /app/app.jar
ExecStop=/usr/bin/docker stop ranking-service

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable user-service asset-service ranking-service
systemctl start user-service asset-service ranking-service

# ── Node Exporter ─────────────────────────────────────────────
NODE_EXPORTER_VERSION="1.8.0"
wget -q "https://github.com/prometheus/node_exporter/releases/download/v$${NODE_EXPORTER_VERSION}/node_exporter-$${NODE_EXPORTER_VERSION}.linux-amd64.tar.gz" \
  -O /tmp/node_exporter.tgz
tar -xzf /tmp/node_exporter.tgz -C /tmp/
mv /tmp/node_exporter-$${NODE_EXPORTER_VERSION}.linux-amd64/node_exporter /usr/local/bin/

cat > /etc/systemd/system/node_exporter.service <<EOF
[Unit]
Description=Prometheus Node Exporter
After=network.target
[Service]
Type=simple
ExecStart=/usr/local/bin/node_exporter --web.listen-address=:${node_exporter_port}
Restart=on-failure
[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable node_exporter
systemctl start node_exporter

# ── Promtail ──────────────────────────────────────────────────
PROMTAIL_VERSION="3.0.0"
wget -q "https://github.com/grafana/loki/releases/download/v$${PROMTAIL_VERSION}/promtail-linux-amd64.zip" \
  -O /tmp/promtail.zip
unzip -q /tmp/promtail.zip -d /tmp/
mv /tmp/promtail-linux-amd64 /usr/local/bin/promtail
chmod +x /usr/local/bin/promtail
mkdir -p /etc/promtail

cat > /etc/promtail/config.yaml <<EOF
server:
  http_listen_port: ${promtail_port}
  grpc_listen_port: 0
positions:
  filename: /tmp/promtail-positions.yaml
clients:
  - url: http://${loki_ip}:${loki_port}/loki/api/v1/push
scrape_configs:
  - job_name: domain-services
    static_configs:
      - targets: [localhost]
        labels: { job: user-service,    host: domain-ec2, __path__: /opt/logs/user-service.log }
      - targets: [localhost]
        labels: { job: asset-service,   host: domain-ec2, __path__: /opt/logs/asset-service.log }
      - targets: [localhost]
        labels: { job: ranking-service, host: domain-ec2, __path__: /opt/logs/ranking-service.log }
  - job_name: system
    static_configs:
      - targets: [localhost]
        labels: { job: system, host: domain-ec2, __path__: /var/log/syslog }
EOF

cat > /etc/systemd/system/promtail.service <<EOF
[Unit]
Description=Promtail
After=network.target
[Service]
Type=simple
ExecStart=/usr/local/bin/promtail -config.file=/etc/promtail/config.yaml
Restart=on-failure
RestartSec=10
[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable promtail
systemctl start promtail

echo "===== [domain-ec2] DONE: $(date) ====="
