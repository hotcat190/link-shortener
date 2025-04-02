package com.example.linkshortener.service;

import com.example.linkshortener.data.entity.Data;
import com.example.linkshortener.data.repository.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class DataService {
    private final DataRepository dataRepository;

    @Autowired
    public DataService(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public String findOrigin(String id) {
        return dataRepository.findById(id)
                .map(Data::getUrl)
                .orElse(null);
    }

    public String shortUrl(String url) {
        Data data = dataRepository.save(Data.builder().url(url).build());
        String id = data.getId().toString();
        System.out.println("Shortened URL: " + id);
        return id;
    }
}
