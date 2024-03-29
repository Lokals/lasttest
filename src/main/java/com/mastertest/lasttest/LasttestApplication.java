package com.mastertest.lasttest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LasttestApplication {

	public static void main(String[] args) {
		SpringApplication.run(LasttestApplication.class, args);
	}

}
