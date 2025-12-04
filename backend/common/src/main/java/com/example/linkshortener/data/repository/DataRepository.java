package com.example.linkshortener.data.repository;

import com.example.linkshortener.data.entity.Data;
import com.example.linkshortener.sharding.ShardingKey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DataRepository extends JpaRepository<Data, Long> {

    Optional<Data> findByShortenedUrl(@ShardingKey String shortenedUrl);

    boolean existsByShortenedUrl(@ShardingKey String shortenedUrl);

    // Override save() để gắn @ShardingKey, đảm bảo Aspect bắt được sharding key
    @Override
    <S extends Data> S save(@ShardingKey S entity);

}