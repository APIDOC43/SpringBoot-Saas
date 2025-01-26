package com.hocs.server.saas_v2.service;

import com.hocs.server.common.domain.DocGeneratePiplineTask;
import com.hocs.server.saas_v2.repository.DocGeneratePiplineTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocGeneratePiplineTaskService {

	private final DocGeneratePiplineTaskRepository repository;


	@Transactional
	public void save(DocGeneratePiplineTask docGeneratePiplineTask) {
		repository.save(docGeneratePiplineTask);
	}
}
