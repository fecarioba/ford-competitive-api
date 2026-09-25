package com.fordchallenge.ford_competitive_api.vehicles.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String marca;

    private String modelo;

    private Integer ano;

    private String versao;

    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "vehicle", cascade = CascadeType.ALL)
    private VehicleSpec specs;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}