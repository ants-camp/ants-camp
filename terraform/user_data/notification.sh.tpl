#!/bin/bash
set -euo pipefail
exec > >(tee /var/log/user-data.log | logger -t user-data -s 2>/dev/console) 2>&1

echo "===== [notification-ec2] START: $(date) ====="

wait_for_http() {
  local url=$1 label=$2
  echo "Waiting for $label ($url)..."
  for i in $(seq 1 90); do
    if curl -sf "$url" > /dev/null 2>&1; then echo "$label is ready!"; return 0; fi
    echo "  attempt $i/90 ..."; sleep 10
  done
  echo "ERROR: $label did not respond" >&2; exit 1
}

apt-get update -y
apt-get install -y curl wget unzip jq netcat-openbsd awscli nginx certbot python3-certbot-nginx
curl -fsSL https://get.docker.com | sh
systemctl enable docker
systemctl start docker
docker pull ${docker_image}

mkdir -p /opt/services /opt/logs

echo "Downloading JARs from S3..."
aws s3 cp "s3://${s3_bucket}/${notification_jar}" /opt/services/notification-service.jar
aws s3 cp "s3://${s3_bucket}/${assistant_jar}"    /opt/services/assistant-service.jar

wait_for_http "http://${infra_ip}:${config_server_port}/actuator/health" "Config Server"
wait_for_http "http://${infra_ip}:${eureka_port}/actuator/health"        "Eureka Server"

# ============================================================
# notification-service
#   heap: ${heap_notification} / mem: ${docker_mem_limit}
# ============================================================
cat > /etc/systemd/system/notification-service.service <<EOF
[Unit]
Description=Notification Service (Docker)
After=docker.service
Requires=docker.service

[Service]
Restart=on-failure
RestartSec=15
ExecStartPre=-/usr/bin/docker stop notification-service
ExecStartPre=-/usr/bin/docker rm   notification-service
ExecStart=/usr/bin/docker run --name notification-service \\
  --memory=${docker_mem_limit} --memory-swap=${docker_mem_limit} \\
  -p ${notification_port}:${notification_port} \\
  -v /opt/services/notification-service.jar:/app/app.jar \\
  -v /opt/logs:/opt/logs \\
  -e JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport" \\
  ${docker_image} \\
  java \\
    -Xmx${heap_notification} -Xms128m \\
    -Dserver.port=${notification_port} \\
    -Dspring.config.import=configserver:http://${infra_ip}:${config_server_port} \\
    -Deureka.client.service-url.defaultZone=http://${infra_ip}:${eureka_port}/eureka/ \\
    -Dspring.kafka.bootstrap-servers=${kafka_ip}:${kafka_port} \\
    -Dserver.tomcat.threads.max=${tomcat_threads} \\
    -Dspring.datasource.hikari.maximum-pool-size=${hikari_pool} \\
    -Dlogging.file.name=/opt/logs/notification-service.log \\
    -jar /app/app.jar
ExecStop=/usr/bin/docker stop notification-service

[Install]
WantedBy=multi-user.target
EOF

# ============================================================
# assistant-service
#   heap: ${heap_assistant} (512m) / mem: ${docker_mem_limit}
# ============================================================
cat > /etc/systemd/system/assistant-service.service <<EOF
[Unit]
Description=Assistant Service (Docker)
After=docker.service
Requires=docker.service

[Service]
Restart=on-failure
RestartSec=15
ExecStartPre=-/usr/bin/docker stop assistant-service
ExecStartPre=-/usr/bin/docker rm   assistant-service
ExecStart=/usr/bin/docker run --name assistant-service \\
  --memory=${docker_mem_limit} --memory-swap=${docker_mem_limit} \\
  -p ${assistant_port}:${assistant_port} \\
  -v /opt/services/assistant-service.jar:/app/app.jar \\
  -v /opt/logs:/opt/logs \\
  -e JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport" \\
  ${docker_image} \\
  java \\
    -Xmx${heap_assistant} -Xms256m \\
    -Dserver.port=${assistant_port} \\
    -Dspring.config.import=configserver:http://${infra_ip}:${config_server_port} \\
    -Deureka.client.service-url.defaultZone=http://${infra_ip}:${eureka_port}/eureka/ \\
    -Dspring.kafka.bootstrap-servers=${kafka_ip}:${kafka_port} \\
    -Dserver.tomcat.threads.max=${tomcat_threads} \\
    -Dspring.datasource.hikari.maximum-pool-size=${hikari_pool} \\
    -Dlogging.file.name=/opt/logs/assistant-service.log \\
    -jar /app/app.jar
ExecStop=/usr/bin/docker stop assistant-service

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable notification-service assistant-service
systemctl start notification-service assistant-service

# ── Nginx (HTTPS 처리) ────────────────────────────────────────
cat > /etc/nginx/sites-available/notification <<'NGINX_EOF'
server {
    listen 80;
    server_name _;

    location /notification/ {
        proxy_pass http://localhost:${notification_port}/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /assistant/ {
        proxy_pass http://localhost:${assistant_port}/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # SSE 지원 (알림 스트림)
    location /notification/subscribe {
        proxy_pass http://localhost:${notification_port}/subscribe;
        proxy_http_version 1.1;
        proxy_set_header Connection '';
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 3600s;
    }
}
NGINX_EOF

ln -sf /etc/nginx/sites-available/notification /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default
nginx -t && systemctl enable nginx && systemctl restart nginx

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
  - job_name: notification-services
    static_configs:
      - targets: [localhost]
        labels: { job: notification-service, host: notification-ec2, __path__: /opt/logs/notification-service.log }
      - targets: [localhost]
        labels: { job: assistant-service,    host: notification-ec2, __path__: /opt/logs/assistant-service.log }
      - targets: [localhost]
        labels: { job: nginx, host: notification-ec2, __path__: /var/log/nginx/*.log }
  - job_name: system
    static_configs:
      - targets: [localhost]
        labels: { job: system, host: notification-ec2, __path__: /var/log/syslog }
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

echo "===== [notification-ec2] DONE: $(date) ====="
echo "  notification-service: ${heap_notification} heap, ${docker_mem_limit} container memory"
echo "  assistant-service:    ${heap_assistant} heap, ${docker_mem_limit} container memory"
