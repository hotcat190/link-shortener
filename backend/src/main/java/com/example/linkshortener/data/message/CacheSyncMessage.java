package com.example.linkshortener.data.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheSyncMessage {
    private String shortenedUrl;
    private int clickCount;
}