package com.hocs.server.saas_v2.service;

import com.hocs.server.saas_v2.common.exception.CustomException;
import com.hocs.server.saas_v2.common.exception.ErrorCode;
import com.hocs.server.saas_v2.domain.ClientProjectPath;
import com.hocs.server.saas_v2.domain.CodingLanguage;
import com.hocs.server.saas_v2.domain.ProjectFramework;
import com.hocs.server.saas_v2.domain.ProjectMetaData;
import com.hocs.server.saas_v2.repository.ClientProjectMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectMetaDataService {

	private final ClientProjectMetadataRepository clientProjectMetadataRepository;
	@Transactional
	public Long saveProjectMetaData(CodingLanguage language , ProjectFramework projectFramework, String coreSrcRootPath, String gitCloneUrl,
		ClientProjectPath path) {
		ProjectMetaData projectMetaData = new ProjectMetaData(
			language,
			projectFramework,
			coreSrcRootPath,
			gitCloneUrl,
			path
		);

		//metadata save
		ProjectMetaData save = clientProjectMetadataRepository.save(projectMetaData);
		return save.getId();
	}

	public ProjectMetaData findMetadataById(long metadataId) {
		return clientProjectMetadataRepository.findById(metadataId)
			.orElseThrow(() -> {
				log.info("ProjectMetaDataService.findMetadataById NOT_FOUND_EXCEPTION");
				return new CustomException(ErrorCode.NOT_FOUND_EXCEPTION);
			});
	}
}