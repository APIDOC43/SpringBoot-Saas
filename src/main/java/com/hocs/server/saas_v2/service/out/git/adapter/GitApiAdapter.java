package com.hocs.server.saas_v2.service.out.git.adapter;

import static com.hocs.server.saas_v2.common.exception.ErrorCode.GIT_CLONE_FAIL;
import static com.hocs.server.saas_v2.common.exception.ErrorCode.GIT_REPOSITORY_IS_EMPTY;

import com.hocs.server.saas_v2.common.annotation.Adapter;
import com.hocs.server.saas_v2.common.exception.CustomException;
import com.hocs.server.saas_v2.common.exception.ErrorCode;
import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.saas_v2.domain.GitRepository;
import com.hocs.server.saas_v2.domain.GitRepoData;
import com.hocs.server.saas_v2.service.out.git.adapter.dto.RepositoryResponse;
import com.hocs.server.saas_v2.service.out.git.port.GitApiPort;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import org.springframework.web.client.RestTemplate;


@Adapter
@RequiredArgsConstructor
@Slf4j
public class GitApiAdapter implements GitApiPort {
	String API_URL = "https://api.github.com";

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
				m -> new GitRepository(m.cloneUrl())
			).collect(Collectors.toList());
	}

	@Override
	public String getDefaultBranchName(GitRepoData gitRepoData) {

		String apiUrl = API_URL + "/repos/" + gitRepoData.getOwnerName() + "/" + gitRepoData.getRepoName();
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
				log.error("GET 요청 실패. 응답 코드: " + responseCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("GitApiAdapter.getDefaultBranchName Exception");
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return "main"; // 기본값 반환
	}

	@Override
	public ClientProjectPath gitClone(GitRepoData gitRepoData, Path path) {
		File folder = new File(path.toUri());

		// 폴더 생성
		if (!folder.exists()) {
			if (!folder.mkdirs()) {
				System.err.println("GitApiAdapter.gitClone throw mkdirs exception");
				throw new CustomException(ErrorCode.IO_CREATE_DIR_FAIL);
			}
		}

		File cloneFolder = cloneCommand(gitRepoData, path);
		return new ClientProjectPath(cloneFolder.toPath());
	}

	//default Jgit, extends and override this method if you want other
	protected File cloneCommand(GitRepoData gitRepoData, Path path) {
		try {

			File cloneFolder = cloneDirNamingStrategy(gitRepoData, path).toFile();
			Git.cloneRepository()
				.setURI(gitRepoData.getCloneUrl())
				.setDirectory(cloneFolder)
				.call();

			return cloneFolder;
		} catch (GitAPIException gitAPIException) {
			gitAPIException.printStackTrace();
			log.error("GitApiAdapter.gitClone throw GitAPIException");
			throw new CustomException(GIT_CLONE_FAIL);
		}
	}

	private Path cloneDirNamingStrategy(GitRepoData gitRepoData, Path path) {
		return path.resolve(gitRepoData.getRepoName() + System.currentTimeMillis());
	}
}