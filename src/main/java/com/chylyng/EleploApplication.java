package com.chylyng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class EleploApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(EleploApplication.class);
		app.run(args);
		System.out.println("========================================");
		System.out.println("===== Eleplo Parser Version: v2.2.0 ====");
		System.out.println("========================================");
	}

	@Bean
	@Scope("prototype")
	public ObjectMapper getObjectMapper() {
		return new ObjectMapper();
	}

	@Bean
	@Scope("prototype")
	public RestTemplate getRestTemplate() {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(3 * 1000);
		requestFactory.setReadTimeout(3 * 1000);
		return new RestTemplate(requestFactory);
	}
	



}
