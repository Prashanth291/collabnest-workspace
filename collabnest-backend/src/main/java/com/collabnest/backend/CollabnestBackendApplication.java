package com.collabnest.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CollabnestBackendApplication {

	public static void main(String[] args) {
		try {
			Dotenv dotenv = Dotenv.configure()
					.ignoreIfMissing()
					.load();
			
			dotenv.entries().forEach(entry -> 
				System.setProperty(entry.getKey(), entry.getValue())
			);
		} catch (Exception e) {
			System.out.println("No .env file found, using system environment variables");
		}
		
		SpringApplication.run(CollabnestBackendApplication.class, args);
	}

}
