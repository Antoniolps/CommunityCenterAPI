package br.com.antoniolps.CommunityCenterAPI.services;

import br.com.antoniolps.CommunityCenterAPI.model.Address;
import br.com.antoniolps.CommunityCenterAPI.model.CommunityCenter;
import br.com.antoniolps.CommunityCenterAPI.model.dtos.CommunityCenterRequest;
import br.com.antoniolps.CommunityCenterAPI.model.enums.ResourceTypeEnum;
import br.com.antoniolps.CommunityCenterAPI.repository.CommunityCenterRepository;
import br.com.antoniolps.CommunityCenterAPI.service.CommunityCenterServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommunityCenterServiceImpl Tests")
class CommunityCenterServiceImplTest {

    @Mock
    private CommunityCenterRepository communityCenterRepository;

    @InjectMocks
    private CommunityCenterServiceImpl communityCenterService;

    private CommunityCenter communityCenter;
    private UUID centerId;
    private CommunityCenterRequest validRequest;
    private Address address;

    @BeforeEach
    void setUp() {
        centerId = UUID.randomUUID();


        address = new Address();
        address.setStreet("Rua Teste, 123");
        address.setCity("João Pessoa");
        address.setState("PB");
        address.setZipCode("58000-000");
        address.setLatitude(-7.1195);
        address.setLongitude(-34.8450);

        communityCenter = CommunityCenter.builder()
                .id(centerId)
                .name("Centro Teste")
                .address(address)
                .maxCapacity(100)
                .currentOccupancy(50)
                .resourceMap(createResourceMap())
                .build();

        validRequest = new CommunityCenterRequest();
        validRequest.setName("Centro Novo");
        validRequest.setAddress(address);
        validRequest.setMaxCapacity(200);
        validRequest.setCurrentOccupancy(0);
        validRequest.setResources(createResourceMap());
    }

    private Map<ResourceTypeEnum, Integer> createResourceMap() {
        Map<ResourceTypeEnum, Integer> resources = new HashMap<>();
        resources.put(ResourceTypeEnum.VOLUNTARIO, 10);
        resources.put(ResourceTypeEnum.KIT_MEDICO, 5);
        resources.put(ResourceTypeEnum.MEDICO, 3);
        return resources;
    }

    @Test
    @DisplayName("Deve adicionar centro comunitário com dados válidos")
    void shouldAddCenterWithValidData() {

        when(communityCenterRepository.save(any(CommunityCenter.class))).thenReturn(communityCenter);

        assertDoesNotThrow(() -> communityCenterService.addCenter(validRequest));

        verify(communityCenterRepository, times(1)).save(any(CommunityCenter.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar adicionar centro com nome nulo")
    void shouldThrowExceptionWhenAddingCenterWithNullName() {
        validRequest.setName(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> communityCenterService.addCenter(validRequest)
        );

        assertEquals("Centro comunitário inválido: nome, endereço e capacidade máxima são obrigatórios.",
                exception.getMessage());
        verify(communityCenterRepository, never()).save(any(CommunityCenter.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar adicionar centro com endereço nulo")
    void shouldThrowExceptionWhenAddingCenterWithNullAddress() {
        validRequest.setAddress(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> communityCenterService.addCenter(validRequest)
        );

        assertEquals("Centro comunitário inválido: nome, endereço e capacidade máxima são obrigatórios.",
                exception.getMessage());
        verify(communityCenterRepository, never()).save(any(CommunityCenter.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar adicionar centro com capacidade máxima inválida")
    void shouldThrowExceptionWhenAddingCenterWithInvalidMaxCapacity() {

        validRequest.setMaxCapacity(-1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> communityCenterService.addCenter(validRequest)
        );

        assertEquals("Centro comunitário inválido: nome, endereço e capacidade máxima são obrigatórios.",
                exception.getMessage());
        verify(communityCenterRepository, never()).save(any(CommunityCenter.class));
    }

    @Test
    @DisplayName("Deve atualizar ocupação com sucesso")
    void shouldUpdateOccupancySuccessfully() {
        int newOccupancy = 75;
        when(communityCenterRepository.findById(centerId)).thenReturn(Optional.of(communityCenter));
        when(communityCenterRepository.save(any(CommunityCenter.class))).thenReturn(communityCenter);

        assertDoesNotThrow(() -> communityCenterService.updateOccupancy(centerId.toString(), newOccupancy));

        verify(communityCenterRepository, times(1)).findById(centerId);
        verify(communityCenterRepository, times(1)).save(communityCenter);
        assertEquals(newOccupancy, communityCenter.getCurrentOccupancy());
    }

    @Test
    @DisplayName("Deve gerar notificação quando ocupação atingir capacidade máxima")
    void shouldGenerateNotificationWhenOccupancyReachesMaxCapacity() {
        int maxCapacity = communityCenter.getMaxCapacity();
        when(communityCenterRepository.findById(centerId)).thenReturn(Optional.of(communityCenter));
        when(communityCenterRepository.save(any(CommunityCenter.class))).thenReturn(communityCenter);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            communityCenterService.updateOccupancy(centerId.toString(), maxCapacity);

            String output = outputStream.toString();
            assertTrue(output.contains("Alerta: O centro comunitário " + communityCenter.getName() + " atingiu sua capacidade máxima."));
            assertEquals(maxCapacity, communityCenter.getCurrentOccupancy());
        } finally {
            System.setOut(originalOut);
        }

        verify(communityCenterRepository, times(1)).save(communityCenter);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar ocupação com valor negativo")
    void shouldThrowExceptionWhenUpdatingOccupancyWithNegativeValue() {
        int invalidOccupancy = -5;
        when(communityCenterRepository.findById(centerId)).thenReturn(Optional.of(communityCenter));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> communityCenterService.updateOccupancy(centerId.toString(), invalidOccupancy)
        );

        assertEquals("Ocupação inválida: deve ser entre 0 e a capacidade máxima do centro comunitário.",
                exception.getMessage());
        verify(communityCenterRepository, never()).save(any(CommunityCenter.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar ocupação acima da capacidade máxima")
    void shouldThrowExceptionWhenUpdatingOccupancyAboveMaxCapacity() {
        int invalidOccupancy = 150;
        when(communityCenterRepository.findById(centerId)).thenReturn(Optional.of(communityCenter));
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> communityCenterService.updateOccupancy(centerId.toString(), invalidOccupancy)
        );

        assertEquals("Ocupação inválida: deve ser entre 0 e a capacidade máxima do centro comunitário.",
                exception.getMessage());
        verify(communityCenterRepository, never()).save(any(CommunityCenter.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando centro não for encontrado para atualização")
    void shouldThrowExceptionWhenCenterNotFoundForUpdate() {
        when(communityCenterRepository.findById(centerId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> communityCenterService.updateOccupancy(centerId.toString(), 50)
        );

        assertEquals("Centro comunitário não encontrado", exception.getMessage());
        verify(communityCenterRepository, never()).save(any(CommunityCenter.class));
    }

    @Test
    @DisplayName("Deve executar notificação de capacidade máxima para todos os centros")
    void shouldPerformMaxCapacityNotificationForAllCenters() {
        CommunityCenter centerAtMaxCapacity = CommunityCenter.builder()
                .name("Centro Lotado")
                .maxCapacity(50)
                .currentOccupancy(50)
                .build();

        CommunityCenter centerBelowMaxCapacity = CommunityCenter.builder()
                .name("Centro Normal")
                .maxCapacity(80)
                .currentOccupancy(40)
                .build();

        List<CommunityCenter> centers = Arrays.asList(centerAtMaxCapacity, centerBelowMaxCapacity);

        when(communityCenterRepository.findByCurrentOccupancyGreaterThan(0)).thenReturn(centers);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            communityCenterService.performMaxCapacityNotification();

            String output = outputStream.toString();
            assertTrue(output.contains("Alerta: O centro comunitário Centro Lotado atingiu sua capacidade máxima."));
            assertFalse(output.contains("Centro Normal"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    @DisplayName("Deve retornar true quando centro estiver lotado")
    void shouldReturnTrueWhenCenterIsFull() {
        communityCenter.setCurrentOccupancy(communityCenter.getMaxCapacity());
        when(communityCenterRepository.findById(centerId)).thenReturn(Optional.of(communityCenter));

        boolean isFull = communityCenterService.isCenterFull(centerId);

        assertTrue(isFull);
    }

    @Test
    @DisplayName("Deve retornar false quando centro não estiver lotado")
    void shouldReturnFalseWhenCenterIsNotFull() {
        communityCenter.setCurrentOccupancy(50);
        when(communityCenterRepository.findById(centerId)).thenReturn(Optional.of(communityCenter));

        boolean isFull = communityCenterService.isCenterFull(centerId);

        assertFalse(isFull);
    }

    @Test
    @DisplayName("Deve lançar exceção quando centro não for encontrado para verificar se está lotado")
    void shouldThrowExceptionWhenCenterNotFoundForFullCheck() {
        when(communityCenterRepository.findById(centerId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> communityCenterService.isCenterFull(centerId)
        );

        assertEquals("Centro comunitário não encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Deve retornar todos os centros")
    void shouldReturnAllCenters() {
        List<CommunityCenter> centers = Arrays.asList(communityCenter);
        when(communityCenterRepository.findAll()).thenReturn(centers);

        List<CommunityCenter> result = communityCenterService.getAllCenters();

        assertEquals(1, result.size());
        assertEquals(communityCenter, result.get(0));
    }

    @Test
    @DisplayName("Deve retornar centro por ID")
    void shouldReturnCenterById() {
        when(communityCenterRepository.findById(centerId)).thenReturn(Optional.of(communityCenter));

        CommunityCenter result = communityCenterService.getCenterById(centerId.toString());

        assertEquals(communityCenter, result);
    }

    @Test
    @DisplayName("Deve lançar exceção quando centro não for encontrado por ID")
    void shouldThrowExceptionWhenCenterNotFoundById() {
        when(communityCenterRepository.findById(centerId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> communityCenterService.getCenterById(centerId.toString())
        );

        assertEquals("Centro comunitário não encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Deve deletar centro com sucesso")
    void shouldDeleteCenterSuccessfully() {
        when(communityCenterRepository.findById(centerId)).thenReturn(Optional.of(communityCenter));

        assertDoesNotThrow(() -> communityCenterService.deleteCenter(centerId.toString()));

        verify(communityCenterRepository, times(1)).findById(centerId);
        verify(communityCenterRepository, times(1)).delete(communityCenter);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar centro não encontrado")
    void shouldThrowExceptionWhenDeletingCenterNotFound() {
        when(communityCenterRepository.findById(centerId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> communityCenterService.deleteCenter(centerId.toString())
        );

        assertEquals("Centro comunitário não encontrado", exception.getMessage());
        verify(communityCenterRepository, never()).delete(any(CommunityCenter.class));
    }

    @Test
    @DisplayName("Deve retornar centro com maior ocupação")
    void shouldReturnCenterWithHighestOccupancy() {
        CommunityCenter centerWithLowOccupancy = CommunityCenter.builder()
                .currentOccupancy(20)
                .build();

        communityCenter.setCurrentOccupancy(80);

        List<CommunityCenter> centers = Arrays.asList(centerWithLowOccupancy, communityCenter);
        when(communityCenterRepository.findAll()).thenReturn(centers);

        CommunityCenter result = communityCenterService.listCenterWithHighestOccupancy();

        assertEquals(communityCenter, result);
        assertEquals(80, result.getCurrentOccupancy());
    }

    @Test
    @DisplayName("Deve lançar exceção quando não há centros para encontrar o de maior ocupação")
    void shouldThrowExceptionWhenNoCentersFoundForHighestOccupancy() {
        when(communityCenterRepository.findAll()).thenReturn(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> communityCenterService.listCenterWithHighestOccupancy()
        );

        assertEquals("Nenhum centro comunitário encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Deve retornar centros com ocupação maior que 90%")
    void shouldReturnCentersWithOccupancyGreaterThanNinetyPercent() {
        CommunityCenter centerWithHighOccupancy = CommunityCenter.builder()
                .maxCapacity(100)
                .currentOccupancy(95) // 95%
                .build();

        CommunityCenter centerWithLowOccupancy = CommunityCenter.builder()
                .maxCapacity(100)
                .currentOccupancy(80) // 80%
                .build();

        List<CommunityCenter> allCenters = Arrays.asList(centerWithHighOccupancy, centerWithLowOccupancy);
        when(communityCenterRepository.findByCurrentOccupancyGreaterThan(0)).thenReturn(allCenters);

        List<CommunityCenter> result = communityCenterService.getCentersWithOccupancyGreaterThanNinetyPercent();

        assertEquals(1, result.size());
        assertEquals(centerWithHighOccupancy, result.get(0));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há centros com ocupação maior que 90%")
    void shouldReturnEmptyListWhenNoCentersWithHighOccupancy() {
        CommunityCenter centerWithLowOccupancy = CommunityCenter.builder()
                .maxCapacity(100)
                .currentOccupancy(80) // 80%
                .build();

        List<CommunityCenter> allCenters = Arrays.asList(centerWithLowOccupancy);
        when(communityCenterRepository.findByCurrentOccupancyGreaterThan(0)).thenReturn(allCenters);

        List<CommunityCenter> result = communityCenterService.getCentersWithOccupancyGreaterThanNinetyPercent();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve calcular média de recursos por tipo corretamente")
    void shouldCalculateAverageResourcesPerTypeCorrectly() {
        CommunityCenter center1 = CommunityCenter.builder()
                .resourceMap(Map.of(
                        ResourceTypeEnum.VOLUNTARIO, 10,
                        ResourceTypeEnum.KIT_MEDICO, 5,
                        ResourceTypeEnum.MEDICO, 2
                ))
                .build();

        CommunityCenter center2 = CommunityCenter.builder()
                .resourceMap(Map.of(
                        ResourceTypeEnum.VOLUNTARIO, 20,
                        ResourceTypeEnum.KIT_MEDICO, 15,
                        ResourceTypeEnum.VEICULO, 8
                ))
                .build();

        List<CommunityCenter> centers = Arrays.asList(center1, center2);
        when(communityCenterRepository.findAll()).thenReturn(centers);

        Map<ResourceTypeEnum, Double> result = communityCenterService.getAverageResourcesPerType();

        assertEquals(15.0, result.get(ResourceTypeEnum.VOLUNTARIO), 0.001);
        assertEquals(10.0, result.get(ResourceTypeEnum.KIT_MEDICO), 0.001);
        assertEquals(2.0, result.get(ResourceTypeEnum.MEDICO), 0.001);
        assertEquals(8.0, result.get(ResourceTypeEnum.VEICULO), 0.001);
    }

    @Test
    @DisplayName("Deve retornar mapa vazio quando não há centros para calcular média de recursos")
    void shouldReturnEmptyMapWhenNoCentersForAverageResources() {
        when(communityCenterRepository.findAll()).thenReturn(Collections.emptyList());

        Map<ResourceTypeEnum, Double> result = communityCenterService.getAverageResourcesPerType();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve calcular pontuação correta dos recursos baseada nos pontos do enum")
    void shouldCalculateCorrectResourcePointsBasedOnEnum() {
        Map<ResourceTypeEnum, Integer> resources = Map.of(
                ResourceTypeEnum.MEDICO, 2,
                ResourceTypeEnum.VOLUNTARIO, 5,
                ResourceTypeEnum.KIT_MEDICO, 3,
                ResourceTypeEnum.VEICULO, 1,
                ResourceTypeEnum.CESTA_BASICA, 10
        );

        int expectedTotalPoints = (2 * ResourceTypeEnum.MEDICO.getPoints()) +
                (5 * ResourceTypeEnum.VOLUNTARIO.getPoints()) +
                (3 * ResourceTypeEnum.KIT_MEDICO.getPoints()) +
                (1 * ResourceTypeEnum.VEICULO.getPoints()) +
                (10 * ResourceTypeEnum.CESTA_BASICA.getPoints());

        assertEquals(69, expectedTotalPoints);

        assertEquals(4, ResourceTypeEnum.MEDICO.getPoints());
        assertEquals(3, ResourceTypeEnum.VOLUNTARIO.getPoints());
        assertEquals(7, ResourceTypeEnum.KIT_MEDICO.getPoints());
        assertEquals(5, ResourceTypeEnum.VEICULO.getPoints());
        assertEquals(2, ResourceTypeEnum.CESTA_BASICA.getPoints());
    }
}
