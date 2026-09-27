package com.fordchallenge.ford_competitive_api.vehicles.service;

import com.fordchallenge.ford_competitive_api.common.exception.ResourceNotFoundException;
import com.fordchallenge.ford_competitive_api.searches.entity.SearchHistory;
import com.fordchallenge.ford_competitive_api.searches.repository.SearchHistoryRepository;
import com.fordchallenge.ford_competitive_api.security.AuthenticatedUserProvider;
import com.fordchallenge.ford_competitive_api.users.entity.User;
import com.fordchallenge.ford_competitive_api.vehicles.dto.VehicleResponse;
import com.fordchallenge.ford_competitive_api.vehicles.dto.VehicleSearchOutcome;
import com.fordchallenge.ford_competitive_api.vehicles.dto.VehicleSearchRequest;
import com.fordchallenge.ford_competitive_api.vehicles.entity.Vehicle;
import com.fordchallenge.ford_competitive_api.vehicles.entity.VehicleSpec;
import com.fordchallenge.ford_competitive_api.vehicles.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public VehicleService(
            VehicleRepository vehicleRepository,
            SearchHistoryRepository searchHistoryRepository,
            AuthenticatedUserProvider authenticatedUserProvider
    ) {
        this.vehicleRepository = vehicleRepository;
        this.searchHistoryRepository = searchHistoryRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    public List<VehicleResponse> findAll() {
        return vehicleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public VehicleResponse findById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com id: " + id));

        return mapToResponse(vehicle);
    }

    /**
     * Busca um veículo pelos atributos informados. Caso não exista na base,
     * cria um registro (atualmente com dados mockados — ver README, seção
     * "Preparação para Futuras Sprints" sobre integração com IA/scraping real)
     * e retorna indicando que o recurso foi criado (para status HTTP 201).
     *
     * Endpoint protegido: exige usuário autenticado, pois o histórico de busca
     * é sempre vinculado ao usuário do token (ver saveSearchHistory).
     */
    public VehicleSearchOutcome searchVehicle(VehicleSearchRequest request) {

        var existing = vehicleRepository
                .findByMarcaIgnoreCaseAndModeloIgnoreCaseAndAnoAndVersaoIgnoreCase(
                        request.marca(),
                        request.modelo(),
                        request.ano(),
                        request.versao()
                );

        boolean created = existing.isEmpty();
        Vehicle vehicle = existing.orElseGet(() -> createMockVehicle(request));

        saveSearchHistory(request, vehicle);

        return new VehicleSearchOutcome(mapToResponse(vehicle), created);
    }

    private Vehicle createMockVehicle(VehicleSearchRequest request) {

        Vehicle vehicle = Vehicle.builder()
                .marca(request.marca())
                .modelo(request.modelo())
                .ano(request.ano())
                .versao(request.versao())
                .build();

        VehicleSpec spec = VehicleSpec.builder()
                .potencia("180cv")
                .torque("27kgfm")
                .combustivel("Flex")
                .cambio("Automático")
                .consumo("11km/l")
                .fonteUrl("https://mock-api.ford-challenge.com")
                .vehicle(vehicle)
                .build();

        vehicle.setSpecs(spec);

        return vehicleRepository.save(vehicle);
    }

    private void saveSearchHistory(
            VehicleSearchRequest request,
            Vehicle vehicle
    ) {
        // Correção: antes o histórico era sempre gravado com o usuário de id fixo (1L),
        // ignorando quem de fato fez a requisição autenticada. Agora usamos o usuário
        // presente no SecurityContext (populado pelo JwtAuthenticationFilter a partir do token).
        User currentUser = authenticatedUserProvider.getCurrentUser();

        SearchHistory history = SearchHistory.builder()
                .termoBusca(
                        request.marca() + " "
                                + request.modelo() + " "
                                + request.versao()
                )
                .vehicle(vehicle)
                .user(currentUser)
                .build();

        searchHistoryRepository.save(history);
    }

    private VehicleResponse mapToResponse(Vehicle vehicle) {

        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getMarca(),
                vehicle.getModelo(),
                vehicle.getAno(),
                vehicle.getVersao(),
                vehicle.getSpecs().getPotencia(),
                vehicle.getSpecs().getTorque(),
                vehicle.getSpecs().getCombustivel(),
                vehicle.getSpecs().getCambio(),
                vehicle.getSpecs().getConsumo()
        );
    }
}
