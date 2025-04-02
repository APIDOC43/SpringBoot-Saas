package com.hocs.server.pipline_orchestrator.config;


import java.util.concurrent.Semaphore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SemaphoreConfig {

	@Value("${pipeline.thread.pool.heavy.size}")
	private int HEAVY_PIPELINE_THREAD_POOL_SIZE;
	@Value("${pipeline.thread.pool.heavy.size}")
	private int FAST_PIPELINE_THREAD_POOL_SIZE;
	@Bean(name = "HeavyQueueSemaphore")
	public Semaphore heavyQueueSemaphore() {
		return new Semaphore(HEAVY_PIPELINE_THREAD_POOL_SIZE);
	}

	@Bean(name = "FastQueueSemaphore")
	public Semaphore fastQueueSemaphore() {
		return new Semaphore(FAST_PIPELINE_THREAD_POOL_SIZE);
	}
}