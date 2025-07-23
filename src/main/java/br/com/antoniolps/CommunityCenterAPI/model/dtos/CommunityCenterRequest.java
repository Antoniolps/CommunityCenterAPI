package br.com.antoniolps.CommunityCenterAPI.model.dtos;

import br.com.antoniolps.CommunityCenterAPI.model.Address;
import br.com.antoniolps.CommunityCenterAPI.model.enums.ResourceTypeEnum;
import lombok.Data;

import java.util.Map;

@Data
public class CommunityCenterRequest {
    private String name;
    private Address address;
    private int maxCapacity;
    private int currentOccupancy;
    private Map<ResourceTypeEnum, Integer> resources;
}
