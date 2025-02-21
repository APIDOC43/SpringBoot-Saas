package com.hocs.server.extractor.service;

import com.hocs.server.custom_rag.legacy.extractor.service.GitApiService;
import com.hocs.server.front_server.legacy.saas.user.gitapi.domin.GitRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GitApiServiceTest {

	@Test
	public void getDefaultBranch(){
		GitApiService gitApiService = new GitApiService();
		GitRepo gitRepo = GitRepo.of("https://github.com/khoubyari/spring-boot-rest-example.git");
		String defaultBranch = gitApiService.getDefaultBranch(gitRepo);

		Assertions.assertEquals("master", defaultBranch);
	}

	@Test
	public void buildSourceCodeUrl(){
		GitApiService gitApiService = new GitApiService();
		GitRepo gitRepo = GitRepo.of("https://github.com/khoubyari/spring-boot-rest-example.git");
		String sourceCodeUrl
			= gitApiService.buildSourceCodeUrl(gitRepo, "src/main/java/com/example/demo/controller/HelloController.java");

		Assertions.assertEquals(sourceCodeUrl,"https://github.com/khoubyari/spring-boot-rest-example/blob/master//src/main/java/com/example/demo/controller/HelloController.java");
	}

}

