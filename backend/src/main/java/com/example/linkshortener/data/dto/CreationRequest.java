package com.example.linkshortener.data.dto;

import com.example.linkshortener.util.NullOrNotBlank;
import com.example.linkshortener.util.NullOrPositive;
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
