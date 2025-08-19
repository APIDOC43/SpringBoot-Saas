package com.hocs.server.saas_platform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.saas_platform.domain.GitRepoData;
import com.hocs.server.saas_platform.service.external.git.port.GitApiPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("GitCloneService 테스트")
class GitCloneServiceTest {

	@Mock
	private GitApiPort gitApiPort;

	@InjectMocks
	private GitCloneService gitCloneService;

	@Captor
	private ArgumentCaptor<GitRepoData> gitRepoDataCaptor;

	@Captor
	private ArgumentCaptor<Path> pathCaptor;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(gitCloneService, "cloneDir", "/tmp/clone");
	}

	@Test
	@DisplayName("Git URL로 클론 시 GitApiPort를 통해 클론을 수행해야 한다")
	void shouldCloneRepositoryUsingGitApiPort() {
		// Given
		String gitCloneUrl = "https://github.com/test/repo.git";
		ClientProjectPath expectedPath = ClientProjectPath.of("/tmp/clone/test/repo");

		when(gitApiPort.gitClone(any(GitRepoData.class), any(Path.class)))
			.thenReturn(expectedPath);

		// When
		ClientProjectPath result = gitCloneService.gitClone(gitCloneUrl);

		// Then
		assertThat(result).isEqualTo(expectedPath);

		verify(gitApiPort).gitClone(gitRepoDataCaptor.capture(), pathCaptor.capture());

		GitRepoData capturedGitRepoData = gitRepoDataCaptor.getValue();
		assertThat(capturedGitRepoData.getCloneUrl()).isEqualTo(gitCloneUrl);

		Path capturedPath = pathCaptor.getValue();
		assertThat(capturedPath).isEqualTo(Path.of("/tmp/clone"));
	}

	@Test
	@DisplayName("SSH Git URL로 클론 시 정상적으로 처리되어야 한다")
	void shouldCloneRepositoryWithSSHUrl() {
		// Given
		String sshGitUrl = "git@github.com:test/repo.git";
		ClientProjectPath expectedPath = ClientProjectPath.of("/tmp/clone/test/repo");

		when(gitApiPort.gitClone(any(GitRepoData.class), any(Path.class)))
			.thenReturn(expectedPath);

		// When
		ClientProjectPath result = gitCloneService.gitClone(sshGitUrl);

		// Then
		assertThat(result).isEqualTo(expectedPath);

		verify(gitApiPort).gitClone(gitRepoDataCaptor.capture(), eq(Path.of("/tmp/clone")));

		GitRepoData capturedGitRepoData = gitRepoDataCaptor.getValue();
		assertThat(capturedGitRepoData.getCloneUrl()).isEqualTo(sshGitUrl);
	}

	@Test
	@DisplayName("GitLab URL로 클론 시 정상적으로 처리되어야 한다")
	void shouldCloneRepositoryWithGitLabUrl() {
		// Given
		String gitLabUrl = "https://gitlab.com/test/project.git";
		ClientProjectPath expectedPath = ClientProjectPath.of("/tmp/clone/test/project");

		when(gitApiPort.gitClone(any(GitRepoData.class), any(Path.class)))
			.thenReturn(expectedPath);

		// When
		ClientProjectPath result = gitCloneService.gitClone(gitLabUrl);

		// Then
		assertThat(result).isEqualTo(expectedPath);

		verify(gitApiPort).gitClone(gitRepoDataCaptor.capture(), eq(Path.of("/tmp/clone")));

		GitRepoData capturedGitRepoData = gitRepoDataCaptor.getValue();
		assertThat(capturedGitRepoData.getCloneUrl()).isEqualTo(gitLabUrl);
	}

	@Test
	@DisplayName("클론 디렉토리 설정이 다른 경우에도 정상적으로 동작해야 한다")
	void shouldWorkWithDifferentCloneDirectory() {
		// Given
		ReflectionTestUtils.setField(gitCloneService, "cloneDir", "/custom/path");
		String gitCloneUrl = "https://github.com/test/repo.git";
		ClientProjectPath expectedPath = ClientProjectPath.of("/custom/path/test/repo");

		when(gitApiPort.gitClone(any(GitRepoData.class), any(Path.class)))
			.thenReturn(expectedPath);

		// When
		ClientProjectPath result = gitCloneService.gitClone(gitCloneUrl);

		// Then
		assertThat(result).isEqualTo(expectedPath);

		verify(gitApiPort).gitClone(any(GitRepoData.class), eq(Path.of("/custom/path")));
	}
}
