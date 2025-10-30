package com.example.linkshortener.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClickEvent implements Serializable {
    private String shortenedUrl;
    private LocalDateTime timestamp;
}