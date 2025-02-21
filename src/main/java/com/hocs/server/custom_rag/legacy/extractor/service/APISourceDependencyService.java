package com.hocs.server.custom_rag.legacy.extractor.service;

import com.hocs.server.custom_rag.legacy.extractor.respository.mongo.APISourceDependencyRepository;
import com.hocs.server.custom_rag.legacy.extractor.core.client.SpringJavaApiCodeClient;
import com.hocs.server.custom_rag.legacy.extractor.domain.APISourceDependencyInfo;
import com.hocs.server.custom_rag.legacy.extractor.domain.ClientProjectType;
import com.hocs.server.saas_v2.legacy.saas.user.gitapi.domin.GitRepo;
import java.io.File;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class APISourceDependencyService {

	private final APISourceDependencyRepository repository;
	private final SpringJavaApiCodeClient springJavaApiCodeManager;


	public APISourceDependencyInfo extractApiSourceDependencyInfo(ClientProjectType clientProjectType,File PROJECT_ROOT_DIR, GitRepo gitRepo,String userId)
		throws Exception {
			Path SOURCE_ROOT = new File(PROJECT_ROOT_DIR, clientProjectType.srcRootPath()).toPath();

			APISourceDependencyInfo apiSourceDependencyInfo = springJavaApiCodeManager
				.findDependencyInfo(clientProjectType,SOURCE_ROOT, gitRepo, userId);

			repository.save(apiSourceDependencyInfo);

			return apiSourceDependencyInfo;
	}


}
