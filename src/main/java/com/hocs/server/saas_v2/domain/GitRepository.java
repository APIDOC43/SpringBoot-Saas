package com.hocs.server.saas_v2.domain;

import lombok.Getter;

@Getter
public class GitRepository {
	private final UrlData urlData;

	public GitRepository(String cloneUrl) {
		this.urlData = UrlData.of(cloneUrl);
	}
}
