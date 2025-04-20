package com.example.linkshortener.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@lombok.Data
@Entity
@Table(
        name = "data",
        indexes = {
            @Index(name = "idx_shortened_url", columnList = "shortenedUrl")
        },
        uniqueConstraints = {
            @UniqueConstraint(name = "uc_shortened_url", columnNames = "shortenedUrl")
        }
)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Data {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String url;

    @Column(unique = true, nullable = false)
    private String shortenedUrl;

    @Column(nullable = false)
    private int clickCount;

    @Column(nullable = false)
    private LocalDateTime creationTime;

    @Column
    private LocalDateTime expirationTime;
}
