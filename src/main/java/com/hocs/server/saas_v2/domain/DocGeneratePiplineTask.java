package com.hocs.server.saas_v2.domain;

import com.hocs.server.common.ApiInfo;
import com.hocs.server.common.ProjectMetaData;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.DigestUtils;

@Entity
@NoArgsConstructor
@Getter
/** Not Yet **/
public class DocGeneratePiplineTask extends BaseEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String userId;
	private String requestId;
	private String gitUrl;
	private List<ApiInfo> excludeApiInfo;

	@OneToOne
	@JoinColumn(name = "project_metadata_id")
	private ProjectMetaData projectMetaData;

	public DocGeneratePiplineTask(String userId, ProjectMetaData projectMetaData, UrlData urlData) {
		this.userId = userId;
		this.requestId = generateIdempotencyKey(urlData);
		this.gitUrl = urlData.getCloneUrl();
		this.projectMetaData = projectMetaData;
	}

	private String generateIdempotencyKey(UrlData urlData) {
		String data = String.join("|", urlData.getCloneUrl(), urlData.getRepoName(), urlData.getOwnerName());
		return DigestUtils.md5DigestAsHex(data.getBytes());
	}
}
