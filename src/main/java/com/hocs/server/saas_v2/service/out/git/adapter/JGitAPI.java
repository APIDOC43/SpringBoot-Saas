package com.hocs.server.saas_v2.service.out.git.adapter;

import static com.hocs.server.saas_v2.common.exception.ErrorCode.GIT_REPOSITORY_IS_EMPTY;

import com.hocs.server.saas_v2.common.exception.CustomException;
import com.hocs.server.saas_v2.domain.GitRepository;
import com.hocs.server.saas_v2.service.out.git.adapter.dto.RepositoryResponse;
import com.hocs.server.saas_v2.service.out.git.port.GitApiPort;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class JGitAPI implements GitApiPort {

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
					m.fullName(), m.owner().login())
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
		String apiUrl = API_URL + "/repos/" + repo.getOwnerName() + "/" + repo.getName();
		HttpURLConnection connection = null;

		try {
			// URL 연결 설정
			URL url = new URL(apiUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");

			// 응답 코드 확인
			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				// 응답 데이터 읽기
				try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(connection.getInputStream()))) {
					StringBuilder response = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						response.append(line);
					}

					// JSON 파싱 및 기본 브랜치 반환
					JSONObject json = new JSONObject(response.toString());
					return json.optString("default_branch", "main");
				}
			} else {
				System.err.println("GET 요청 실패. 응답 코드: " + responseCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("JGitAPI.getDefaultBranchName Exception");
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return "main"; // 기본값 반환
	}
}
