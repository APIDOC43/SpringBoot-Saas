package com.hocs.server.common.service;

import org.springframework.util.DigestUtils;

import com.hocs.server.saas_platform.domain.GitRepoData;

public class GenerateIdempotencyKeyService {

	public static String generateIdempotencyKey(GitRepoData gitRepoData) {
		String data = String.join("|", gitRepoData.getCloneUrl(), gitRepoData.getRepoName(), gitRepoData.getOwnerName());
		return DigestUtils.md5DigestAsHex(data.getBytes());
	}
}
