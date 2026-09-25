package com.fordchallenge.ford_competitive_api.vehicles.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_specs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String potencia;

    private String torque;

    private String combustivel;

    private String cambio;

    private String consumo;

    private String fonteUrl;

    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonIgnore
    private Vehicle vehicle;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}