package com.hocs.server.saas_platform.service.external.git.adapter;

import com.hocs.server.saas_platform.service.external.git.port.PrivateRepositoryClonePort;
import java.io.File;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PrivateRepositoryClone  implements PrivateRepositoryClonePort {

	private final OAuth2AuthorizedClientService authorizedClientService;
	public void gitPrivateClone(OAuth2AuthenticationToken authentication) {
		OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
			authentication.getAuthorizedClientRegistrationId(),
			authentication.getName());

		// Access token 받아오기
		String accessToken = client.getAccessToken().getTokenValue();

		// GitHub API를 통해 installation id 가져오기
		String installationId = fetchInstallationId(accessToken);

		// Installation Access Token 발급 받기
		String installationAccessToken = getInstallationAccessToken(installationId);

		// Clone Repository
		cloneRepository(installationAccessToken, "owner/repo");
	}

	private String fetchInstallationId(String accessToken) {
		// GitHub API 요청을 통해 installation_id 받아오기
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken);
		headers.set("Accept", "application/vnd.github.v3+json");

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(
			"https://api.github.com/user/installations",
			HttpMethod.GET,
			entity,
			String.class
		);

		// JSON 응답에서 installation_id 추출
		// 실제 응답 데이터에서 installation ID를 파싱해야 합니다.
		String installationId = parseInstallationId(response.getBody());
		return installationId;
	}



	private String getInstallationAccessToken(String installationId) {
		// GitHub API를 통해 installation access token 요청
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + "YOUR_APP_PRIVATE_KEY"); // JWT 인증
		headers.set("Accept", "application/vnd.github.v3+json");

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(
			"https://api.github.com/app/installations/" + installationId + "/access_tokens",
			HttpMethod.POST,
			entity,
			String.class
		);

		// JSON 응답에서 access token 추출
		String accessToken = parseAccessToken(response.getBody());
		return accessToken;
	}

	private void cloneRepository(String accessToken, String repositoryUrl) {
		String cloneUrl = "https://x-access-token:" + accessToken + "@github.com/" + repositoryUrl + ".git";

		try {
			Git.cloneRepository()
				.setURI(cloneUrl)
				.setDirectory(new File("/path/to/clone/repo"))
				.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String parseInstallationId(String jsonResponse) {
		// JSON 파싱 로직 추가
		// installation_id 추출
		return "installation_id_value";
	}

	private String parseAccessToken(String jsonResponse) {
		// JSON 파싱 로직 추가
		// access_token 추출
		return "access_token_value";
	}
}
