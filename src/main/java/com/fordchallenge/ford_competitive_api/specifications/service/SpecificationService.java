package com.fordchallenge.ford_competitive_api.specifications.service;

import com.fordchallenge.ford_competitive_api.common.exception.ResourceNotFoundException;
import com.fordchallenge.ford_competitive_api.specifications.dto.SpecificationResponse;
import com.fordchallenge.ford_competitive_api.vehicles.entity.Vehicle;
import com.fordchallenge.ford_competitive_api.vehicles.entity.VehicleSpec;
import com.fordchallenge.ford_competitive_api.vehicles.repository.VehicleRepository;
import org.springframework.stereotype.Service;

@Service
public class SpecificationService {

    private final VehicleRepository vehicleRepository;

    public SpecificationService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public SpecificationResponse findByVehicleId(Long vehicleId) {

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com id: " + vehicleId));

        VehicleSpec spec = vehicle.getSpecs();

        return new SpecificationResponse(
                vehicle.getId(),
                spec.getPotencia(),
                spec.getTorque(),
                spec.getCombustivel(),
                spec.getCambio(),
                spec.getConsumo(),
                spec.getFonteUrl()
        );
    }
}
