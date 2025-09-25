package com.example.linkshortener.data.dto;

import com.example.linkshortener.util.validation.NullOrNotBlank;
import com.example.linkshortener.util.validation.NullOrPositive;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor
public class CreationRequest {
    @NotEmpty
    @NonNull
    private String url;

    @NullOrPositive
    private Long ttlMinute;

    @NullOrNotBlank
    private String customShortenedUrl;
}
