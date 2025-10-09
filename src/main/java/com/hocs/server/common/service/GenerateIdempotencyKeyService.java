package com.hocs.server.common.service;

import org.springframework.util.DigestUtils;

import com.hocs.server.saas_platform.domain.GitRepoData;

import java.util.UUID;

public class GenerateIdempotencyKeyService {

	public static String generateIdempotencyKey(GitRepoData gitRepoData, Long projectMetadataId) {
		String data = String.join("|", gitRepoData.getCloneUrl(), gitRepoData.getRepoName(), gitRepoData.getOwnerName(), projectMetadataId.toString());
		return DigestUtils.md5DigestAsHex(data.getBytes());
	}
}
