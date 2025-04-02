package com.hocs.server.common.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

	@Bean
	public MyCommandCounterListener commandCounterListener() {
		return new MyCommandCounterListener();
	}

	@Bean
	public MongoClient mongoClient(MyCommandCounterListener listener) {
		MongoClientSettings settings = MongoClientSettings.builder()
			.applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
			.addCommandListener(listener)
			.build();

		return MongoClients.create(settings);
	}
}