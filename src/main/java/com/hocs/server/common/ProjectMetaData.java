package com.hocs.server.common;
import com.hocs.server.common.ClientProjectPath;
import com.hocs.server.common.CodingLanguage;
import com.hocs.server.common.ProjectFramework;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.nio.file.Path;
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

	@Embedded
	private ClientProjectPath projectRootPath;

	@Enumerated(EnumType.STRING)
	private CodingLanguage codingLanguage;

	@Enumerated(EnumType.STRING)
	private ProjectFramework projectFramework;

	public ProjectMetaData(CodingLanguage codingLanguage, ProjectFramework projectFramework, String srcRootPath,
		String gitCloneUrl, ClientProjectPath path) {
		this.codingLanguage = codingLanguage;
		this.projectFramework = projectFramework;
		this.srcRootPath = srcRootPath;
		this.gitCloneUrl = gitCloneUrl;
		this.projectRootPath = path;
	}



}
