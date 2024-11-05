package com.hocs.server.gitapi.domin;

import com.hocs.server.saas.user.gitapi.domin.GitRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GitRepoTest {

	@Test
	public void can_parse_git_url(){
		String repoUrl = "https://github.com/spring-projects/spring-petclinic.git";
		GitRepo gitRepo = GitRepo.of(repoUrl);

		String url = gitRepo.getUrl();
		Assertions.assertEquals("https://github.com/spring-projects/spring-petclinic",url);
	}
}