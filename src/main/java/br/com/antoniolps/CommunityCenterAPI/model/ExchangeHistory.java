package br.com.antoniolps.CommunityCenterAPI.model;

import br.com.antoniolps.CommunityCenterAPI.model.enums.ResourceTypeEnum;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Document(collection = "exchange_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeHistory {
    @Id
    private UUID id;
    private String sourceCenterId;
    private String targetCenterId;
    private Map<ResourceTypeEnum, Integer> resourcesGiven;
    private Map<ResourceTypeEnum, Integer> resourcesReceived;
    private LocalDateTime timestamp;
}

