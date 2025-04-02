package com.example.linkshortener.data.entity;

import jakarta.persistence.*;
import lombok.Builder;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@lombok.Data
@Entity
@Table(name = "data")
@Builder
public class Data {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String url;
}
