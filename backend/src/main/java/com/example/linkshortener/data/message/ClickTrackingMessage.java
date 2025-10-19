package com.example.linkshortener.data.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClickTrackingMessage {
    private String shortenedUrl;
    private String ipAddress;
    private long timestamp;
}