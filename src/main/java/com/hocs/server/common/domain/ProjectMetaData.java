package com.hocs.server.common.domain;
import com.hocs.server.saas_platform.domain.GitRepoData;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class ProjectMetaData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String srcRootPath;

	@Embedded
	private GitRepoData gitRepoData;

	@Embedded
	private ClientProjectPath projectRootPath;

	@Enumerated(EnumType.STRING)
	private CodingLanguage codingLanguage;

	@Enumerated(EnumType.STRING)
	private ProjectFramework projectFramework;

	public ProjectMetaData(CodingLanguage codingLanguage, ProjectFramework projectFramework, String srcRootPath,
		GitRepoData gitRepoData, ClientProjectPath path) {
		this.codingLanguage = codingLanguage;
		this.projectFramework = projectFramework;
		this.srcRootPath = srcRootPath;
		this.gitRepoData = gitRepoData;
		this.projectRootPath = path;
	}



}
