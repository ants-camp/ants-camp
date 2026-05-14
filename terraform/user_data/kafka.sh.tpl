#!/bin/bash
set -euo pipefail
exec > >(tee /var/log/user-data.log | logger -t user-data -s 2>/dev/console) 2>&1

echo "===== [kafka-ec2] START: $(date) ====="

# ── 패키지 설치 ───────────────────────────────────────────────
apt-get update -y
apt-get install -y curl wget unzip jq netcat-openbsd awscli

# Java 17
apt-get install -y openjdk-17-jdk
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))

# ── Kafka 설치 (KRaft 모드) ───────────────────────────────────
KAFKA_VERSION="3.7.0"
KAFKA_DIR="/opt/kafka"

wget -q "https://downloads.apache.org/kafka/$${KAFKA_VERSION}/kafka_2.13-$${KAFKA_VERSION}.tgz" \
  -O /tmp/kafka.tgz
tar -xzf /tmp/kafka.tgz -C /opt/
ln -s /opt/kafka_2.13-$${KAFKA_VERSION} $${KAFKA_DIR}

# ── KRaft 설정 ────────────────────────────────────────────────
KAFKA_CLUSTER_ID=$($${KAFKA_DIR}/bin/kafka-storage.sh random-uuid)
NODE_ID="${node_id}"
PRIVATE_IP=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4)

cat > $${KAFKA_DIR}/config/kraft/server.properties <<EOF
# KRaft 모드 기본 설정
process.roles=broker,controller
node.id=$${NODE_ID}
controller.quorum.voters=$${NODE_ID}@$${PRIVATE_IP}:${kafka_controller_port}

listeners=PLAINTEXT://$${PRIVATE_IP}:${kafka_broker_port},CONTROLLER://$${PRIVATE_IP}:${kafka_controller_port}
advertised.listeners=PLAINTEXT://$${PRIVATE_IP}:${kafka_broker_port}

controller.listener.names=CONTROLLER
inter.broker.listener.name=PLAINTEXT
listener.security.protocol.map=PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT

log.dirs=/var/kafka/data
num.partitions=3
default.replication.factor=1
offsets.topic.replication.factor=1
transaction.state.log.replication.factor=1
transaction.state.log.min.isr=1

log.retention.hours=168
log.segment.bytes=1073741824
log.retention.check.interval.ms=300000
auto.create.topics.enable=true
EOF

# ── 데이터 디렉토리 & 포맷 ────────────────────────────────────
mkdir -p /var/kafka/data
$${KAFKA_DIR}/bin/kafka-storage.sh format \
  -t $${KAFKA_CLUSTER_ID} \
  -c $${KAFKA_DIR}/config/kraft/server.properties

# ── systemd 서비스 등록 ───────────────────────────────────────
cat > /etc/systemd/system/kafka.service <<EOF
[Unit]
Description=Apache Kafka (KRaft)
After=network.target

[Service]
Type=simple
User=root
Environment="JAVA_HOME=$${JAVA_HOME}"
Environment="KAFKA_HEAP_OPTS=-Xmx${kafka_heap} -Xms${kafka_heap}"
ExecStart=$${KAFKA_DIR}/bin/kafka-server-start.sh $${KAFKA_DIR}/config/kraft/server.properties
ExecStop=$${KAFKA_DIR}/bin/kafka-server-stop.sh
Restart=on-failure
RestartSec=10
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
  if nc -z localhost "${kafka_broker_port}" 2>/dev/null; then
    echo "Kafka is ready!"
    break
  fi
  echo "  attempt $i/60..."
  sleep 5
done

# ── Node Exporter 설치 (Prometheus 메트릭) ────────────────────
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

# ── Promtail 설치 (로그 → Loki) ──────────────────────────────
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
  - job_name: kafka
    static_configs:
      - targets:
          - localhost
        labels:
          job: kafka
          host: kafka-ec2
          __path__: /opt/kafka/logs/*.log

  - job_name: system
    static_configs:
      - targets:
          - localhost
        labels:
          job: system
          host: kafka-ec2
          __path__: /var/log/syslog
EOF

cat > /etc/systemd/system/promtail.service <<EOF
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
