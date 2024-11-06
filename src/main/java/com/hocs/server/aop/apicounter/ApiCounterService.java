package com.hocs.server.aop.apicounter;

import com.hocs.server.aop.apicounter.entity.ApiCounter;
import com.hocs.server.aop.apicounter.repository.ApiCounterJpaARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiCounterService {


	private final ApiCounterJpaARepository apiCounterRepository;

	@Transactional
	public void increaseCount(String endpoint) {
		ApiCounter count = getCountOrElseCreate(endpoint);
		count.plus(1);
	}

	public ApiCounter getCountOrElseCreate(String endpoint) {
		return apiCounterRepository.findById(endpoint)
			.orElseGet(() -> apiCounterRepository.save(ApiCounter.of(endpoint)));
	}
}
