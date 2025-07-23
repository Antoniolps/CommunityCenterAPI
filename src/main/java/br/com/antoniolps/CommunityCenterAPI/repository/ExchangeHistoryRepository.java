package br.com.antoniolps.CommunityCenterAPI.repository;

import br.com.antoniolps.CommunityCenterAPI.model.ExchangeHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExchangeHistoryRepository extends MongoRepository<ExchangeHistory, UUID> {

    List<ExchangeHistory> findBySourceCenterIdOrTargetCenterIdAndTimestampAfter(
            String sourceId, String targetId, LocalDateTime fromDate
    );

    List<ExchangeHistory> findBySourceCenterIdOrTargetCenterId(String sourceId, String targetId);
}
