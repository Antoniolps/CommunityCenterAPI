package br.com.antoniolps.CommunityCenterAPI.services;

import br.com.antoniolps.CommunityCenterAPI.model.Address;
import br.com.antoniolps.CommunityCenterAPI.model.CommunityCenter;
import br.com.antoniolps.CommunityCenterAPI.model.ExchangeHistory;
import br.com.antoniolps.CommunityCenterAPI.model.dtos.ExchangeRequest;
import br.com.antoniolps.CommunityCenterAPI.model.dtos.ExchangeReturn;
import br.com.antoniolps.CommunityCenterAPI.model.enums.ResourceTypeEnum;
import br.com.antoniolps.CommunityCenterAPI.repository.CommunityCenterRepository;
import br.com.antoniolps.CommunityCenterAPI.repository.ExchangeHistoryRepository;
import br.com.antoniolps.CommunityCenterAPI.service.ExchangeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeServiceImpl Tests")
class ExchangeServiceImplTest {

    @Mock
    private CommunityCenterRepository communityCenterRepository;

    @Mock
    private ExchangeHistoryRepository exchangeHistoryRepository;

    @InjectMocks
    private ExchangeServiceImpl exchangeService;

    private CommunityCenter sourceCenter;
    private CommunityCenter targetCenter;
    private ExchangeRequest validExchangeRequest;
    private UUID sourceCenterId;
    private UUID targetCenterId;

    @BeforeEach
    void setUp() {
        sourceCenterId = UUID.randomUUID();
        targetCenterId = UUID.randomUUID();

        sourceCenter = CommunityCenter.builder()
                .id(sourceCenterId)
                .name("Centro Origem")
                .address(createAddress())
                .maxCapacity(100)
                .currentOccupancy(50)
                .resourceMap(createSourceResourceMap())
                .build();

        targetCenter = CommunityCenter.builder()
                .id(targetCenterId)
                .name("Centro Destino")
                .address(createAddress())
                .maxCapacity(120)
                .currentOccupancy(60)
                .resourceMap(createTargetResourceMap())
                .build();

        validExchangeRequest = new ExchangeRequest();
        validExchangeRequest.setSourceCenterId(sourceCenterId.toString());
        validExchangeRequest.setTargetCenterId(targetCenterId.toString());
        validExchangeRequest.setResourcesGiven(createResourcesGiven());
        validExchangeRequest.setResourcesRequested(createResourcesRequested());
    }

    private Address createAddress() {
        Address address = new Address();
        address.setStreet("Rua Teste, 123");
        address.setCity("João Pessoa");
        address.setState("PB");
        address.setZipCode("58000-000");
        address.setLatitude(-7.1195);
        address.setLongitude(-34.8450);
        return address;
    }

    private Map<ResourceTypeEnum, Integer> createSourceResourceMap() {
        Map<ResourceTypeEnum, Integer> resources = new HashMap<>();
        resources.put(ResourceTypeEnum.VOLUNTARIO, 15);
        resources.put(ResourceTypeEnum.KIT_MEDICO, 8);
        resources.put(ResourceTypeEnum.MEDICO, 5);
        resources.put(ResourceTypeEnum.CESTA_BASICA, 20);
        return resources;
    }

    private Map<ResourceTypeEnum, Integer> createTargetResourceMap() {
        Map<ResourceTypeEnum, Integer> resources = new HashMap<>();
        resources.put(ResourceTypeEnum.VEICULO, 3);
        resources.put(ResourceTypeEnum.MEDICO, 8);
        resources.put(ResourceTypeEnum.VOLUNTARIO, 10);
        resources.put(ResourceTypeEnum.CESTA_BASICA, 15);
        return resources;
    }

    private Map<ResourceTypeEnum, Integer> createResourcesGiven() {
        Map<ResourceTypeEnum, Integer> resources = new HashMap<>();
        resources.put(ResourceTypeEnum.VOLUNTARIO, 2);
        return resources;
    }

    private Map<ResourceTypeEnum, Integer> createResourcesRequested() {
        Map<ResourceTypeEnum, Integer> resources = new HashMap<>();
        resources.put(ResourceTypeEnum.CESTA_BASICA, 3);
        return resources;
    }

    @Test
    @DisplayName("Deve realizar troca de recursos com sucesso quando pontos são equilibrados")
    void shouldExchangeResourcesSuccessfullyWhenPointsAreBalanced() {
        when(communityCenterRepository.findById(sourceCenterId)).thenReturn(Optional.of(sourceCenter));
        when(communityCenterRepository.findById(targetCenterId)).thenReturn(Optional.of(targetCenter));
        when(exchangeHistoryRepository.save(any(ExchangeHistory.class))).thenReturn(any());

        assertDoesNotThrow(() -> exchangeService.exchangeResources(validExchangeRequest));

        verify(communityCenterRepository, times(1)).findById(sourceCenterId);
        verify(communityCenterRepository, times(1)).findById(targetCenterId);
        verify(communityCenterRepository, times(2)).save(any(CommunityCenter.class));
        verify(exchangeHistoryRepository, times(1)).save(any(ExchangeHistory.class));

        assertEquals(13, sourceCenter.getResourceMap().get(ResourceTypeEnum.VOLUNTARIO));
        assertEquals(23, sourceCenter.getResourceMap().get(ResourceTypeEnum.CESTA_BASICA));

        assertEquals(12, targetCenter.getResourceMap().get(ResourceTypeEnum.CESTA_BASICA));
        assertEquals(12, targetCenter.getResourceMap().get(ResourceTypeEnum.VOLUNTARIO));
    }

    @Test
    @DisplayName("Deve lançar exceção quando centro de origem não for encontrado")
    void shouldThrowExceptionWhenSourceCenterNotFound() {
        when(communityCenterRepository.findById(sourceCenterId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> exchangeService.exchangeResources(validExchangeRequest)
        );

        assertEquals("Centro de origem não encontrado.", exception.getMessage());
        verify(communityCenterRepository, never()).save(any(CommunityCenter.class));
        verify(exchangeHistoryRepository, never()).save(any(ExchangeHistory.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando centro de destino não for encontrado")
    void shouldThrowExceptionWhenTargetCenterNotFound() {
        when(communityCenterRepository.findById(sourceCenterId)).thenReturn(Optional.of(sourceCenter));
        when(communityCenterRepository.findById(targetCenterId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> exchangeService.exchangeResources(validExchangeRequest)
        );

        assertEquals("Centro de destino não encontrado.", exception.getMessage());
        verify(communityCenterRepository, never()).save(any(CommunityCenter.class));
        verify(exchangeHistoryRepository, never()).save(any(ExchangeHistory.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando pontos da troca não são equilibrados")
    void shouldThrowExceptionWhenExchangePointsAreNotBalanced() {
        Map<ResourceTypeEnum, Integer> unbalancedResourcesRequested = new HashMap<>();
        unbalancedResourcesRequested.put(ResourceTypeEnum.MEDICO, 2);

        validExchangeRequest.setResourcesRequested(unbalancedResourcesRequested);

        when(communityCenterRepository.findById(sourceCenterId)).thenReturn(Optional.of(sourceCenter));
        when(communityCenterRepository.findById(targetCenterId)).thenReturn(Optional.of(targetCenter));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> exchangeService.exchangeResources(validExchangeRequest)
        );

        assertEquals("A troca deve ser equilibrada em pontos.", exception.getMessage());
        verify(communityCenterRepository, never()).save(any(CommunityCenter.class));
        verify(exchangeHistoryRepository, never()).save(any(ExchangeHistory.class));
    }

    @Test
    @DisplayName("Deve permitir troca desequilibrada quando centro origem está com ocupação crítica (>90%)")
    void shouldAllowUnbalancedExchangeWhenSourceCenterHasCriticalOccupancy() {
        sourceCenter.setCurrentOccupancy(95);

        Map<ResourceTypeEnum, Integer> unbalancedResourcesRequested = new HashMap<>();
        unbalancedResourcesRequested.put(ResourceTypeEnum.MEDICO, 2);

        validExchangeRequest.setResourcesRequested(unbalancedResourcesRequested);

        when(communityCenterRepository.findById(sourceCenterId)).thenReturn(Optional.of(sourceCenter));
        when(communityCenterRepository.findById(targetCenterId)).thenReturn(Optional.of(targetCenter));
        when(exchangeHistoryRepository.save(any(ExchangeHistory.class))).thenReturn(any());

        assertDoesNotThrow(() -> exchangeService.exchangeResources(validExchangeRequest));

        verify(communityCenterRepository, times(2)).save(any(CommunityCenter.class));
        verify(exchangeHistoryRepository, times(1)).save(any(ExchangeHistory.class));
    }

    @Test
    @DisplayName("Deve calcular pontos corretamente baseado no enum ResourceTypeEnum")
    void shouldCalculatePointsCorrectlyBasedOnResourceTypeEnum() {
        Map<ResourceTypeEnum, Integer> resources = Map.of(
                ResourceTypeEnum.MEDICO, 2,
                ResourceTypeEnum.VOLUNTARIO, 3,
                ResourceTypeEnum.KIT_MEDICO, 1,
                ResourceTypeEnum.VEICULO, 2,
                ResourceTypeEnum.CESTA_BASICA, 5
        );

        validExchangeRequest.setResourcesGiven(resources);

        Map<ResourceTypeEnum, Integer> balancedResources = Map.of(
                ResourceTypeEnum.CESTA_BASICA, 22
        );

        validExchangeRequest.setResourcesRequested(balancedResources);

        when(communityCenterRepository.findById(sourceCenterId)).thenReturn(Optional.of(sourceCenter));
        when(communityCenterRepository.findById(targetCenterId)).thenReturn(Optional.of(targetCenter));
        when(exchangeHistoryRepository.save(any(ExchangeHistory.class))).thenReturn(any());

        assertDoesNotThrow(() -> exchangeService.exchangeResources(validExchangeRequest));

        verify(communityCenterRepository, times(2)).save(any(CommunityCenter.class));
    }

    @Test
    @DisplayName("Deve registrar corretamente no histórico de trocas")
    void shouldCorrectlyRegisterInExchangeHistory() {
        when(communityCenterRepository.findById(sourceCenterId)).thenReturn(Optional.of(sourceCenter));
        when(communityCenterRepository.findById(targetCenterId)).thenReturn(Optional.of(targetCenter));

        ArgumentCaptor<ExchangeHistory> historyCaptor = ArgumentCaptor.forClass(ExchangeHistory.class);
        when(exchangeHistoryRepository.save(historyCaptor.capture())).thenReturn(any());

        exchangeService.exchangeResources(validExchangeRequest);

        ExchangeHistory savedHistory = historyCaptor.getValue();

        assertNotNull(savedHistory.getId());
        assertEquals(sourceCenterId.toString(), savedHistory.getSourceCenterId());
        assertEquals(targetCenterId.toString(), savedHistory.getTargetCenterId());
        assertEquals(validExchangeRequest.getResourcesGiven(), savedHistory.getResourcesGiven());
        assertEquals(validExchangeRequest.getResourcesRequested(), savedHistory.getResourcesReceived());
        assertNotNull(savedHistory.getTimestamp());
        assertTrue(savedHistory.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(savedHistory.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(10)));
    }

    @Test
    @DisplayName("Deve buscar todas as trocas de um centro após uma data específica")
    void shouldGetAllExchangesByCenterAfterSpecificDate() {
        String centerId = sourceCenterId.toString();
        String dateString = "2024-01-01T00:00:00";
        LocalDateTime fromDate = LocalDateTime.parse(dateString);

        ExchangeHistory history1 = createExchangeHistory(centerId, targetCenterId.toString());
        ExchangeHistory history2 = createExchangeHistory(targetCenterId.toString(), centerId);

        List<ExchangeHistory> mockHistories = Arrays.asList(history1, history2);

        when(exchangeHistoryRepository.findBySourceCenterIdOrTargetCenterIdAndTimestampAfter(
                eq(centerId), eq(centerId), eq(fromDate)
        )).thenReturn(mockHistories);

        List<ExchangeReturn> result = exchangeService.getAllExchangesByCenterAfterDate(centerId, dateString);

        assertEquals(2, result.size());

        ExchangeReturn firstExchange = result.get(0);
        assertEquals(history1.getSourceCenterId(), firstExchange.getSourceCenterId());
        assertEquals(history1.getTargetCenterId(), firstExchange.getTargetCenterId());
        assertEquals(history1.getResourcesGiven(), firstExchange.getResourcesGiven());
        assertEquals(history1.getResourcesReceived(), firstExchange.getResourcesReceived());

        verify(exchangeHistoryRepository, times(1))
                .findBySourceCenterIdOrTargetCenterIdAndTimestampAfter(centerId, centerId, fromDate);
    }

    @Test
    @DisplayName("Deve buscar todas as trocas de um centro específico")
    void shouldGetAllExchangesBySpecificCenter() {
        String centerId = sourceCenterId.toString();

        ExchangeHistory history1 = createExchangeHistory(centerId, targetCenterId.toString());
        ExchangeHistory history2 = createExchangeHistory(targetCenterId.toString(), centerId);
        ExchangeHistory history3 = createExchangeHistory(centerId, UUID.randomUUID().toString());

        List<ExchangeHistory> mockHistories = Arrays.asList(history1, history2, history3);

        when(exchangeHistoryRepository.findBySourceCenterIdOrTargetCenterId(eq(centerId), eq(centerId)))
                .thenReturn(mockHistories);

        List<ExchangeReturn> result = exchangeService.getAllByCenterId(centerId);

        assertEquals(3, result.size());

        result.forEach(exchange -> {
            assertTrue(
                    exchange.getSourceCenterId().equals(centerId) ||
                            exchange.getTargetCenterId().equals(centerId)
            );
        });

        verify(exchangeHistoryRepository, times(1))
                .findBySourceCenterIdOrTargetCenterId(centerId, centerId);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há trocas para o centro")
    void shouldReturnEmptyListWhenNoCenterExchanges() {
        String centerId = sourceCenterId.toString();
        when(exchangeHistoryRepository.findBySourceCenterIdOrTargetCenterId(eq(centerId), eq(centerId)))
                .thenReturn(Collections.emptyList());

        List<ExchangeReturn> result = exchangeService.getAllByCenterId(centerId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve atualizar recursos corretamente quando centro não possui o tipo de recurso")
    void shouldUpdateResourcesCorrectlyWhenCenterDoesNotHaveResourceType() {
        sourceCenter.getResourceMap().remove(ResourceTypeEnum.CESTA_BASICA);

        when(communityCenterRepository.findById(sourceCenterId)).thenReturn(Optional.of(sourceCenter));
        when(communityCenterRepository.findById(targetCenterId)).thenReturn(Optional.of(targetCenter));
        when(exchangeHistoryRepository.save(any(ExchangeHistory.class))).thenReturn(any());

        exchangeService.exchangeResources(validExchangeRequest);

        assertEquals(3, sourceCenter.getResourceMap().get(ResourceTypeEnum.CESTA_BASICA));
        verify(communityCenterRepository, times(2)).save(any(CommunityCenter.class));
    }

    @Test
    @DisplayName("Deve validar que recursos não podem ficar negativos após troca")
    void shouldValidateThatResourcesCannotBeNegativeAfterExchange() {
        sourceCenter.getResourceMap().put(ResourceTypeEnum.VOLUNTARIO, 1);

        when(communityCenterRepository.findById(sourceCenterId)).thenReturn(Optional.of(sourceCenter));
        when(communityCenterRepository.findById(targetCenterId)).thenReturn(Optional.of(targetCenter));
        when(exchangeHistoryRepository.save(any(ExchangeHistory.class))).thenReturn(any());

        exchangeService.exchangeResources(validExchangeRequest);

        assertEquals(-1, sourceCenter.getResourceMap().get(ResourceTypeEnum.VOLUNTARIO));
        verify(communityCenterRepository, times(2)).save(any(CommunityCenter.class));
    }

    private ExchangeHistory createExchangeHistory(String sourceCenterId, String targetCenterId) {
        return ExchangeHistory.builder()
                .id(UUID.randomUUID())
                .sourceCenterId(sourceCenterId)
                .targetCenterId(targetCenterId)
                .resourcesGiven(Map.of(ResourceTypeEnum.VOLUNTARIO, 2))
                .resourcesReceived(Map.of(ResourceTypeEnum.CESTA_BASICA, 3))
                .timestamp(LocalDateTime.now())
                .build();
    }
}