package com.johnhollandheb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan({"com.johnhollandheb"})
//@ComponentScan("com.johnhollandheb")
public class JohnhollandhebApplication {

	public static void main(String[] args) {
		SpringApplication.run(JohnhollandhebApplication.class, args);
	}

}
