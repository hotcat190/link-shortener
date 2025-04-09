package com.example.linkshortener.service;

import com.example.linkshortener.data.entity.Data;
import com.example.linkshortener.data.repository.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public final class DataService {
    private final DataRepository dataRepository;

    @Autowired
    public DataService(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public String findOrigin(String shortenedUrl) {
        Data data = dataRepository.findByShortenedUrl(shortenedUrl).orElse(null);

        if (data != null) {
            // Check if the URL has expired
            // URL is expired if expirationTime is not null and is before the current time
            // If expirationTime is null, the URL is not expired
            if (data.getExpirationTime() == null ||
                    data.getExpirationTime().isAfter(LocalDateTime.now())) {
                data.setClickCount(data.getClickCount() + 1);
                dataRepository.save(data);
                return data.getUrl();
            } else {
                deleteUrl(shortenedUrl);
                return null;
            }
        }

        return null;
    }

    public String shortenUrl(String url, Long ttlMinute, String customShortenedUrl) throws SQLIntegrityConstraintViolationException {
        String shortenedUrl = customShortenedUrl != null ?
                customShortenedUrl : UUID.randomUUID().toString();

        LocalDateTime creationTime = LocalDateTime.now();

        LocalDateTime expirationTime = ttlMinute == null ?
                null : creationTime.plusMinutes(ttlMinute);

        Data data = Data.builder()
                .url(url)
                .shortenedUrl(shortenedUrl)
                .creationTime(creationTime)
                .expirationTime(expirationTime)
                .build();

        dataRepository.save(data);

        return data.getShortenedUrl();
    }

    public void deleteUrl(String shortenedUrl) {
        dataRepository.findByShortenedUrl(shortenedUrl)
                .ifPresent(dataRepository::delete);
    }

    public void deleteUrlById(Long id) {
        dataRepository.deleteById(id);
    }

    public List<Data> findAll() {
        return dataRepository.findAll();
    }

    public void save(Data data) {
        dataRepository.save(data);
    }

    public boolean isShortenedUrlExisting(String shortenedUrl) {
        return dataRepository.existsByShortenedUrl(shortenedUrl);
    }
}
