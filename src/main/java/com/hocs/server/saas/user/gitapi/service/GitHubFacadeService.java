package com.hocs.server.saas.user.gitapi.service;


import com.hocs.server.saas.user.gitapi.domin.GitRepo;
import com.hocs.server.saas.user.gitapi.port.out.PrivateRepositoryClonePort;
import com.hocs.server.saas.user.gitapi.port.out.PublicRepositoryClonePort;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubFacadeService {

	private static final String API_URL = "https://api.github.com";
	private final PublicRepositoryClonePort publicRepositoryClonePort;
	private final PrivateRepositoryClonePort privateRepositoryClonePort;

	public List<Map<String, Object>> getUserRepos(String accessToken) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		HttpEntity<?> entity = new HttpEntity<>(headers);

		ResponseEntity<List> response = restTemplate.exchange(
			API_URL + "/user/repos",
			HttpMethod.GET,
			entity,
			List.class);

		return response.getBody();
	}


	/**
	 *
	 * @param gitRepo
	 * @return 클론받은 path
	 */
	public Optional<Path> gitClone(GitRepo gitRepo) {
		return publicRepositoryClonePort.gitClone(gitRepo);
	}


	public void gitPrivateClone(OAuth2AuthenticationToken authentication) {
		privateRepositoryClonePort.gitPrivateClone(authentication);
	}
}
