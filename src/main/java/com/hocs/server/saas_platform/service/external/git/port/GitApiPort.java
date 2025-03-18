package com.hocs.server.saas_platform.service.external.git.port;

import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.saas_platform.domain.GitRepoData;
import com.hocs.server.saas_platform.domain.GitRepository;
import java.nio.file.Path;
import java.util.List;


public interface GitApiPort {
	List<GitRepository> findRepositories(String accessToken);
	String getDefaultBranchName(GitRepoData gitRepoData);
	ClientProjectPath gitClone(GitRepoData gitRepoData, Path path);
}
