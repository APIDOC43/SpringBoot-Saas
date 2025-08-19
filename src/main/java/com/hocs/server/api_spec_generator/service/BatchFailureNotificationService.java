package com.hocs.server.api_spec_generator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BatchFailureNotificationService {
    
    public void sendFailureAlert(String entityId, String entityType, String failureReason, Integer retryCount) {
        String slackMessage = createSlackMessage(entityId, entityType, failureReason, retryCount);
        
        // TODO: 실제 Slack API 호출
        // slackClient.sendMessage(slackMessage);
        
        // 현재는 로그로 대체
        log.warn("[SLACK ALERT] 배치 실패 알림: {}", slackMessage);
        
        // 심각한 경우 추가 알림 (재시도 3회 이상)
        if (retryCount >= 3) {
            log.error("[CRITICAL] 배치 실패 임계치 초과 - 즉시 확인 필요: EntityID={}, RetryCount={}", 
                     entityId, retryCount);
        }
    }
    
    public void sendBatchHealthCheck(long totalFailed, long recentFailed) {
        String healthMessage = String.format(
            "배치 시스템 상태 체크 - 총 실패 건수: %d, 최근 1시간 실패: %d, 상태: %s",
            totalFailed, 
            recentFailed,
            recentFailed > 10 ? "주의" : "정상"
        );
        
        log.info("[BATCH HEALTH] {}", healthMessage);
    }
    
    private String createSlackMessage(String entityId, String entityType, String failureReason, Integer retryCount) {
        return String.format(
            "배치 처리 실패 알림 - Entity ID: %s, Type: %s, 실패 사유: %s, 재시도 횟수: %d, 시간: %s",
            entityId, entityType, failureReason, retryCount, 
            java.time.LocalDateTime.now().toString()
        );
    }
}
