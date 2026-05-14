#!/bin/bash
set -euo pipefail
exec > >(tee /var/log/user-data.log | logger -t user-data -s 2>/dev/console) 2>&1

echo "===== [monitoring-ec2] START: $(date) ====="

apt-get update -y
apt-get install -y curl wget unzip jq adduser libfontconfig1

# ── Prometheus 설치 ───────────────────────────────────────────
PROMETHEUS_VERSION="2.52.0"
wget -q "https://github.com/prometheus/prometheus/releases/download/v$${PROMETHEUS_VERSION}/prometheus-$${PROMETHEUS_VERSION}.linux-amd64.tar.gz" \
  -O /tmp/prometheus.tgz
tar -xzf /tmp/prometheus.tgz -C /tmp/
mv /tmp/prometheus-$${PROMETHEUS_VERSION}.linux-amd64/prometheus    /usr/local/bin/
mv /tmp/prometheus-$${PROMETHEUS_VERSION}.linux-amd64/promtool      /usr/local/bin/

mkdir -p /etc/prometheus /var/lib/prometheus

# Prometheus 설정 파일
cat > /etc/prometheus/prometheus.yml <<EOF
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    cluster: ${project_name}

alerting:
  alertmanagers: []

rule_files: []

scrape_configs:
  # ── Node Exporter (시스템 메트릭) ────────────────────────
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

  # ── Spring Boot Actuator (애플리케이션 메트릭) ───────────
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

cat > /etc/systemd/system/prometheus.service <<EOF
[Unit]
Description=Prometheus
After=network.target

[Service]
Type=simple
User=root
ExecStart=/usr/local/bin/prometheus \
  --config.file=/etc/prometheus/prometheus.yml \
  --storage.tsdb.path=/var/lib/prometheus \
  --storage.tsdb.retention.time=15d \
  --web.listen-address=:${prometheus_port} \
  --web.enable-lifecycle
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable prometheus
systemctl start prometheus

# ── Loki 설치 ─────────────────────────────────────────────────
LOKI_VERSION="3.0.0"
wget -q "https://github.com/grafana/loki/releases/download/v$${LOKI_VERSION}/loki-linux-amd64.zip" \
  -O /tmp/loki.zip
unzip -q /tmp/loki.zip -d /tmp/
mv /tmp/loki-linux-amd64 /usr/local/bin/loki
chmod +x /usr/local/bin/loki

mkdir -p /etc/loki /var/lib/loki

cat > /etc/loki/config.yaml <<EOF
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

cat > /etc/systemd/system/loki.service <<EOF
[Unit]
Description=Grafana Loki
After=network.target

[Service]
Type=simple
User=root
ExecStart=/usr/local/bin/loki -config.file=/etc/loki/config.yaml
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable loki
systemctl start loki

# ── Grafana 설치 ──────────────────────────────────────────────
wget -q "https://dl.grafana.com/oss/release/grafana_11.0.0_amd64.deb" \
  -O /tmp/grafana.deb
dpkg -i /tmp/grafana.deb

# 기본 설정 (익명 접근 비활성화)
cat > /etc/grafana/grafana.ini <<EOF
[server]
http_port = ${grafana_port}

[security]
admin_user = admin
admin_password = ChangeMe!123

[auth.anonymous]
enabled = false

[users]
allow_sign_up = false
EOF

# Prometheus / Loki 데이터소스 자동 프로비저닝
mkdir -p /etc/grafana/provisioning/datasources

cat > /etc/grafana/provisioning/datasources/datasources.yaml <<EOF
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://localhost:${prometheus_port}
    isDefault: true
    editable: false

  - name: Loki
    type: loki
    access: proxy
    url: http://localhost:${loki_port}
    editable: false
EOF

systemctl daemon-reload
systemctl enable grafana-server
systemctl start grafana-server

# ── Node Exporter (monitoring 자신의 메트릭도 수집) ───────────
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

echo "===== [monitoring-ec2] DONE: $(date) ====="
echo "  Grafana  → http://$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4):${grafana_port}"
echo "  Prometheus → http://$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4):${prometheus_port}"
echo "  Loki     → http://$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4):${loki_port}"
