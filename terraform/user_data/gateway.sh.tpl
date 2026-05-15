#!/bin/bash
set -euo pipefail
exec > >(tee /var/log/user-data.log | logger -t user-data -s 2>/dev/console) 2>&1

echo "===== [gateway-ec2] START: $(date) ====="

apt-get update -y
apt-get install -y curl wget unzip jq nginx awscli
curl -fsSL https://get.docker.com | sh
systemctl enable docker
systemctl start docker

# ── Nginx 리버스 프록시 설정 ──────────────────────────────────
cat > /etc/nginx/sites-available/ants-camp <<NGINX_EOF
# Rate Limiting
limit_req_zone \$binary_remote_addr zone=api:10m rate=30r/s;

# Upstream 정의
upstream user_service        { server ${domain_ip}:${user_port}; }
upstream asset_service       { server ${domain_ip}:${asset_port}; }
upstream ranking_service     { server ${domain_ip}:${ranking_port}; }
upstream trade_service       { server ${domain2_ip}:${trade_port}; }
upstream competition_service { server ${domain2_ip}:${competition_port}; }
upstream notification_svc    { server ${notification_ip}:${notification_port}; }
upstream assistant_service   { server ${notification_ip}:${assistant_port}; }
upstream eureka_dashboard    { server ${infra_ip}:${eureka_port}; }
upstream kafka_ui_dashboard  { server ${infra_ip}:${kafka_ui_port}; }

server {
    listen 80;
    server_name _;

    location /health {
        return 200 "ok\n";
        add_header Content-Type text/plain;
    }

    location /api/users/ {
        limit_req zone=api burst=20 nodelay;
        proxy_pass http://user_service/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 10s;
        proxy_read_timeout 60s;
    }

    location /api/assets/ {
        limit_req zone=api burst=20 nodelay;
        proxy_pass http://asset_service/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location /api/rankings/ {
        limit_req zone=api burst=20 nodelay;
        proxy_pass http://ranking_service/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location /api/trades/ {
        limit_req zone=api burst=20 nodelay;
        proxy_pass http://trade_service/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location /api/competitions/ {
        limit_req zone=api burst=20 nodelay;
        proxy_pass http://competition_service/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location /api/notifications/ {
        proxy_pass http://notification_svc/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location /api/assistant/ {
        proxy_pass http://assistant_service/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location /internal/eureka/ {
        proxy_pass http://eureka_dashboard/;
        proxy_set_header Host \$host;
    }

    location /internal/kafka-ui/ {
        proxy_pass http://kafka_ui_dashboard/;
        proxy_set_header Host \$host;
    }
}
NGINX_EOF

ln -sf /etc/nginx/sites-available/ants-camp /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default
nginx -t
systemctl enable nginx
systemctl restart nginx

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
  - job_name: nginx
    static_configs:
      - targets: [localhost]
        labels: { job: nginx-access, host: gateway-ec2, __path__: /var/log/nginx/access.log }
      - targets: [localhost]
        labels: { job: nginx-error,  host: gateway-ec2, __path__: /var/log/nginx/error.log }
  - job_name: system
    static_configs:
      - targets: [localhost]
        labels: { job: system, host: gateway-ec2, __path__: /var/log/syslog }
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

echo "===== [gateway-ec2] DONE: $(date) ====="
