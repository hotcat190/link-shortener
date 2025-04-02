package com.example.linkshortener.data.repository;

import com.example.linkshortener.data.entity.Data;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataRepository extends JpaRepository<Data, String> {
}
