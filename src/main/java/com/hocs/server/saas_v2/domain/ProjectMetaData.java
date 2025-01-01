package com.hocs.server.saas_v2.domain;
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
	private String gitCloneUrl;

	@Enumerated(EnumType.STRING)
	private CodingLanguage codingLanguage;

	@Enumerated(EnumType.STRING)
	private ProjectFramework projectFramework;

	public ProjectMetaData(CodingLanguage codingLanguage, ProjectFramework projectFramework, String srcRootPath,
		String gitCloneUrl) {
		this.codingLanguage = codingLanguage;
		this.projectFramework = projectFramework;
		this.srcRootPath = srcRootPath;
		this.gitCloneUrl = gitCloneUrl;
	}
}
