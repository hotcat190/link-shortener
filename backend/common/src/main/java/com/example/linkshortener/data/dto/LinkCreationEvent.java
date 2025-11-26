package com.example.linkshortener.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkCreationEvent implements Serializable {
    private String url;
    private String shortenedUrl;
    private LocalDateTime creationTime;
    private LocalDateTime expirationTime;
}