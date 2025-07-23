package br.com.antoniolps.CommunityCenterAPI.service;

import br.com.antoniolps.CommunityCenterAPI.model.CommunityCenter;
import br.com.antoniolps.CommunityCenterAPI.model.dtos.CommunityCenterRequest;
import br.com.antoniolps.CommunityCenterAPI.model.enums.ResourceTypeEnum;
import br.com.antoniolps.CommunityCenterAPI.repository.CommunityCenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityCenterServiceImpl implements CommunityCenterService {

    private final CommunityCenterRepository communityCenterRepository;

    @Override
    public void addCenter(CommunityCenterRequest dto){

        if (dto.getName() == null || dto.getAddress() == null || dto.getMaxCapacity() <= 0) {
            throw new IllegalArgumentException("Invalid community center request");
        }

        CommunityCenter communityCenter = new CommunityCenter();
        communityCenter.setName(dto.getName());
        communityCenter.setAddress(dto.getAddress());
        communityCenter.setMaxCapacity(dto.getMaxCapacity());
        communityCenter.setCurrentOccupancy(dto.getCurrentOccupancy());
        communityCenter.setResourceMap(dto.getResources());

        communityCenterRepository.save(communityCenter);
    }

    @Override
    public void updateOccupancy(String centerId, int newOccupancy) {
        CommunityCenter communityCenter = communityCenterRepository.findById(UUID.fromString(centerId))
                .orElseThrow(() -> new IllegalArgumentException("Community center not found"));

        if (newOccupancy < 0 || newOccupancy > communityCenter.getMaxCapacity()) {
            throw new IllegalArgumentException("Invalid occupancy value");
        }

        communityCenter.setCurrentOccupancy(newOccupancy);
        communityCenterRepository.save(communityCenter);
    }

    @Override
    public List<CommunityCenter> getAllCenters() {
        return communityCenterRepository.findAll();
    }

    @Override
    public CommunityCenter getCenterById(String centerId) {
        return communityCenterRepository.findById(UUID.fromString(centerId))
                .orElseThrow(() -> new IllegalArgumentException("Community center not found"));
    }

    @Override
    public void deleteCenter(String centerId) {
        CommunityCenter communityCenter = communityCenterRepository.findById(UUID.fromString(centerId))
                .orElseThrow(() -> new IllegalArgumentException("Community center not found"));
        communityCenterRepository.delete(communityCenter);
    }

    @Override
    public CommunityCenter listCenterWithHighestOccupancy() {
        return communityCenterRepository.findAll().stream()
                .max((c1, c2) -> Integer.compare(c1.getCurrentOccupancy(), c2.getCurrentOccupancy()))
                .orElseThrow(() -> new IllegalArgumentException("No community centers found"));
    }

    @Override
    public List<CommunityCenter> getCentersWithOccupancyGreaterThanNinetyPercent(){
        List<CommunityCenter> centers = communityCenterRepository.findByCurrentOccupancyGreaterThan(0);
        return centers.stream()
                .filter(center -> center.getCurrentOccupancy() > center.getMaxCapacity() * 0.9)
                .toList();
    }

    @Override
    public Map<ResourceTypeEnum, Double> getAverageResourcesPerType() {
        List<CommunityCenter> centers = communityCenterRepository.findAll();

        return centers.stream()
                .flatMap(center -> center.getResourceMap().entrySet().stream())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.averagingInt(Map.Entry::getValue)
                ));
    }
}
