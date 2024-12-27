package com.hocs.server.saas_v2.out;

import static org.junit.jupiter.api.Assertions.*;

import com.hocs.server.saas_v2.domain.GitRepository;
import com.hocs.server.saas_v2.out.git.adapter.JGitAPI;
import java.util.List;
import org.junit.jupiter.api.Test;

class JGitAPITest {
	String accessToken = "YOUR ACCESS KEY OR Ignore";
	@Test
	public void findRepositories(){
		JGitAPI jGitAPI = new JGitAPI();
		List<GitRepository> repositories =
			jGitAPI.findRepositories(accessToken);

		assertFalse(repositories.isEmpty());
	}
}