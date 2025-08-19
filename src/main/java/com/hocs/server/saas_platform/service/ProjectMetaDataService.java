package com.hocs.server.saas_platform.service;

import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.common.domain.CodingLanguage;
import com.hocs.server.common.domain.ProjectFramework;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.saas_platform.common.exception.CustomException;
import com.hocs.server.saas_platform.common.exception.ErrorCode;
import com.hocs.server.saas_platform.domain.GitRepoData;
import com.hocs.server.saas_platform.repository.ClientProjectMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectMetaDataService {

	private final ClientProjectMetadataRepository clientProjectMetadataRepository;
	
	@Transactional
	public Long saveProjectMetaData(CodingLanguage language , ProjectFramework projectFramework, String coreSrcRootPath, GitRepoData gitRepoData,
		ClientProjectPath path) {
		ProjectMetaData projectMetaData = new ProjectMetaData(
			language,
			projectFramework,
			coreSrcRootPath,
			gitRepoData,
			path
		);

		//metadata save
		ProjectMetaData save = clientProjectMetadataRepository.save(projectMetaData);
		return save.getId();
	}

	// 테스트 호환성을 위한 메소드
	public ProjectMetaData createProjectMetaData(GitRepoData gitRepoData, Path projectRootPath, Path clonePath, CodingLanguage codingLanguage, ProjectFramework projectFramework) {
		if (gitRepoData == null) {
			throw new CustomException(ErrorCode.INVALID_REQUEST_EXCEPTION);
		}
		if (projectRootPath == null) {
			throw new CustomException(ErrorCode.INVALID_REQUEST_EXCEPTION);
		}
		if (clonePath == null) {
			throw new CustomException(ErrorCode.INVALID_REQUEST_EXCEPTION);
		}
		if (codingLanguage == null) {
			throw new CustomException(ErrorCode.INVALID_REQUEST_EXCEPTION);
		}
		
		return ProjectMetaData.builder()
			.gitRepoData(gitRepoData)
			.projectRootPath(ClientProjectPath.of(projectRootPath))
			.codingLanguage(codingLanguage)
			.projectFramework(projectFramework)
			.srcRootPath(clonePath.toString())
			.build();
	}

	// 테스트 호환성을 위한 메소드
	public ProjectMetaData save(ProjectMetaData metaData) {
		return clientProjectMetadataRepository.save(metaData);
	}

	public ProjectMetaData findMetadataById(long metadataId) {
		return clientProjectMetadataRepository.findById(metadataId)
			.orElseThrow(() -> {
				log.info("ProjectMetaDataService.findMetadataById NOT_FOUND_EXCEPTION");
				return new CustomException(ErrorCode.NOT_FOUND_EXCEPTION);
			});
	}
}