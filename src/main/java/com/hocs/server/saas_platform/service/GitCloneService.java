package com.hocs.server.saas_platform.service;

import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.saas_platform.domain.GitRepository;
import com.hocs.server.saas_platform.service.external.git.port.GitApiPort;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitCloneService {

	private final GitApiPort gitApiPort;

	@Value("${path.clone-dir}")
	private String cloneDir;

	public ClientProjectPath gitClone(String gitCloneUrl) {
		GitRepository gitRepository = new GitRepository(gitCloneUrl);
		return gitApiPort.gitClone(gitRepository.getGitRepoData(), Path.of(cloneDir));
	}
}