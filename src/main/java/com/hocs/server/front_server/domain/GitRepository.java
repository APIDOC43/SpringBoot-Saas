package com.hocs.server.front_server.domain;

import lombok.Getter;

@Getter
public class GitRepository {
	private final GitRepoData gitRepoData;

	public GitRepository(String cloneUrl) {
		this.gitRepoData = GitRepoData.of(cloneUrl);
	}
}
