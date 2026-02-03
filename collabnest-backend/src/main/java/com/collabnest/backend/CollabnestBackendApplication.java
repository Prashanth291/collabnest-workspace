package com.collabnest.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CollabnestBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CollabnestBackendApplication.class, args);
	}

}
