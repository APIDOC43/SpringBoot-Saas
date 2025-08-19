package com.hocs.server.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hocs.server.api_spec_generator.domain.FailedBatchEntity;
import com.hocs.server.api_spec_generator.repository.FailedBatchRepository;
import com.hocs.server.api_spec_generator.service.BatchFailureNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenericBatchFailureHandler<T> {
    
    private final FailedBatchRepository failedBatchRepository;
    private final BatchFailureNotificationService notificationService;
    private final ObjectMapper objectMapper;
    
    public void handleFailure(T failedEntity, String entityType, String entityId, int maxRetryCount) {
        log.error("배치 처리 최대 재시도 초과 - Entity Type: {}, Entity ID: {}", 
                 entityType, entityId);
        
        try {
            saveToFailedTable(failedEntity, entityType, entityId, maxRetryCount);
            sendNotification(entityId, entityType, maxRetryCount);
            
        } catch (Exception e) {
            log.error("실패 처리 중 오류 발생: {}", e.getMessage());
            saveToEmergencyFile(failedEntity, entityType, entityId);
        }
    }
    
    private void saveToFailedTable(T failedEntity, String entityType, String entityId, int maxRetryCount) 
            throws JsonProcessingException {
        String entityData = objectMapper.writeValueAsString(failedEntity);
        
        FailedBatchEntity failedRecord = FailedBatchEntity.createFrom(
            entityId,
            entityType,
            entityData,
            "MAX_RETRY_EXCEEDED",
            maxRetryCount,
            "배치 저장 최대 재시도 횟수 초과"
        );
        
        failedBatchRepository.save(failedRecord);
        log.info("실패 엔티티 저장 완료 - Failed Record ID: {}", failedRecord.getId());
    }
    
    private void sendNotification(String entityId, String entityType, int maxRetryCount) {
        notificationService.sendFailureAlert(
            entityId,
            entityType,
            "배치 저장 실패",
            maxRetryCount
        );
    }
    
    private void saveToEmergencyFile(T failedEntity, String entityType, String entityId) {
        try {
            String fileName = "emergency_failed_" + entityType + "_" + System.currentTimeMillis() + "_" + entityId + ".json";
            String entityData = objectMapper.writeValueAsString(failedEntity);
            
            Path path = Paths.get("/tmp", fileName);
            Files.write(path, entityData.getBytes());
            
            log.warn("긴급 파일 저장 완료: {}", path.toString());
        } catch (Exception e) {
            log.error("긴급 파일 저장도 실패: {}", e.getMessage());
        }
    }
}
