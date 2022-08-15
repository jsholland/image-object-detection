package com.johnhollandheb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan({"com.johnhollandheb"})
public class JohnhollandhebApplication {

	public static void main(String[] args) {
		SpringApplication.run(JohnhollandhebApplication.class, args);
	}

}
