package com.banking.frauddectectionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FraudDectectionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FraudDectectionServiceApplication.class, args);
	}

}
