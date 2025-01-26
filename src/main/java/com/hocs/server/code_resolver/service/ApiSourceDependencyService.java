package com.hocs.server.code_resolver.service;

import com.hocs.server.code_resolver.legacy.extractor.domain.APISourceDependencyInfo;
import com.hocs.server.code_resolver.legacy.extractor.respository.mongo.APISourceDependencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApiSourceDependencyService {

	private final APISourceDependencyRepository repository;

	@Transactional
	public void save(APISourceDependencyInfo apiSourceDependencyInfo) {
		repository.save(apiSourceDependencyInfo);
	}
}
