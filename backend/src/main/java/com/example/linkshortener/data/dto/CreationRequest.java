package com.example.linkshortener.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreationRequest {
    private String url;
    private Long ttlMinute;
    private String customShortenedUrl;
}
