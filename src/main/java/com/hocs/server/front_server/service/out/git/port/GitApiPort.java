package com.hocs.server.front_server.service.out.git.port;

import com.hocs.server.front_server.domain.GitRepository;
import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.front_server.domain.GitRepoData;
import java.nio.file.Path;
import java.util.List;


public interface GitApiPort {
	List<GitRepository> findRepositories(String accessToken);
	String getDefaultBranchName(GitRepoData gitRepoData);
	ClientProjectPath gitClone(GitRepoData gitRepoData, Path path);
}
