package com.hocs.server.saas_v2;

import java.nio.file.Path;
import java.util.List;

public interface GitAPI {
	List<GitRepository> findRepositories(String accessToken);
	void gitClone(GitRepository repo, Path path);
	String getDefaultBranchName(GitRepository repo);
}
