package com.hocs.server.extractor.service;

import com.hocs.server.extractor.domain.APISourceDependencyInfo;
import com.hocs.server.extractor.respository.mongo.APISourceDependencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class APISourceDependencyService {

	private final APISourceDependencyRepository repository;

	@Transactional
	public void save(APISourceDependencyInfo apiSourceDependencyInfo) {
		repository.save(apiSourceDependencyInfo);
	}

	public APISourceDependencyInfo findByUserId(String userId) {
		return repository.findByUserId(userId)
			.orElseThrow(() -> new RuntimeException("User not found"));
	}


}
