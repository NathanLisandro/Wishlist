package com.wishlist.nathan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NathanApplication {

	public static void main(String[] args) {
		SpringApplication.run(NathanApplication.class, args);
	}

}
