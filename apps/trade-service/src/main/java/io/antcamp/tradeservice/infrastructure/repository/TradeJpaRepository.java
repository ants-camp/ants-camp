package io.antcamp.tradeservice.infrastructure.repository;

import io.antcamp.tradeservice.infrastructure.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TradeJpaRepository extends JpaRepository<TradeEntity, UUID> {

}
