package com.masterminds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class VerandaApplication {

	public static void main(String[] args) {
		SpringApplication.run(VerandaApplication.class, args);
	}

}
