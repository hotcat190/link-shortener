package com.example.linkshortener.unit;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import com.example.linkshortener.data.dto.CreationRequest;
import com.example.linkshortener.data.entity.Data;
import com.example.linkshortener.data.repository.DataRepository;
import com.example.linkshortener.service.DataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;


@ExtendWith(MockitoExtension.class)
class DataServiceTests {
    @Mock
    private DataRepository dataRepository;

    @InjectMocks
    private DataService dataService;

    @Test
    void whenCreateShortUrl_thenShouldSaveAndReturnSlug() throws SQLIntegrityConstraintViolationException {
        String originalUrl = "https://google.com";
        String shortenedUrl = "asiof";
        LocalDateTime creationTime = LocalDateTime.now();

        Data urlMappingToSave = Data.builder()
                            .url(originalUrl)
                            .shortenedUrl(shortenedUrl)
                            .creationTime(creationTime)
                            .expirationTime(null)
                            .clickCount(0)
                            .build();


        when(dataRepository.save(any(Data.class))).thenReturn(urlMappingToSave);

        String slug = dataService.shortenUrl(CreationRequest.of(originalUrl));

        assertThat(slug).isNotNull().isNotEmpty();
        verify(dataRepository, times(1)).save(any(Data.class));
    }
}
