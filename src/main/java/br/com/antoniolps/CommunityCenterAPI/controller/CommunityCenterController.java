package br.com.antoniolps.CommunityCenterAPI.controller;

import br.com.antoniolps.CommunityCenterAPI.model.CommunityCenter;
import br.com.antoniolps.CommunityCenterAPI.model.dtos.CommunityCenterRequest;
import br.com.antoniolps.CommunityCenterAPI.model.dtos.UpdateOccupancyRequest;
import br.com.antoniolps.CommunityCenterAPI.model.enums.ResourceTypeEnum;
import br.com.antoniolps.CommunityCenterAPI.service.CommunityCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController(value = "community-center")
@RequiredArgsConstructor
public class CommunityCenterController {

    private final CommunityCenterService communityCenterService;

    @PostMapping
    public void addCenter(CommunityCenterRequest dto) {
        communityCenterService.addCenter(dto);
    }

    @PutMapping("/{id}/occupancy")
    public void updateOccupancy(@PathVariable("id") String id, UpdateOccupancyRequest dto) {
        communityCenterService.updateOccupancy(id, dto.getCurrentOccupancy());
    }

    @GetMapping("/highest-occupancy")
    public CommunityCenter listCenterWithHighestOccupancy() {
        return communityCenterService.listCenterWithHighestOccupancy();
    }

    @GetMapping("/occupancy-greater-than-ninety")
    public List<CommunityCenter> getCentersWithOccupancyGreaterThanNinetyPercent() {
        return communityCenterService.getCentersWithOccupancyGreaterThanNinetyPercent();
    }

    @GetMapping("/average-resources")
    public Map<ResourceTypeEnum, Double> getAverageResourcesPerType() {
        return communityCenterService.getAverageResourcesPerType();
    }

}
