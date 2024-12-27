package com.hocs.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class HocsserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(HocsserverApplication.class, args);
	}

}
