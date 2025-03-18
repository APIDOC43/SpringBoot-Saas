package com.hocs.server.saas_platform.out;

import static org.junit.jupiter.api.Assertions.*;

import com.hocs.server.saas_platform.domain.GitRepository;
import com.hocs.server.saas_platform.service.external.git.adapter.GitApiAdapter;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("빌드 시 테스트에서 제외")
class JGitAPITest {
	String accessToken = "YOUR ACCESS KEY OR Ignore";
	@Test
	public void findRepositoriesByAccessToken(){
		GitApiAdapter jGitAPI = new GitApiAdapter();
		List<GitRepository> repositories =
			jGitAPI.findRepositories(accessToken);

		assertFalse(repositories.isEmpty());
	}
}