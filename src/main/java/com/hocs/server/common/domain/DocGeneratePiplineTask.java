package com.hocs.server.common.domain;

import com.hocs.server.saas_platform.domain.BaseEntity;
import com.hocs.server.saas_platform.domain.GitRepoData;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.DigestUtils;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
/** Not Yet **/
public class DocGeneratePiplineTask extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String userId;
	private String requestId;

	@OneToOne
	@JoinColumn(name = "project_metadata_id")
	private ProjectMetaData projectMetaData;

	public DocGeneratePiplineTask(String userId, ProjectMetaData projectMetaData) {
		this.userId = userId;
		this.requestId = generateIdempotencyKey(projectMetaData.getGitRepoData());
		this.projectMetaData = projectMetaData;
	}

	private String generateIdempotencyKey(GitRepoData gitRepoData) {
		String data = String.join("|", gitRepoData.getCloneUrl(), gitRepoData.getRepoName(), gitRepoData.getOwnerName());
		return DigestUtils.md5DigestAsHex(data.getBytes());
	}
}
