package com.fordchallenge.ford_competitive_api.vehicles.controller;

import com.fordchallenge.ford_competitive_api.vehicles.dto.VehicleResponse;
import com.fordchallenge.ford_competitive_api.vehicles.dto.VehicleSearchOutcome;
import com.fordchallenge.ford_competitive_api.vehicles.dto.VehicleSearchRequest;
import com.fordchallenge.ford_competitive_api.vehicles.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@Tag(name = "Vehicles", description = "Consulta e pesquisa de veículos (endpoints de leitura são públicos)")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @Operation(summary = "Lista todos os veículos já pesquisados/cadastrados (público)")
    @GetMapping
    public ResponseEntity<List<VehicleResponse>> findAll() {
        return ResponseEntity.ok(vehicleService.findAll());
    }

    @Operation(summary = "Busca um veículo pelo id (público)")
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.findById(id));
    }

    @Operation(summary = "Pesquisa um veículo por marca/modelo/ano/versão (protegido - requer Bearer Token)")
    @PostMapping("/search")
    public ResponseEntity<VehicleResponse> searchVehicle(
            @RequestBody @Valid VehicleSearchRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        VehicleSearchOutcome outcome = vehicleService.searchVehicle(request);

        if (outcome.created()) {
            var location = uriComponentsBuilder
                    .path("/vehicles/{id}")
                    .buildAndExpand(outcome.vehicle().id())
                    .toUri();

            return ResponseEntity.created(location).body(outcome.vehicle());
        }

        return ResponseEntity.status(HttpStatus.OK).body(outcome.vehicle());
    }
}
