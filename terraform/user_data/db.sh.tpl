#!/bin/bash
# ============================================================
# db-ec2: Redis 전용 (PostgreSQL은 Aurora RDS로 이관)
# ============================================================
set -euo pipefail
exec > >(tee /var/log/user-data.log | logger -t user-data -s 2>/dev/console) 2>&1

echo "===== [db-ec2] START: $(date) ====="

apt-get update -y
apt-get install -y curl wget unzip jq

# ── Redis 설치 ────────────────────────────────────────────────
apt-get install -y redis-server

# bind 0.0.0.0 (SG에서 포트 차단)
sed -i 's/^bind 127.0.0.1 -::1/bind 0.0.0.0/' /etc/redis/redis.conf

# Redis 비밀번호 설정
sed -i 's/# requirepass foobared/requirepass ChangeMe!Redis2024/' /etc/redis/redis.conf

# maxmemory 설정 (t3.micro 기준)
sed -i 's/# maxmemory <bytes>/maxmemory 256mb/' /etc/redis/redis.conf
sed -i 's/# maxmemory-policy noeviction/maxmemory-policy allkeys-lru/' /etc/redis/redis.conf

systemctl enable redis-server
systemctl restart redis-server

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

# ── Redis Exporter ────────────────────────────────────────────
REDIS_EXPORTER_VERSION="1.61.0"
wget -q "https://github.com/oliver006/redis_exporter/releases/download/v$${REDIS_EXPORTER_VERSION}/redis_exporter-v$${REDIS_EXPORTER_VERSION}.linux-amd64.tar.gz" \
  -O /tmp/redis_exporter.tgz
tar -xzf /tmp/redis_exporter.tgz -C /tmp/
mv /tmp/redis_exporter-v$${REDIS_EXPORTER_VERSION}.linux-amd64/redis_exporter /usr/local/bin/

cat > /etc/systemd/system/redis_exporter.service <<EOF
[Unit]
Description=Redis Exporter
After=network.target redis-server.service

[Service]
Type=simple
Environment="REDIS_PASSWORD=ChangeMe!Redis2024"
ExecStart=/usr/local/bin/redis_exporter \
  --redis.addr=redis://localhost:${redis_port} \
  --web.listen-address=:9121
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable redis_exporter
systemctl start redis_exporter

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
  - job_name: system
    static_configs:
      - targets: [localhost]
        labels:
          job: system
          host: db-ec2
          __path__: /var/log/syslog

  - job_name: redis
    static_configs:
      - targets: [localhost]
        labels:
          job: redis
          host: db-ec2
          __path__: /var/log/redis/*.log
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

echo "===== [db-ec2] DONE: $(date) ====="
