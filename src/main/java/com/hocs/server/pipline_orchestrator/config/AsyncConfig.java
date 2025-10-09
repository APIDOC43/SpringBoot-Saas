package com.hocs.server.pipline_orchestrator.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
	@Value("${pipeline.thread.pool.heavy.size}")
	private int HEAVY_PIPELINE_THREAD_POOL_SIZE;
	@Value("${pipeline.thread.pool.fast.size}")
	private int FAST_PIPELINE_THREAD_POOL_SIZE;


	@Bean(name = "HeavyAsyncExecutor")
	public Executor HeavyAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(HEAVY_PIPELINE_THREAD_POOL_SIZE);
		executor.setMaxPoolSize(HEAVY_PIPELINE_THREAD_POOL_SIZE);
		executor.setQueueCapacity(0);
		executor.setKeepAliveSeconds(30);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		executor.setThreadNamePrefix("HeavyAsyncExecutor-");
		executor.initialize();
		return executor;
	}

	@Bean(name = "FastAsyncExecutor")
	public Executor FastAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(FAST_PIPELINE_THREAD_POOL_SIZE);
		executor.setMaxPoolSize(FAST_PIPELINE_THREAD_POOL_SIZE);
		executor.setQueueCapacity(0);
		executor.setKeepAliveSeconds(30);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		executor.setThreadNamePrefix("FastAsyncExecutor-");
		executor.initialize();
		return executor;
	}
}
