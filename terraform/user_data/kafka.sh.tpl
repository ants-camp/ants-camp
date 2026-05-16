#!/bin/bash
set -euo pipefail
exec > >(tee /var/log/user-data.log | logger -t user-data -s 2>/dev/console) 2>&1

echo "===== [kafka-ec2] START: $(date) ====="

# ── 패키지 설치 ───────────────────────────────────────────────
apt-get update -y
apt-get install -y curl wget unzip jq netcat-openbsd awscli

# ── Docker 설치 ───────────────────────────────────────────────
echo "Installing Docker..."
curl -fsSL https://get.docker.com | sh
systemctl enable docker
systemctl start docker

# ── 데이터 디렉토리 ───────────────────────────────────────────
mkdir -p /var/kafka/data
chown -R 1001:1001 /var/kafka/data   # bitnami/kafka 컨테이너 UID

# ── Private IP 획득 (user_data 실행 시점에 결정) ─────────────
PRIVATE_IP=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4)
echo "Private IP: $PRIVATE_IP"

# ── Kafka 이미지 사전 pull ────────────────────────────────────
docker pull bitnami/kafka:3.7

# ── Kafka 기동 래퍼 스크립트 ─────────────────────────────────
# Private IP는 user_data 실행 시점에 스크립트에 고정됨
cat > /usr/local/bin/start-kafka.sh << SCRIPTEOF
#!/bin/bash
exec docker run --rm --name kafka \
  --log-driver json-file \
  --log-opt max-size=100m \
  --log-opt max-file=3 \
  -p ${kafka_broker_port}:${kafka_broker_port} \
  -p ${kafka_controller_port}:${kafka_controller_port} \
  -v /var/kafka/data:/bitnami/kafka/data \
  -e KAFKA_CFG_NODE_ID=${node_id} \
  -e KAFKA_CFG_PROCESS_ROLES=broker,controller \
  -e "KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=${node_id}@0.0.0.0:${kafka_controller_port}" \
  -e "KAFKA_CFG_LISTENERS=PLAINTEXT://:${kafka_broker_port},CONTROLLER://:${kafka_controller_port}" \
  -e "KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://$PRIVATE_IP:${kafka_broker_port}" \
  -e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
  -e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE=true \
  -e KAFKA_CFG_NUM_PARTITIONS=3 \
  -e KAFKA_CFG_DEFAULT_REPLICATION_FACTOR=1 \
  -e KAFKA_CFG_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  -e KAFKA_CFG_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 \
  -e KAFKA_CFG_TRANSACTION_STATE_LOG_MIN_ISR=1 \
  -e KAFKA_CFG_LOG_RETENTION_HOURS=168 \
  -e KAFKA_CFG_LOG_SEGMENT_BYTES=1073741824 \
  -e "KAFKA_HEAP_OPTS=-Xmx${kafka_heap} -Xms${kafka_heap}" \
  bitnami/kafka:3.7
SCRIPTEOF

chmod +x /usr/local/bin/start-kafka.sh

# ── systemd 서비스 ────────────────────────────────────────────
cat > /etc/systemd/system/kafka.service << EOF
[Unit]
Description=Apache Kafka (KRaft, Docker)
After=docker.service network-online.target
Requires=docker.service

[Service]
Type=simple
Restart=always
RestartSec=15
ExecStartPre=-/usr/bin/docker stop kafka
ExecStartPre=-/usr/bin/docker rm kafka
ExecStart=/usr/local/bin/start-kafka.sh
ExecStop=/usr/bin/docker stop kafka
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable kafka
systemctl start kafka

# ── Kafka 기동 대기 ───────────────────────────────────────────
echo "Waiting for Kafka to be ready..."
for i in $(seq 1 60); do
  if nc -z localhost ${kafka_broker_port} 2>/dev/null; then
    echo "Kafka is ready!"
    break
  fi
  echo "  attempt $i/60..."
  sleep 5
done

# ── Node Exporter (시스템 메트릭 → Prometheus) ────────────────
NODE_EXPORTER_VERSION="1.8.0"
wget -q "https://github.com/prometheus/node_exporter/releases/download/v$${NODE_EXPORTER_VERSION}/node_exporter-$${NODE_EXPORTER_VERSION}.linux-amd64.tar.gz" \
  -O /tmp/node_exporter.tgz
tar -xzf /tmp/node_exporter.tgz -C /tmp/
mv /tmp/node_exporter-$${NODE_EXPORTER_VERSION}.linux-amd64/node_exporter /usr/local/bin/

cat > /etc/systemd/system/node_exporter.service << EOF
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

# ── Promtail (로그 → Loki) ────────────────────────────────────
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
  - url: http://${loki_ip}:${loki_port}/loki/api/v1/push

scrape_configs:
  # Kafka Docker 컨테이너 로그 (json-file 드라이버)
  - job_name: kafka-docker
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
          job: kafka
          host: kafka-ec2
          __path__: /var/lib/docker/containers/*/*.log

  - job_name: system
    static_configs:
      - targets:
          - localhost
        labels:
          job: system
          host: kafka-ec2
          __path__: /var/log/syslog
EOF

cat > /etc/systemd/system/promtail.service << EOF
[Unit]
Description=Promtail (log shipper to Loki)
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

echo "===== [kafka-ec2] DONE: $(date) ====="
echo "  Kafka broker → $PRIVATE_IP:${kafka_broker_port}"
