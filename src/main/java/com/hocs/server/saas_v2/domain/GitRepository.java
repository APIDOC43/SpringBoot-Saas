package com.hocs.server.saas_v2.domain;

import lombok.Getter;

@Getter
public class GitRepository {
	private final GitRepoData gitRepoData;

	public GitRepository(String cloneUrl) {
		this.gitRepoData = GitRepoData.of(cloneUrl);
	}
}
