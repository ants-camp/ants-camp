# ============================================================
# Security Group Rules (cross-SG 참조)
# aws_security_group 내 인라인 블록에서 순환 참조 발생하므로
# 별도 aws_security_group_rule 리소스로 분리
# ============================================================

# ──────────────────────────────────────────────────────────────
# KAFKA
# ──────────────────────────────────────────────────────────────

resource "aws_security_group_rule" "kafka_ingress_from_infra" {
  type                     = "ingress"
  description              = "Kafka broker from infra-ec2"
  security_group_id        = aws_security_group.kafka.id
  source_security_group_id = aws_security_group.infra.id
  from_port                = var.ports["kafka_broker"]
  to_port                  = var.ports["kafka_broker"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "kafka_ingress_from_domain" {
  type                     = "ingress"
  description              = "Kafka broker from domain-ec2"
  security_group_id        = aws_security_group.kafka.id
  source_security_group_id = aws_security_group.domain.id
  from_port                = var.ports["kafka_broker"]
  to_port                  = var.ports["kafka_broker"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "kafka_ingress_from_domain2" {
  type                     = "ingress"
  description              = "Kafka broker from domain2-ec2"
  security_group_id        = aws_security_group.kafka.id
  source_security_group_id = aws_security_group.domain2.id
  from_port                = var.ports["kafka_broker"]
  to_port                  = var.ports["kafka_broker"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "kafka_ingress_from_notification" {
  type                     = "ingress"
  description              = "Kafka broker from notification-ec2"
  security_group_id        = aws_security_group.kafka.id
  source_security_group_id = aws_security_group.notification.id
  from_port                = var.ports["kafka_broker"]
  to_port                  = var.ports["kafka_broker"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "kafka_ingress_ssh_from_gateway" {
  type                     = "ingress"
  description              = "SSH from bastion"
  security_group_id        = aws_security_group.kafka.id
  source_security_group_id = aws_security_group.gateway.id
  from_port                = 22
  to_port                  = 22
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "kafka_ingress_node_exporter_from_monitoring" {
  type                     = "ingress"
  description              = "Node exporter scrape from monitoring"
  security_group_id        = aws_security_group.kafka.id
  source_security_group_id = aws_security_group.monitoring.id
  from_port                = var.ports["node_exporter"]
  to_port                  = var.ports["node_exporter"]
  protocol                 = "tcp"
}

# ──────────────────────────────────────────────────────────────
# INFRA (Config Server + Eureka + Kafka UI)
# ──────────────────────────────────────────────────────────────

# Config Server inbound from app EC2s
resource "aws_security_group_rule" "infra_ingress_config_from_domain" {
  type                     = "ingress"
  description              = "Config Server from domain-ec2"
  security_group_id        = aws_security_group.infra.id
  source_security_group_id = aws_security_group.domain.id
  from_port                = var.ports["config_server"]
  to_port                  = var.ports["config_server"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "infra_ingress_config_from_domain2" {
  type                     = "ingress"
  description              = "Config Server from domain2-ec2"
  security_group_id        = aws_security_group.infra.id
  source_security_group_id = aws_security_group.domain2.id
  from_port                = var.ports["config_server"]
  to_port                  = var.ports["config_server"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "infra_ingress_config_from_notification" {
  type                     = "ingress"
  description              = "Config Server from notification-ec2"
  security_group_id        = aws_security_group.infra.id
  source_security_group_id = aws_security_group.notification.id
  from_port                = var.ports["config_server"]
  to_port                  = var.ports["config_server"]
  protocol                 = "tcp"
}

# Eureka inbound from app EC2s + gateway
resource "aws_security_group_rule" "infra_ingress_eureka_from_domain" {
  type                     = "ingress"
  description              = "Eureka from domain-ec2"
  security_group_id        = aws_security_group.infra.id
  source_security_group_id = aws_security_group.domain.id
  from_port                = var.ports["eureka"]
  to_port                  = var.ports["eureka"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "infra_ingress_eureka_from_domain2" {
  type                     = "ingress"
  description              = "Eureka from domain2-ec2"
  security_group_id        = aws_security_group.infra.id
  source_security_group_id = aws_security_group.domain2.id
  from_port                = var.ports["eureka"]
  to_port                  = var.ports["eureka"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "infra_ingress_eureka_from_notification" {
  type                     = "ingress"
  description              = "Eureka from notification-ec2"
  security_group_id        = aws_security_group.infra.id
  source_security_group_id = aws_security_group.notification.id
  from_port                = var.ports["eureka"]
  to_port                  = var.ports["eureka"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "infra_ingress_eureka_from_gateway" {
  type                     = "ingress"
  description              = "Eureka dashboard from gateway (nginx)"
  security_group_id        = aws_security_group.infra.id
  source_security_group_id = aws_security_group.gateway.id
  from_port                = var.ports["eureka"]
  to_port                  = var.ports["eureka"]
  protocol                 = "tcp"
}

# Kafka UI inbound from gateway only
resource "aws_security_group_rule" "infra_ingress_kafka_ui_from_gateway" {
  type                     = "ingress"
  description              = "Kafka UI from gateway"
  security_group_id        = aws_security_group.infra.id
  source_security_group_id = aws_security_group.gateway.id
  from_port                = var.ports["kafka_ui"]
  to_port                  = var.ports["kafka_ui"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "infra_ingress_ssh_from_gateway" {
  type                     = "ingress"
  description              = "SSH from bastion"
  security_group_id        = aws_security_group.infra.id
  source_security_group_id = aws_security_group.gateway.id
  from_port                = 22
  to_port                  = 22
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "infra_ingress_node_exporter_from_monitoring" {
  type                     = "ingress"
  description              = "Node exporter from monitoring"
  security_group_id        = aws_security_group.infra.id
  source_security_group_id = aws_security_group.monitoring.id
  from_port                = var.ports["node_exporter"]
  to_port                  = var.ports["node_exporter"]
  protocol                 = "tcp"
}

# ──────────────────────────────────────────────────────────────
# DOMAIN (user, asset, ranking)
# ──────────────────────────────────────────────────────────────

resource "aws_security_group_rule" "domain_ingress_user_from_gateway" {
  type                     = "ingress"
  description              = "user-service from gateway"
  security_group_id        = aws_security_group.domain.id
  source_security_group_id = aws_security_group.gateway.id
  from_port                = var.ports["user"]
  to_port                  = var.ports["user"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "domain_ingress_asset_from_gateway" {
  type                     = "ingress"
  description              = "asset-service from gateway"
  security_group_id        = aws_security_group.domain.id
  source_security_group_id = aws_security_group.gateway.id
  from_port                = var.ports["asset"]
  to_port                  = var.ports["asset"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "domain_ingress_ranking_from_gateway" {
  type                     = "ingress"
  description              = "ranking-service from gateway"
  security_group_id        = aws_security_group.domain.id
  source_security_group_id = aws_security_group.gateway.id
  from_port                = var.ports["ranking"]
  to_port                  = var.ports["ranking"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "domain_ingress_ssh_from_gateway" {
  type                     = "ingress"
  description              = "SSH from bastion"
  security_group_id        = aws_security_group.domain.id
  source_security_group_id = aws_security_group.gateway.id
  from_port                = 22
  to_port                  = 22
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "domain_ingress_node_exporter_from_monitoring" {
  type                     = "ingress"
  description              = "Node exporter from monitoring"
  security_group_id        = aws_security_group.domain.id
  source_security_group_id = aws_security_group.monitoring.id
  from_port                = var.ports["node_exporter"]
  to_port                  = var.ports["node_exporter"]
  protocol                 = "tcp"
}

# ──────────────────────────────────────────────────────────────
# DOMAIN2 (trade, competition)
# ──────────────────────────────────────────────────────────────

resource "aws_security_group_rule" "domain2_ingress_trade_from_gateway" {
  type                     = "ingress"
  description              = "trade-service from gateway"
  security_group_id        = aws_security_group.domain2.id
  source_security_group_id = aws_security_group.gateway.id
  from_port                = var.ports["trade"]
  to_port                  = var.ports["trade"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "domain2_ingress_competition_from_gateway" {
  type                     = "ingress"
  description              = "competition-service from gateway"
  security_group_id        = aws_security_group.domain2.id
  source_security_group_id = aws_security_group.gateway.id
  from_port                = var.ports["competition"]
  to_port                  = var.ports["competition"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "domain2_ingress_ssh_from_gateway" {
  type                     = "ingress"
  description              = "SSH from bastion"
  security_group_id        = aws_security_group.domain2.id
  source_security_group_id = aws_security_group.gateway.id
  from_port                = 22
  to_port                  = 22
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "domain2_ingress_node_exporter_from_monitoring" {
  type                     = "ingress"
  description              = "Node exporter from monitoring"
  security_group_id        = aws_security_group.domain2.id
  source_security_group_id = aws_security_group.monitoring.id
  from_port                = var.ports["node_exporter"]
  to_port                  = var.ports["node_exporter"]
  protocol                 = "tcp"
}

# ──────────────────────────────────────────────────────────────
# NOTIFICATION
# ──────────────────────────────────────────────────────────────

resource "aws_security_group_rule" "notification_ingress_notification_from_gateway" {
  type                     = "ingress"
  description              = "notification-service from gateway"
  security_group_id        = aws_security_group.notification.id
  source_security_group_id = aws_security_group.gateway.id
  from_port                = var.ports["notification"]
  to_port                  = var.ports["notification"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "notification_ingress_assistant_from_gateway" {
  type                     = "ingress"
  description              = "assistant-service from gateway"
  security_group_id        = aws_security_group.notification.id
  source_security_group_id = aws_security_group.gateway.id
  from_port                = var.ports["assistant"]
  to_port                  = var.ports["assistant"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "notification_ingress_ssh_from_gateway" {
  type                     = "ingress"
  description              = "SSH from bastion"
  security_group_id        = aws_security_group.notification.id
  source_security_group_id = aws_security_group.gateway.id
  from_port                = 22
  to_port                  = 22
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "notification_ingress_node_exporter_from_monitoring" {
  type                     = "ingress"
  description              = "Node exporter from monitoring"
  security_group_id        = aws_security_group.notification.id
  source_security_group_id = aws_security_group.monitoring.id
  from_port                = var.ports["node_exporter"]
  to_port                  = var.ports["node_exporter"]
  protocol                 = "tcp"
}

# ──────────────────────────────────────────────────────────────
# MONITORING (Prometheus, Grafana, Loki)
# ──────────────────────────────────────────────────────────────

resource "aws_security_group_rule" "monitoring_ingress_ssh_from_gateway" {
  type                     = "ingress"
  description              = "SSH from bastion"
  security_group_id        = aws_security_group.monitoring.id
  source_security_group_id = aws_security_group.gateway.id
  from_port                = 22
  to_port                  = 22
  protocol                 = "tcp"
}

# Loki (Promtail push) from all app EC2s
resource "aws_security_group_rule" "monitoring_ingress_loki_from_kafka" {
  type                     = "ingress"
  description              = "Loki from kafka-ec2 (Promtail)"
  security_group_id        = aws_security_group.monitoring.id
  source_security_group_id = aws_security_group.kafka.id
  from_port                = var.ports["loki"]
  to_port                  = var.ports["loki"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "monitoring_ingress_loki_from_infra" {
  type                     = "ingress"
  description              = "Loki from infra-ec2 (Promtail)"
  security_group_id        = aws_security_group.monitoring.id
  source_security_group_id = aws_security_group.infra.id
  from_port                = var.ports["loki"]
  to_port                  = var.ports["loki"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "monitoring_ingress_loki_from_domain" {
  type                     = "ingress"
  description              = "Loki from domain-ec2 (Promtail)"
  security_group_id        = aws_security_group.monitoring.id
  source_security_group_id = aws_security_group.domain.id
  from_port                = var.ports["loki"]
  to_port                  = var.ports["loki"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "monitoring_ingress_loki_from_domain2" {
  type                     = "ingress"
  description              = "Loki from domain2-ec2 (Promtail)"
  security_group_id        = aws_security_group.monitoring.id
  source_security_group_id = aws_security_group.domain2.id
  from_port                = var.ports["loki"]
  to_port                  = var.ports["loki"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "monitoring_ingress_loki_from_notification" {
  type                     = "ingress"
  description              = "Loki from notification-ec2 (Promtail)"
  security_group_id        = aws_security_group.monitoring.id
  source_security_group_id = aws_security_group.notification.id
  from_port                = var.ports["loki"]
  to_port                  = var.ports["loki"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "monitoring_ingress_loki_from_db" {
  type                     = "ingress"
  description              = "Loki from db-ec2 (Promtail)"
  security_group_id        = aws_security_group.monitoring.id
  source_security_group_id = aws_security_group.db.id
  from_port                = var.ports["loki"]
  to_port                  = var.ports["loki"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "monitoring_ingress_loki_from_gateway" {
  type                     = "ingress"
  description              = "Loki from gateway-ec2 (Promtail)"
  security_group_id        = aws_security_group.monitoring.id
  source_security_group_id = aws_security_group.gateway.id
  from_port                = var.ports["loki"]
  to_port                  = var.ports["loki"]
  protocol                 = "tcp"
}

# ──────────────────────────────────────────────────────────────
# DB (Redis only) - PostgreSQL은 Aurora RDS로 이관
# ──────────────────────────────────────────────────────────────

# Redis
resource "aws_security_group_rule" "db_ingress_redis_from_domain" {
  type                     = "ingress"
  description              = "Redis from domain-ec2"
  security_group_id        = aws_security_group.db.id
  source_security_group_id = aws_security_group.domain.id
  from_port                = var.ports["redis"]
  to_port                  = var.ports["redis"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "db_ingress_redis_from_domain2" {
  type                     = "ingress"
  description              = "Redis from domain2-ec2"
  security_group_id        = aws_security_group.db.id
  source_security_group_id = aws_security_group.domain2.id
  from_port                = var.ports["redis"]
  to_port                  = var.ports["redis"]
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "db_ingress_redis_from_notification" {
  type                     = "ingress"
  description              = "Redis from notification-ec2"
  security_group_id        = aws_security_group.db.id
  source_security_group_id = aws_security_group.notification.id
  from_port                = var.ports["redis"]
  to_port                  = var.ports["redis"]
  protocol                 = "tcp"
}

# SSH from gateway
resource "aws_security_group_rule" "db_ingress_ssh_from_gateway" {
  type                     = "ingress"
  description              = "SSH from bastion"
  security_group_id        = aws_security_group.db.id
  source_security_group_id = aws_security_group.gateway.id
  from_port                = 22
  to_port                  = 22
  protocol                 = "tcp"
}

# Node exporter from monitoring
resource "aws_security_group_rule" "db_ingress_node_exporter_from_monitoring" {
  type                     = "ingress"
  description              = "Node exporter from monitoring"
  security_group_id        = aws_security_group.db.id
  source_security_group_id = aws_security_group.monitoring.id
  from_port                = var.ports["node_exporter"]
  to_port                  = var.ports["node_exporter"]
  protocol                 = "tcp"
}
