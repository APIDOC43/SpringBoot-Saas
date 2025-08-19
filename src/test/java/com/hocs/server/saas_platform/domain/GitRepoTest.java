package com.hocs.server.saas_platform.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GitRepo 테스트")
class GitRepoTest {

	@Test
	@DisplayName("HTTPS GitHub URL을 올바르게 파싱해야 한다")
	void shouldParseHttpsGitHubUrl() {
		// Given
		String url = "https://github.com/owner/repository.git";

		// When
		GitRepo gitRepo = GitRepo.of(url);

		// Then
		assertThat(gitRepo.getUrl()).isEqualTo("https://github.com/owner/repository");
		assertThat(gitRepo.getOwner()).isEqualTo("owner");
		assertThat(gitRepo.getRepoName()).isEqualTo("repository");
	}

	@Test
	@DisplayName("HTTPS GitHub URL에서 .git 확장자가 없어도 올바르게 파싱해야 한다")
	void shouldParseHttpsGitHubUrlWithoutGitExtension() {
		// Given
		String url = "https://github.com/owner/repository";

		// When
		GitRepo gitRepo = GitRepo.of(url);

		// Then
		assertThat(gitRepo.getUrl()).isEqualTo("https://github.com/owner/repository");
		assertThat(gitRepo.getOwner()).isEqualTo("owner");
		assertThat(gitRepo.getRepoName()).isEqualTo("repository");
	}

	@Test
	@DisplayName("SSH GitHub URL을 올바르게 파싱해야 한다")
	void shouldParseSshGitHubUrl() {
		// Given
		String url = "git@github.com:owner/repository.git";

		// When
		GitRepo gitRepo = GitRepo.of(url);

		// Then
		assertThat(gitRepo.getUrl()).isEqualTo("https://github.com/owner/repository");
		assertThat(gitRepo.getOwner()).isEqualTo("owner");
		assertThat(gitRepo.getRepoName()).isEqualTo("repository");
	}

	@Test
	@DisplayName("GitLab URL을 올바르게 파싱해야 한다")
	void shouldParseGitLabUrl() {
		// Given
		String url = "https://gitlab.com/owner/project.git";

		// When
		GitRepo gitRepo = GitRepo.of(url);

		// Then
		assertThat(gitRepo.getUrl()).isEqualTo("https://gitlab.com/owner/project");
		assertThat(gitRepo.getOwner()).isEqualTo("owner");
		assertThat(gitRepo.getRepoName()).isEqualTo("project");
	}

	@Test
	@DisplayName("Bitbucket URL을 올바르게 파싱해야 한다")
	void shouldParseBitbucketUrl() {
		// Given
		String url = "https://bitbucket.org/owner/repository.git";

		// When
		GitRepo gitRepo = GitRepo.of(url);

		// Then
		assertThat(gitRepo.getUrl()).isEqualTo("https://bitbucket.org/owner/repository");
		assertThat(gitRepo.getOwner()).isEqualTo("owner");
		assertThat(gitRepo.getRepoName()).isEqualTo("repository");
	}

	@Test
	@DisplayName("서브 그룹이 있는 GitLab URL을 올바르게 파싱해야 한다")
	void shouldParseGitLabUrlWithSubgroup() {
		// Given
		String url = "https://gitlab.com/group/subgroup/project.git";

		// When
		GitRepo gitRepo = GitRepo.of(url);

		// Then
		assertThat(gitRepo.getUrl()).isEqualTo("https://gitlab.com/group/subgroup/project");
		assertThat(gitRepo.getOwner()).isEqualTo("subgroup");
		assertThat(gitRepo.getRepoName()).isEqualTo("project");
	}

	@Test
	@DisplayName("포트가 포함된 URL을 올바르게 파싱해야 한다")
	void shouldParseUrlWithPort() {
		// Given
		String url = "https://git.company.com:8080/owner/repository.git";

		// When
		GitRepo gitRepo = GitRepo.of(url);

		// Then
		assertThat(gitRepo.getUrl()).isEqualTo("https://git.company.com:8080/owner/repository");
		assertThat(gitRepo.getOwner()).isEqualTo("owner");
		assertThat(gitRepo.getRepoName()).isEqualTo("repository");
	}

	@Test
	@DisplayName("잘못된 형식의 URL에 대해 예외가 발생해야 한다")
	void shouldThrowExceptionForInvalidUrl() {
		// Given
		String invalidUrl = "invalid-url";

		// When & Then
		assertThatThrownBy(() -> GitRepo.of(invalidUrl))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("null URL에 대해 예외가 발생해야 한다")
	void shouldThrowExceptionForNullUrl() {
		// Given
		String nullUrl = null;

		// When & Then
		assertThatThrownBy(() -> GitRepo.of(nullUrl))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("빈 URL에 대해 예외가 발생해야 한다")
	void shouldThrowExceptionForEmptyUrl() {
		// Given
		String emptyUrl = "";

		// When & Then
		assertThatThrownBy(() -> GitRepo.of(emptyUrl))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("슬래시가 부족한 URL에 대해 예외가 발생해야 한다")
	void shouldThrowExceptionForUrlWithInsufficientSlashes() {
		// Given
		String insufficientUrl = "https://github.com/owner";

		// When & Then
		assertThatThrownBy(() -> GitRepo.of(insufficientUrl))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("쿼리 파라미터가 포함된 URL을 올바르게 파싱해야 한다")
	void shouldParseUrlWithQueryParameters() {
		// Given
		String url = "https://github.com/owner/repository.git?ref=main";

		// When
		GitRepo gitRepo = GitRepo.of(url);

		// Then
		assertThat(gitRepo.getUrl()).isEqualTo("https://github.com/owner/repository?ref=main");
		assertThat(gitRepo.getOwner()).isEqualTo("owner");
		assertThat(gitRepo.getRepoName()).isEqualTo("repository?ref=main");
	}

	@Test
	@DisplayName("대소문자가 섞인 URL을 올바르게 파싱해야 한다")
	void shouldParseMixedCaseUrl() {
		// Given
		String url = "https://GitHub.com/Owner/Repository.git";

		// When
		GitRepo gitRepo = GitRepo.of(url);

		// Then
		assertThat(gitRepo.getUrl()).isEqualTo("https://GitHub.com/Owner/Repository");
		assertThat(gitRepo.getOwner()).isEqualTo("Owner");
		assertThat(gitRepo.getRepoName()).isEqualTo("Repository");
	}

	@Test
	@DisplayName("하이픈과 언더스코어가 포함된 레포지토리 이름을 올바르게 파싱해야 한다")
	void shouldParseRepositoryNameWithHyphensAndUnderscores() {
		// Given
		String url = "https://github.com/my-owner/my_awesome-repository.git";

		// When
		GitRepo gitRepo = GitRepo.of(url);

		// Then
		assertThat(gitRepo.getUrl()).isEqualTo("https://github.com/my-owner/my_awesome-repository");
		assertThat(gitRepo.getOwner()).isEqualTo("my-owner");
		assertThat(gitRepo.getRepoName()).isEqualTo("my_awesome-repository");
	}

	@Test
	@DisplayName("숫자가 포함된 사용자명과 레포지토리명을 올바르게 파싱해야 한다")
	void shouldParseNumericNamesCorrectly() {
		// Given
		String url = "https://github.com/user123/repo456.git";

		// When
		GitRepo gitRepo = GitRepo.of(url);

		// Then
		assertThat(gitRepo.getUrl()).isEqualTo("https://github.com/user123/repo456");
		assertThat(gitRepo.getOwner()).isEqualTo("user123");
		assertThat(gitRepo.getRepoName()).isEqualTo("repo456");
	}
}
