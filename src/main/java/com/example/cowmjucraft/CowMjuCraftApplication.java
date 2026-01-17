package com.example.cowmjucraft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CowMjuCraftApplication {

	public static void main(String[] args) {
		SpringApplication.run(CowMjuCraftApplication.class, args);
	}

}
