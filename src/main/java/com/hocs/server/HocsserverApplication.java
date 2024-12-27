package com.hocs.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.ExceptionHandler;

@EnableAsync
@SpringBootApplication
public class HocsserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(HocsserverApplication.class, args);
	}

}
