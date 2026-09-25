package com.fordchallenge.ford_competitive_api.vehicles.repository;

import com.fordchallenge.ford_competitive_api.vehicles.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByMarcaIgnoreCaseAndModeloIgnoreCaseAndAnoAndVersaoIgnoreCase(
            String marca,
            String modelo,
            Integer ano,
            String versao
    );
}