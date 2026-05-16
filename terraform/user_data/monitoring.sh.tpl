#!/bin/bash
set -euo pipefail
exec > >(tee /var/log/user-data.log | logger -t user-data -s 2>/dev/console) 2>&1

echo "===== [monitoring-ec2] START: $(date) ====="

apt-get update -y
apt-get install -y curl wget unzip jq

# ── Docker + Compose 플러그인 설치 ───────────────────────────
curl -fsSL https://get.docker.com | sh
apt-get install -y docker-compose-plugin
systemctl enable docker
systemctl start docker

# ── 설정 디렉토리 ─────────────────────────────────────────────
mkdir -p /opt/monitoring/{prometheus,loki,grafana/provisioning/datasources,grafana/data,loki-data}

# ── Prometheus 설정 ───────────────────────────────────────────
cat > /opt/monitoring/prometheus/prometheus.yml << EOF
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    cluster: ${project_name}

scrape_configs:
  # ── Node Exporter (시스템 메트릭) ────────────────────────────
  - job_name: node-exporter
    static_configs:
      - targets:
          - ${kafka_ip}:${node_exporter_port}
          - ${infra_ip}:${node_exporter_port}
          - ${domain_ip}:${node_exporter_port}
          - ${domain2_ip}:${node_exporter_port}
          - ${notification_ip}:${node_exporter_port}
          - ${db_ip}:${node_exporter_port}
          - ${gateway_ip}:${node_exporter_port}
        labels:
          group: infrastructure

  # ── Spring Boot Actuator 메트릭 ──────────────────────────────
  - job_name: spring-config-server
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['${infra_ip}:${config_server_port}']
        labels: { service: config-server, ec2: infra-ec2 }

  - job_name: spring-eureka
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['${infra_ip}:${eureka_port}']
        labels: { service: eureka-server, ec2: infra-ec2 }

  - job_name: spring-user
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['${domain_ip}:${user_port}']
        labels: { service: user-service, ec2: domain-ec2 }

  - job_name: spring-asset
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['${domain_ip}:${asset_port}']
        labels: { service: asset-service, ec2: domain-ec2 }

  - job_name: spring-ranking
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['${domain_ip}:${ranking_port}']
        labels: { service: ranking-service, ec2: domain-ec2 }

  - job_name: spring-trade
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['${domain2_ip}:${trade_port}']
        labels: { service: trade-service, ec2: domain2-ec2 }

  - job_name: spring-competition
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['${domain2_ip}:${competition_port}']
        labels: { service: competition-service, ec2: domain2-ec2 }

  - job_name: spring-notification
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['${notification_ip}:${notification_port}']
        labels: { service: notification-service, ec2: notification-ec2 }

  - job_name: spring-assistant
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['${notification_ip}:${assistant_port}']
        labels: { service: assistant-service, ec2: notification-ec2 }
EOF

# ── Loki 설정 ─────────────────────────────────────────────────
cat > /opt/monitoring/loki/config.yaml << EOF
auth_enabled: false

server:
  http_listen_port: ${loki_port}
  grpc_listen_port: 9096

common:
  path_prefix: /var/lib/loki
  storage:
    filesystem:
      chunks_directory: /var/lib/loki/chunks
      rules_directory: /var/lib/loki/rules
  replication_factor: 1
  ring:
    kvstore:
      store: inmemory

schema_config:
  configs:
    - from: 2024-01-01
      store: tsdb
      object_store: filesystem
      schema: v13
      index:
        prefix: index_
        period: 24h

limits_config:
  retention_period: 744h   # 31일

compactor:
  working_directory: /var/lib/loki/compactor
  retention_enabled: true
EOF

# ── Grafana 데이터소스 프로비저닝 ─────────────────────────────
cat > /opt/monitoring/grafana/provisioning/datasources/datasources.yaml << EOF
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:${prometheus_port}
    isDefault: true
    editable: false

  - name: Loki
    type: loki
    access: proxy
    url: http://loki:${loki_port}
    editable: false

  - name: Zipkin
    type: zipkin
    access: proxy
    url: http://zipkin:9411
    editable: false
EOF

# ── Docker Compose 파일 ───────────────────────────────────────
cat > /opt/monitoring/docker-compose.yml << EOF
version: '3.8'

networks:
  monitoring:
    driver: bridge

volumes:
  prometheus-data:
  loki-data:
  grafana-data:

services:
  prometheus:
    image: prom/prometheus:v2.52.0
    container_name: antcamp-prometheus
    restart: unless-stopped
    ports:
      - "${prometheus_port}:${prometheus_port}"
    volumes:
      - /opt/monitoring/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--storage.tsdb.retention.time=15d'
      - '--web.listen-address=:${prometheus_port}'
      - '--web.enable-lifecycle'
    networks:
      - monitoring

  loki:
    image: grafana/loki:3.0.0
    container_name: antcamp-loki
    restart: unless-stopped
    ports:
      - "${loki_port}:${loki_port}"
    volumes:
      - /opt/monitoring/loki/config.yaml:/etc/loki/config.yaml:ro
      - loki-data:/var/lib/loki
    command: -config.file=/etc/loki/config.yaml
    networks:
      - monitoring

  grafana:
    image: grafana/grafana:11.0.0
    container_name: antcamp-grafana
    restart: unless-stopped
    ports:
      - "${grafana_port}:${grafana_port}"
    environment:
      - GF_SERVER_HTTP_PORT=${grafana_port}
      - GF_SECURITY_ADMIN_USER=$${GRAFANA_ADMIN_USER:-admin}
      - GF_SECURITY_ADMIN_PASSWORD=$${GRAFANA_ADMIN_PASSWORD:-ChangeMe!123}
      - GF_AUTH_ANONYMOUS_ENABLED=false
      - GF_USERS_ALLOW_SIGN_UP=false
    volumes:
      - /opt/monitoring/grafana/provisioning:/etc/grafana/provisioning:ro
      - grafana-data:/var/lib/grafana
    depends_on:
      - prometheus
      - loki
    networks:
      - monitoring

  zipkin:
    image: openzipkin/zipkin:latest
    container_name: antcamp-zipkin
    restart: unless-stopped
    ports:
      - "9411:9411"
    networks:
      - monitoring

  node-exporter:
    image: prom/node-exporter:v1.8.0
    container_name: antcamp-node-exporter
    restart: unless-stopped
    ports:
      - "${node_exporter_port}:${node_exporter_port}"
    command:
      - '--web.listen-address=:${node_exporter_port}'
      - '--path.rootfs=/host'
    volumes:
      - /:/host:ro,rslave
    pid: host
    networks:
      - monitoring
EOF

# ── systemd 서비스 (Docker Compose 스택 관리) ─────────────────
cat > /etc/systemd/system/monitoring-stack.service << EOF
[Unit]
Description=AntCamp Monitoring Stack (Prometheus + Loki + Grafana + Zipkin)
After=docker.service network-online.target
Requires=docker.service

[Service]
Type=simple
WorkingDirectory=/opt/monitoring
Restart=always
RestartSec=15
ExecStart=/usr/bin/docker compose up
ExecStop=/usr/bin/docker compose down

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable monitoring-stack
systemctl start monitoring-stack

# ── Promtail (monitoring-ec2 자체 로그 → Loki) ───────────────
PROMTAIL_VERSION="3.0.0"
wget -q "https://github.com/grafana/loki/releases/download/v$${PROMTAIL_VERSION}/promtail-linux-amd64.zip" \
  -O /tmp/promtail.zip
unzip -q /tmp/promtail.zip -d /tmp/
mv /tmp/promtail-linux-amd64 /usr/local/bin/promtail
chmod +x /usr/local/bin/promtail

mkdir -p /etc/promtail

cat > /etc/promtail/config.yaml << EOF
server:
  http_listen_port: ${promtail_port}
  grpc_listen_port: 0

positions:
  filename: /tmp/promtail-positions.yaml

clients:
  - url: http://localhost:${loki_port}/loki/api/v1/push

scrape_configs:
  - job_name: monitoring-docker
    pipeline_stages:
      - json:
          expressions:
            log: log
      - output:
          source: log
    static_configs:
      - targets:
          - localhost
        labels:
          job: monitoring
          host: monitoring-ec2
          __path__: /var/lib/docker/containers/*/*.log

  - job_name: system
    static_configs:
      - targets:
          - localhost
        labels:
          job: system
          host: monitoring-ec2
          __path__: /var/log/syslog
EOF

cat > /etc/systemd/system/promtail.service << EOF
[Unit]
Description=Promtail (log shipper to Loki)
After=network.target monitoring-stack.service

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

echo "===== [monitoring-ec2] DONE: $(date) ====="
echo "  Grafana    → http://$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4):${grafana_port}"
echo "  Prometheus → http://$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4):${prometheus_port}"
echo "  Loki       → http://$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4):${loki_port}"
echo "  Zipkin     → http://$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4):9411"
