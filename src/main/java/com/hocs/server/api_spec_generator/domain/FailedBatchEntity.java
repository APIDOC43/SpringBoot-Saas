package com.hocs.server.api_spec_generator.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "failed_batch")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedBatchEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "original_entity_id", nullable = false)
    private String originalEntityId;
    
    @Column(name = "entity_type", nullable = false)
    private String entityType;
    
    @Lob
    @Column(name = "entity_data", nullable = false)
    private String entityData; // JSON 형태로 저장
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @Column(name = "retry_count")
    private Integer retryCount;
    
    @Column(name = "failed_at", nullable = false)
    private LocalDateTime failedAt;
    
    @Column(name = "last_error_message")
    private String lastErrorMessage;
    
    public static FailedBatchEntity createFrom(String entityId, String entityType, 
                                              String entityData, String failureReason, 
                                              Integer retryCount, String errorMessage) {
        return FailedBatchEntity.builder()
                .originalEntityId(entityId)
                .entityType(entityType)
                .entityData(entityData)
                .failureReason(failureReason)
                .retryCount(retryCount)
                .failedAt(LocalDateTime.now())
                .lastErrorMessage(errorMessage)
                .build();
    }
}
