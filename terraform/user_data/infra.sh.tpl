#!/bin/bash
set -euo pipefail
exec > >(tee /var/log/user-data.log | logger -t user-data -s 2>/dev/console) 2>&1

echo "===== [infra-ec2] START: $(date) ====="

# ── 공통 함수 ─────────────────────────────────────────────────
wait_for_http() {
  local url=$1 label=$2
  echo "Waiting for $label ($url)..."
  for i in $(seq 1 90); do
    if curl -sf "$url" > /dev/null 2>&1; then echo "$label is ready!"; return 0; fi
    echo "  attempt $i/90 ..."; sleep 10
  done
  echo "ERROR: $label did not respond" >&2; exit 1
}

# ── 기본 패키지 ───────────────────────────────────────────────
apt-get update -y
apt-get install -y curl wget unzip jq netcat-openbsd awscli

# ── Docker 설치 ───────────────────────────────────────────────
echo "Installing Docker..."
curl -fsSL https://get.docker.com | sh
systemctl enable docker
systemctl start docker

# Java 이미지 사전 pull (서비스 기동 지연 최소화)
docker pull ${docker_image}

# ── 서비스 디렉토리 ───────────────────────────────────────────
mkdir -p /opt/services /opt/logs

# ── S3에서 JAR 다운로드 ───────────────────────────────────────
echo "Downloading JARs from S3..."
aws s3 cp "s3://${s3_bucket}/${config_server_jar}" /opt/services/config-server.jar
aws s3 cp "s3://${s3_bucket}/${eureka_jar}"         /opt/services/eureka-server.jar
aws s3 cp "s3://${s3_bucket}/${kafka_ui_jar}"       /opt/services/kafka-ui.jar

# ── Kafka 기동 대기 ───────────────────────────────────────────
echo "Waiting for Kafka (${kafka_ip}:${kafka_port})..."
for i in $(seq 1 60); do
  nc -z "${kafka_ip}" "${kafka_port}" 2>/dev/null && { echo "Kafka ready!"; break; }
  echo "  attempt $i/60 ..."; sleep 10
done

# ============================================================
# STEP 1: Config Server
#   heap: ${heap_config_server} / docker memory: ${docker_mem_limit}
# ============================================================
cat > /etc/systemd/system/config-server.service <<EOF
[Unit]
Description=Config Server (Docker)
After=docker.service
Requires=docker.service

[Service]
Restart=on-failure
RestartSec=15
ExecStartPre=-/usr/bin/docker stop config-server
ExecStartPre=-/usr/bin/docker rm   config-server
ExecStart=/usr/bin/docker run --name config-server \\
  --memory=${docker_mem_limit} --memory-swap=${docker_mem_limit} \\
  -p ${config_server_port}:${config_server_port} \\
  -v /opt/services/config-server.jar:/app/app.jar \\
  -v /opt/logs:/opt/logs \\
  -e JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport" \\
  ${docker_image} \\
  java \\
    -Xmx${heap_config_server} -Xms128m \\
    -Dserver.port=${config_server_port} \\
    -Dserver.tomcat.threads.max=${tomcat_threads} \\
    -Dspring.cloud.config.server.git.uri=${config_git_uri} \\
    -Dspring.cloud.config.server.git.username=${config_git_username} \\
    -Dspring.cloud.config.server.git.password=${config_git_password} \\
    -Dlogging.file.name=/opt/logs/config-server.log \\
    -jar /app/app.jar
ExecStop=/usr/bin/docker stop config-server

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable config-server
systemctl start config-server

wait_for_http "http://localhost:${config_server_port}/actuator/health" "Config Server"

# ============================================================
# STEP 2: Eureka Server
#   heap: ${heap_eureka} / docker memory: ${docker_mem_limit}
# ============================================================
cat > /etc/systemd/system/eureka-server.service <<EOF
[Unit]
Description=Eureka Server (Docker)
After=docker.service config-server.service
Requires=docker.service

[Service]
Restart=on-failure
RestartSec=15
ExecStartPre=-/usr/bin/docker stop eureka-server
ExecStartPre=-/usr/bin/docker rm   eureka-server
ExecStart=/usr/bin/docker run --name eureka-server \\
  --memory=${docker_mem_limit} --memory-swap=${docker_mem_limit} \\
  -p ${eureka_port}:${eureka_port} \\
  -v /opt/services/eureka-server.jar:/app/app.jar \\
  -v /opt/logs:/opt/logs \\
  -e JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport" \\
  ${docker_image} \\
  java \\
    -Xmx${heap_eureka} -Xms128m \\
    -Dserver.port=${eureka_port} \\
    -Dserver.tomcat.threads.max=${tomcat_threads} \\
    -Dspring.config.import=configserver:http://$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4):${config_server_port} \\
    -Deureka.client.registerWithEureka=false \\
    -Deureka.client.fetchRegistry=false \\
    -Dlogging.file.name=/opt/logs/eureka-server.log \\
    -jar /app/app.jar
ExecStop=/usr/bin/docker stop eureka-server

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable eureka-server
systemctl start eureka-server

wait_for_http "http://localhost:${eureka_port}/actuator/health" "Eureka Server"

# ============================================================
# STEP 3: Kafka UI
#   heap: ${heap_kafka_ui} / docker memory: ${docker_mem_limit}
# ============================================================
cat > /etc/systemd/system/kafka-ui.service <<EOF
[Unit]
Description=Kafka UI (Docker)
After=docker.service eureka-server.service
Requires=docker.service

[Service]
Restart=on-failure
RestartSec=15
ExecStartPre=-/usr/bin/docker stop kafka-ui
ExecStartPre=-/usr/bin/docker rm   kafka-ui
ExecStart=/usr/bin/docker run --name kafka-ui \\
  --memory=${docker_mem_limit} --memory-swap=${docker_mem_limit} \\
  -p ${kafka_ui_port}:${kafka_ui_port} \\
  -v /opt/services/kafka-ui.jar:/app/app.jar \\
  -v /opt/logs:/opt/logs \\
  -e JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport" \\
  ${docker_image} \\
  java \\
    -Xmx${heap_kafka_ui} -Xms128m \\
    -Dserver.port=${kafka_ui_port} \\
    -Dkafka.clusters.0.name=local \\
    -Dkafka.clusters.0.bootstrapServers=${kafka_ip}:${kafka_port} \\
    -Dlogging.file.name=/opt/logs/kafka-ui.log \\
    -jar /app/app.jar
ExecStop=/usr/bin/docker stop kafka-ui

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable kafka-ui
systemctl start kafka-ui

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
  - job_name: infra-services
    static_configs:
      - targets: [localhost]
        labels: { job: config-server, host: infra-ec2, __path__: /opt/logs/config-server.log }
      - targets: [localhost]
        labels: { job: eureka-server, host: infra-ec2, __path__: /opt/logs/eureka-server.log }
      - targets: [localhost]
        labels: { job: kafka-ui,      host: infra-ec2, __path__: /opt/logs/kafka-ui.log }
  - job_name: system
    static_configs:
      - targets: [localhost]
        labels: { job: system, host: infra-ec2, __path__: /var/log/syslog }
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

echo "===== [infra-ec2] DONE: $(date) ====="
