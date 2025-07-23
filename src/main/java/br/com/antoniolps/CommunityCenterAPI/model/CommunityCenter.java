package br.com.antoniolps.CommunityCenterAPI.model;

import br.com.antoniolps.CommunityCenterAPI.model.enums.ResourceTypeEnum;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;
import java.util.UUID;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityCenter {
    @Id
    private UUID id;
    private String name;
    private Address address;
    private int maxCapacity;
    private int currentOccupancy;
    private Map<ResourceTypeEnum, Integer> resourceMap;
}
