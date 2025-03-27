package com.hocs.server.extractor.service;

import com.hocs.server.saas_platform.domain.GitRepo;
import com.hocs.server.saas_platform.domain.GitRepoData;
import com.hocs.server.saas_platform.service.external.git.adapter.GitApiAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class GitApiServiceTest {


	@Test
	@Disabled("이 테스트는 현재 비활성화됨")
	public void getDefaultBranch(){
		GitApiAdapter gitApiService = new GitApiAdapter();
		GitRepoData gitRepo = GitRepoData.of("https://github.com/khoubyari/spring-boot-rest-example.git");
		String defaultBranch = gitApiService.getDefaultBranchName(gitRepo);

		Assertions.assertEquals("master", defaultBranch);
	}

}

