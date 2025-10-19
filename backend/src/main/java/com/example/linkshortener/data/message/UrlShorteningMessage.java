package com.example.linkshortener.data.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlShorteningMessage {
    private String originalUrl;
    private String customShortenedUrl;
    private Long ttlMinute;
}