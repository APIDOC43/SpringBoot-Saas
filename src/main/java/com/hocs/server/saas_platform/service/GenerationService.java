package com.hocs.server.saas_platform.service;

import static com.hocs.server.common.service.GenerateIdempotencyKeyService.*;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.pipline_orchestrator.ratelimit.TaskContext;
import com.hocs.server.pipline_orchestrator.ratelimit.TaskContextStore;
import com.hocs.server.saas_platform.controller.dto.ProgressDto;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerationService {

	private final ProjectMetaDataService projectMetaDataService;

	public ProgressDto getProgressList(Long metadataId) {
		//TODO 리팩토링 필요
		ProgressDto progressDto = new ProgressDto();

		ProjectMetaData metaData = projectMetaDataService.findMetadataById(metadataId);
		TaskContext taskContext = TaskContextStore.get(generateIdempotencyKey(metaData.getGitRepoData(), metadataId));
		if (taskContext != null) {
			int taskSize = taskContext.getTaskSize();
			AtomicInteger completeCount = taskContext.getCompleteCount();
			progressDto.setStatus(taskSize + "/" + completeCount);
			log.info("{}/{}",taskSize, completeCount);
		} else {
			progressDto.setStatus("CREATED");
		}

		return progressDto;
	}
}
