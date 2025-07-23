package br.com.antoniolps.CommunityCenterAPI.controller;

import br.com.antoniolps.CommunityCenterAPI.model.CommunityCenter;
import br.com.antoniolps.CommunityCenterAPI.model.dtos.CommunityCenterRequest;
import br.com.antoniolps.CommunityCenterAPI.model.dtos.UpdateOccupancyRequest;
import br.com.antoniolps.CommunityCenterAPI.model.enums.ResourceTypeEnum;
import br.com.antoniolps.CommunityCenterAPI.service.CommunityCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/community-centers")
@RequiredArgsConstructor
public class CommunityCenterController {

    private final CommunityCenterService communityCenterService;

    @PostMapping
    public ResponseEntity<Void> addCenter(CommunityCenterRequest dto) {
        communityCenterService.addCenter(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}/occupancy")
    public ResponseEntity<Void> updateOccupancy(@PathVariable("id") String id, UpdateOccupancyRequest dto) {
        communityCenterService.updateOccupancy(id, dto.getCurrentOccupancy());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/highest-occupancy")
    public ResponseEntity<CommunityCenter> listCenterWithHighestOccupancy() {
        return ResponseEntity.ok(communityCenterService.listCenterWithHighestOccupancy());
    }

    @GetMapping("/occupancy-greater-than-ninety")
    public ResponseEntity<List<CommunityCenter>> getCentersWithOccupancyGreaterThanNinetyPercent() {
        return ResponseEntity.ok(communityCenterService.getCentersWithOccupancyGreaterThanNinetyPercent());
    }

    @GetMapping("/average-resources")
    public ResponseEntity<Map<ResourceTypeEnum, Double>> getAverageResourcesPerType() {
        return ResponseEntity.ok(communityCenterService.getAverageResourcesPerType());
    }

    @GetMapping
    public ResponseEntity<Boolean> isCenterFull(@RequestParam("id") String centerId) {
        boolean isFull = communityCenterService.isCenterFull(UUID.fromString(centerId));
        return ResponseEntity.ok(isFull);
    }

}
