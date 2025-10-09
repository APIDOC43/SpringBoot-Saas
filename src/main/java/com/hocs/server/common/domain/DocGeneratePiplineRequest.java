package com.hocs.server.common.domain;

import static com.hocs.server.common.service.GenerateIdempotencyKeyService.*;

import com.hocs.server.saas_platform.domain.BaseEntity;
import com.hocs.server.saas_platform.domain.GitRepoData;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.*;
import org.springframework.util.DigestUtils;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@AllArgsConstructor
public class DocGeneratePiplineRequest extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String userId;
	private String requestId;

	@OneToOne
	@JoinColumn(name = "project_metadata_id")
	private ProjectMetaData projectMetaData;

	public DocGeneratePiplineRequest(String userId, ProjectMetaData projectMetaData) {
		this.userId = userId;
		this.requestId = generateIdempotencyKey(projectMetaData.getGitRepoData(), projectMetaData.getId());
		this.projectMetaData = projectMetaData;
	}
}
