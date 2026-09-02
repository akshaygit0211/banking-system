package com.banking.paymentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class corsConfig {
	
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		
		return new WebMvcConfigurer() {
			
			public void addCorsMappping(CorsRegistry registry) {
				registry.addMapping( "/api/**")
				.allowedHeaders("*") 
				.allowedMethods("GET","PUT","POST","DELETE")
				.allowedHeaders("*");
					
				
			}
		
		};
	}
}
