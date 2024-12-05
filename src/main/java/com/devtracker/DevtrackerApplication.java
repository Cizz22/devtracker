package com.devtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.devtracker") 
public class DevtrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevtrackerApplication.class, args);
	}

}
