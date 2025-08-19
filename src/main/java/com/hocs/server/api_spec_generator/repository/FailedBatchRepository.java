package com.hocs.server.api_spec_generator.repository;

import com.hocs.server.api_spec_generator.domain.FailedBatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FailedBatchRepository extends JpaRepository<FailedBatchEntity, Long> {
}
