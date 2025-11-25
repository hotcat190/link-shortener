package com.example.linkshortener.data.dto;

import com.example.linkshortener.util.validation.NullOrNotBlank;
import com.example.linkshortener.util.validation.NullOrPositive;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreationRequest {
    @NotEmpty
    private String url;

    @NullOrPositive
    private Long ttlMinute;

    @NullOrNotBlank
    private String customShortenedUrl;
}
