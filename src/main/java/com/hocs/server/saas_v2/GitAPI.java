package com.hocs.server.saas_v2;

import java.util.List;

public interface GitAPI {
	List<GitRepository> findRepositories(String accessToken);
	void gitClone(GitRepository repo);
	String getDefaultBranchName(GitRepository repo);
}
