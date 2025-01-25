package com.hocs.server.saas_v2.legacy.saas.user.gitapi.domin;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;



@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Slf4j
public class GitRepo {
	private final String url;
	private final String owner;
	private final String repoName;

	public static GitRepo of(String repoUrl) {
		if (repoUrl.endsWith(".git")) {
			repoUrl = repoUrl.substring(0, repoUrl.indexOf(".git"));
		}

		String[] urlParts = repoUrl.split("/");
		return new GitRepo(repoUrl,urlParts[3],urlParts[4]);
	}
}
