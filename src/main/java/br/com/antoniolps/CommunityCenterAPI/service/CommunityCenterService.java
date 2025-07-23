package br.com.antoniolps.CommunityCenterAPI.service;

import br.com.antoniolps.CommunityCenterAPI.model.CommunityCenter;
import br.com.antoniolps.CommunityCenterAPI.model.dtos.CommunityCenterRequest;
import br.com.antoniolps.CommunityCenterAPI.model.dtos.UpdateOccupancyRequest;
import br.com.antoniolps.CommunityCenterAPI.model.enums.ResourceTypeEnum;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public interface CommunityCenterService {
    void addCenter(CommunityCenterRequest dto);

    void updateOccupancy(String centerId, int newOccupancy);

    List<CommunityCenter> getAllCenters();

    CommunityCenter getCenterById(String centerId);

    void deleteCenter(String centerId);

    CommunityCenter listCenterWithHighestOccupancy();

    List<CommunityCenter> getCentersWithOccupancyGreaterThanNinetyPercent();

    Map<ResourceTypeEnum, Double> getAverageResourcesPerType();

    void performMaxCapacityNotification();

    boolean isCenterFull(UUID centerId);
}
