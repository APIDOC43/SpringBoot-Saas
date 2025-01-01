package com.hocs.server.saas_v2.service;

import com.hocs.server.saas_v2.domain.CodingLanguage;
import com.hocs.server.saas_v2.domain.ProjectFramework;
import com.hocs.server.saas_v2.domain.ProjectMetaData;
import com.hocs.server.saas_v2.repository.ClientProjectMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectMetaDataService {

	private final ClientProjectMetadataRepository clientProjectMetadataRepository;
	@Transactional
	public Long saveProjectMetaData(CodingLanguage language , ProjectFramework projectFramework, String coreSrcRootPath, String gitCloneUrl) {
		ProjectMetaData projectMetaData = new ProjectMetaData(
			language,
			projectFramework,
			coreSrcRootPath,
			gitCloneUrl
		);

		//metadata save
		ProjectMetaData save = clientProjectMetadataRepository.save(projectMetaData);
		return save.getId();
	}
}