package com.example.linkshortener.data.repository;

import com.example.linkshortener.data.entity.Data;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DataRepository extends JpaRepository<Data, Long> {

    Optional<Data> findByShortenedUrl(String shortenedUrl);

    boolean existsByShortenedUrl(String shortenedUrl);

}
