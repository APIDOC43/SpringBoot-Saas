package com.hocs.server.ratelimit;

import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AutoScalingService {

	@Autowired
	private RequestQueueService requestQueueService;

	/**
	 * 매 분마다 큐의 가장 오래된 요청을 확인하여 5분 이상 대기하면 오토스케일링 트리거
	 */
	@Scheduled(fixedDelay = 50000)
	public void checkAutoScaling() {
		RateLimitRequest oldest = requestQueueService.peek();
		if (oldest != null) {
			Duration waitingTime = Duration.between(oldest.getArrivalTime(), LocalDateTime.now());
			if (waitingTime.toMinutes() >= 50000) {
				System.out.println("오토스케일링 요청 발생 - 대기 시간: " + waitingTime.toMillis() + "ms");
			}
		}
	}
}