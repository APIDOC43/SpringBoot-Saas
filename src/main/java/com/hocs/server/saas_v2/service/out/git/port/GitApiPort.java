package com.hocs.server.saas_v2.service.out.git.port;

import com.hocs.server.saas_v2.domain.GitRepository;
import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.saas_v2.domain.GitRepoData;
import java.nio.file.Path;
import java.util.List;


public interface GitApiPort {
	List<GitRepository> findRepositories(String accessToken);
	String getDefaultBranchName(GitRepoData gitRepoData);
	ClientProjectPath gitClone(GitRepoData gitRepoData, Path path);
}
