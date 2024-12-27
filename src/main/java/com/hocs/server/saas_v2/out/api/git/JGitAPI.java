package com.hocs.server.saas_v2.out.api.git;

import static com.hocs.server.saas_v2.ErrorCode.GIT_REPOSITORY_IS_EMPTY;

import com.hocs.server.saas_v2.CustomException;
import com.hocs.server.saas_v2.GitAPI;
import com.hocs.server.saas_v2.GitRepository;
import com.hocs.server.saas_v2.out.api.git.dto.RepositoryResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class JGitAPI implements GitAPI {

	private static final String API_URL = "https://api.github.com";

	@Override
	public List<GitRepository> findRepositories(String accessToken) {
		// HTTP 헤더 설정
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		HttpEntity<?> entity = new HttpEntity<>(headers);

		// API 호출
		ResponseEntity<RepositoryResponse[]> response = new RestTemplate().exchange(
			API_URL + "/user/repos",
			HttpMethod.GET,
			entity,
			RepositoryResponse[].class
		);

		// 응답 반환
		RepositoryResponse[] body = response.getBody();

		if (body == null) {
			throw new CustomException(GIT_REPOSITORY_IS_EMPTY);
		}

		return Arrays.stream(body)
			.map(
				m -> new GitRepository(Strings.concat(m.nodeId(), m.name()), m.svnUrl(),
					m.fullName())
			).collect(Collectors.toList());

	}

	@Override
	public void gitClone(GitRepository repo, Path path) {
		try {
			Git.cloneRepository()
				.setURI(repo.getUrl())
				.setDirectory(path.toFile())
				.call();
		} catch (GitAPIException gitAPIException) {
			gitAPIException.printStackTrace();
			log.error("JGitAPI.gitClone throw GitAPIException");
		}
	}

	@Override
	public String getDefaultBranchName(GitRepository repo) {
		return null;
	}
}
