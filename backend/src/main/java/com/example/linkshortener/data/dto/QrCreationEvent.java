package com.example.linkshortener.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QrCreationEvent implements Serializable {
    private String shortenedUrl;
}