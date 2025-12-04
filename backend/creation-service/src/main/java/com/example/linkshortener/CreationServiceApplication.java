package com.example.linkshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class CreationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CreationServiceApplication.class, args);
	}

}
