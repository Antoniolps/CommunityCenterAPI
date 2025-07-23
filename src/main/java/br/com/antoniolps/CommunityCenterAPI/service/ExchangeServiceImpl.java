package br.com.antoniolps.CommunityCenterAPI.service;

import br.com.antoniolps.CommunityCenterAPI.model.CommunityCenter;
import br.com.antoniolps.CommunityCenterAPI.model.ExchangeHistory;
import br.com.antoniolps.CommunityCenterAPI.model.dtos.ExchangeRequest;
import br.com.antoniolps.CommunityCenterAPI.model.dtos.ExchangeReturn;
import br.com.antoniolps.CommunityCenterAPI.model.enums.ResourceTypeEnum;
import br.com.antoniolps.CommunityCenterAPI.repository.CommunityCenterRepository;
import br.com.antoniolps.CommunityCenterAPI.repository.ExchangeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExchangeServiceImpl implements ExchangeService {

    private final CommunityCenterRepository communityCenterRepository;
    private final ExchangeHistoryRepository exchangeHistoryRepository;

    @Override
    public void exchangeResources(ExchangeRequest dto) {

        CommunityCenter source = communityCenterRepository.findById(UUID.fromString(dto.getSourceCenterId()))
                .orElseThrow(() -> new IllegalArgumentException("Centro de origem não encontrado."));
        CommunityCenter target = communityCenterRepository.findById(UUID.fromString(dto.getTargetCenterId()))
                .orElseThrow(() -> new IllegalArgumentException("Centro de destino não encontrado."));

        validateExchangePoints(source, target, dto.getResourcesGiven(), dto.getResourcesRequested());
        updateResources(source, dto.getResourcesGiven(), false);
        updateResources(target, dto.getResourcesRequested(), true);

        updateResources(target, dto.getResourcesGiven(), false);
        updateResources(source, dto.getResourcesRequested(), true);

        communityCenterRepository.save(source);
        communityCenterRepository.save(target);

        saveExchangeHistory(dto.getSourceCenterId(), dto.getTargetCenterId(), dto.getResourcesGiven(), dto.getResourcesRequested());
    }

    @Override
    public List<ExchangeReturn> getAllExchangesByCenterAfterDate(String centerId, String date) {
        LocalDateTime fromDate = LocalDateTime.parse(date);
        List<ExchangeHistory> histories = exchangeHistoryRepository.findBySourceCenterIdOrTargetCenterIdAndTimestampAfter(
                centerId, centerId, fromDate
        );

        return histories.stream().map(history -> ExchangeReturn.builder()
                .sourceCenterId(history.getSourceCenterId())
                .targetCenterId(history.getTargetCenterId())
                .resourcesGiven(history.getResourcesGiven())
                .resourcesReceived(history.getResourcesReceived())
                .build()).toList();
    }

    @Override
    public List<ExchangeReturn> getAllByCenterId(String centerId) {
        return exchangeHistoryRepository.findBySourceCenterIdOrTargetCenterId(centerId, centerId)
                .stream()
                .map(history -> ExchangeReturn.builder()
                        .sourceCenterId(history.getSourceCenterId())
                        .targetCenterId(history.getTargetCenterId())
                        .resourcesGiven(history.getResourcesGiven())
                        .resourcesReceived(history.getResourcesReceived())
                        .build())
                .toList();
    }


    private void validateExchangePoints(CommunityCenter source, CommunityCenter target,
                                        Map<ResourceTypeEnum, Integer> given,
                                        Map<ResourceTypeEnum, Integer> requested) {
        int pointsGiven = calculatePoints(given);
        int pointsRequested = calculatePoints(requested);

        if (source.getCurrentOccupancy() > (source.getMaxCapacity() * 0.9)) {
            return;
        }
        if (pointsGiven != pointsRequested) {
            throw new IllegalArgumentException("A troca deve ser equilibrada em pontos.");
        }
    }

    private int calculatePoints(Map<ResourceTypeEnum, Integer> resources) {
        return resources.entrySet().stream()
                .mapToInt(e -> e.getKey().getPoints() * e.getValue())
                .sum();
    }

    private void updateResources(CommunityCenter center, Map<ResourceTypeEnum, Integer> resources, boolean add) {
        resources.forEach((type, qty) -> {
            center.getResourceMap().putIfAbsent(type, 0);
            center.getResourceMap().compute(type, (k, current) -> add ? current + qty : current - qty);
        });
    }

    private void saveExchangeHistory(String sourceCenterId, String targetCenterId,
                                     Map<ResourceTypeEnum, Integer> resourcesGiven,
                                     Map<ResourceTypeEnum, Integer> resourcesRequested) {
        ExchangeHistory history = ExchangeHistory.builder()
                .id(UUID.randomUUID())
                .sourceCenterId(sourceCenterId)
                .targetCenterId(targetCenterId)
                .resourcesGiven(resourcesGiven)
                .resourcesReceived(resourcesRequested)
                .timestamp(LocalDateTime.now())
                .build();
        exchangeHistoryRepository.save(history);
    }
}

