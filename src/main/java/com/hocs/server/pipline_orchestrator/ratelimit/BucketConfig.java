package com.hocs.server.pipline_orchestrator.ratelimit;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class BucketConfig {

	@Bean
	public Bucket bucket(@Value("${ratelimit.bucket.capacity:20}") int capacity) {
		Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(1, Duration.ofDays(365 * 100)));
		log.info("capacity = {}",capacity);
		return Bucket.builder().addLimit(limit).build();
	}
}