package com.hocs.server.extractor.service;

import com.hocs.server.code_parser.legacy.extractor.service.GitApiService;
import com.hocs.server.saas_platform.domain.GitRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class GitApiServiceTest {


	@Test
	@Disabled("이 테스트는 현재 비활성화됨")
	public void getDefaultBranch(){
		GitApiService gitApiService = new GitApiService();
		GitRepo gitRepo = GitRepo.of("https://github.com/khoubyari/spring-boot-rest-example.git");
		String defaultBranch = gitApiService.getDefaultBranch(gitRepo);

		Assertions.assertEquals("master", defaultBranch);
	}

	@Disabled("이 테스트는 현재 비활성화됨")
	@Test
	public void buildSourceCodeUrl(){
		GitApiService gitApiService = new GitApiService();
		GitRepo gitRepo = GitRepo.of("https://github.com/khoubyari/spring-boot-rest-example.git");
		String sourceCodeUrl
			= gitApiService.buildSourceCodeUrl(gitRepo, "src/main/java/com/example/demo/controller/HelloController.java");

		Assertions.assertEquals(sourceCodeUrl,"https://github.com/khoubyari/spring-boot-rest-example/blob/master//src/main/java/com/example/demo/controller/HelloController.java");
	}

}

