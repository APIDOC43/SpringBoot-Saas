package com.hocs.server.saas_v2.out.git.port;

import com.hocs.server.saas_v2.domain.GitRepository;
import java.nio.file.Path;
import java.util.List;

public interface GitApiPort {
	List<GitRepository> findRepositories(String accessToken);
	void gitClone(GitRepository repo, Path path);
	String getDefaultBranchName(GitRepository repo);
}
