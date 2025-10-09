package com.hocs.server.saas_platform.service;

import com.hocs.server.common.domain.DocGeneratePiplineRequest;
import com.hocs.server.saas_platform.repository.DocGeneratePiplineTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocGeneratePiplineTaskService {

	private final DocGeneratePiplineTaskRepository repository;

	@Transactional
	public void save(DocGeneratePiplineRequest docGeneratePiplineRequest) {
		repository.save(docGeneratePiplineRequest);
	}
}
