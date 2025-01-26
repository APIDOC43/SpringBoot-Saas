package com.hocs.server.saas_v2.out;

import static org.junit.jupiter.api.Assertions.*;

import com.hocs.server.saas_v2.domain.GitRepository;
import com.hocs.server.saas_v2.service.out.git.adapter.GitApiAdapter;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

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