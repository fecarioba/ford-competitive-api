package com.fordchallenge.ford_competitive_api.vehicles.service;

import com.fordchallenge.ford_competitive_api.common.exception.ResourceNotFoundException;
import com.fordchallenge.ford_competitive_api.searches.repository.SearchHistoryRepository;
import com.fordchallenge.ford_competitive_api.security.AuthenticatedUserProvider;
import com.fordchallenge.ford_competitive_api.users.entity.User;
import com.fordchallenge.ford_competitive_api.users.entity.UserRole;
import com.fordchallenge.ford_competitive_api.vehicles.dto.VehicleSearchOutcome;
import com.fordchallenge.ford_competitive_api.vehicles.dto.VehicleSearchRequest;
import com.fordchallenge.ford_competitive_api.vehicles.entity.Vehicle;
import com.fordchallenge.ford_competitive_api.vehicles.entity.VehicleSpec;
import com.fordchallenge.ford_competitive_api.vehicles.repository.VehicleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários de VehicleService (Sprint 3 - critério "Testes Automatizados"):
 * cenário de sucesso ao encontrar veículo existente, criação de mock quando não existe,
 * vínculo correto do histórico ao usuário autenticado (correção do bug do id fixo)
 * e cenário de erro (404) ao buscar por id inexistente.
 */
@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private SearchHistoryRepository searchHistoryRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleService(vehicleRepository, searchHistoryRepository, authenticatedUserProvider);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Vehicle buildVehicleWithSpec() {
        Vehicle vehicle = Vehicle.builder()
                .id(10L)
                .marca("Ford")
                .modelo("Ranger")
                .ano(2026)
                .versao("Raptor")
                .build();

        VehicleSpec spec = VehicleSpec.builder()
                .potencia("397cv")
                .torque("58kgfm")
                .combustivel("Gasolina")
                .cambio("Automático 10 marchas")
                .consumo("7km/l")
                .vehicle(vehicle)
                .build();

        vehicle.setSpecs(spec);
        return vehicle;
    }

    @Test
    void deveRetornarVeiculoExistenteSemCriarNovo() {
        VehicleSearchRequest request = new VehicleSearchRequest("Ford", "Ranger", 2026, "Raptor");
        Vehicle existente = buildVehicleWithSpec();

        when(vehicleRepository.findByMarcaIgnoreCaseAndModeloIgnoreCaseAndAnoAndVersaoIgnoreCase(
                "Ford", "Ranger", 2026, "Raptor"
        )).thenReturn(Optional.of(existente));

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(User.builder().id(5L).build());
        VehicleSearchOutcome outcome = vehicleService.searchVehicle(request);

        assertThat(outcome.created()).isFalse();
        assertThat(outcome.vehicle().marca()).isEqualTo("Ford");
        verify(vehicleRepository, never()).save(any());
        verify(searchHistoryRepository).save(any());
    }

    @Test
    void deveCriarVeiculoMockadoQuandoNaoExiste() {
        VehicleSearchRequest request = new VehicleSearchRequest("Ford", "Bronco", 2026, "Wildtrak");

        when(vehicleRepository.findByMarcaIgnoreCaseAndModeloIgnoreCaseAndAnoAndVersaoIgnoreCase(
                "Ford", "Bronco", 2026, "Wildtrak"
        )).thenReturn(Optional.empty());

        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle vehicle = invocation.getArgument(0);
            vehicle.setId(99L);
            return vehicle;
        });

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(User.builder().id(5L).build());
        VehicleSearchOutcome outcome = vehicleService.searchVehicle(request);

        assertThat(outcome.created()).isTrue();
        assertThat(outcome.vehicle().modelo()).isEqualTo("Bronco");
        assertThat(outcome.vehicle().potencia()).isNotBlank();
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void deveVincularHistoricoAoUsuarioAutenticado() {
        VehicleSearchRequest request = new VehicleSearchRequest("Ford", "Ranger", 2026, "Raptor");
        Vehicle existente = buildVehicleWithSpec();
        User usuarioLogado = User.builder().id(5L).email("victor@test.com").role(UserRole.USER).build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuarioLogado, null, List.of())
        );
        when(authenticatedUserProvider.getCurrentUser()).thenReturn(usuarioLogado);

        when(vehicleRepository.findByMarcaIgnoreCaseAndModeloIgnoreCaseAndAnoAndVersaoIgnoreCase(
                "Ford", "Ranger", 2026, "Raptor"
        )).thenReturn(Optional.of(existente));

        vehicleService.searchVehicle(request);

        verify(searchHistoryRepository).save(argThat(history ->
                history.getUser() != null && history.getUser().getId().equals(5L)
        ));
    }

    @Test
    void deveLancarResourceNotFoundQuandoVeiculoNaoEncontradoPorId() {
        when(vehicleRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.findById(123L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
