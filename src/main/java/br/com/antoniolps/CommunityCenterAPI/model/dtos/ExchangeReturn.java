package br.com.antoniolps.CommunityCenterAPI.model.dtos;

import br.com.antoniolps.CommunityCenterAPI.model.enums.ResourceTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ExchangeReturn {
    private String sourceCenterId;
    private String targetCenterId;
    private Map<ResourceTypeEnum, Integer> resourcesGiven;
    private Map<ResourceTypeEnum, Integer> resourcesReceived;
}
