package com.hocs.server.saas_platform.domain;

import jakarta.persistence.Embeddable;
import lombok.*;
import org.apache.logging.log4j.util.Strings;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Embeddable
@Builder(toBuilder = true)
@ToString
public class GitRepoData {

	private String cloneUrl;
	private String ownerName;
	private String repoName;
	private String token; // 테스트 호환성을 위해 추가

	public String getCloneUrl(){
		return cloneUrl.endsWith(".git") ? cloneUrl : Strings.concat(cloneUrl,".git");
	}

	// 테스트 호환성을 위한 메소드
	public String getUrl() {
		return this.cloneUrl;
	}

	public GitRepoData(String cloneUrl, String ownerName, String repoName) {
		this.cloneUrl = cloneUrl;
		this.ownerName = ownerName;
		this.repoName = repoName;
	}

	public static GitRepoData of(String cloneUrl) {
		if (cloneUrl == null) {
			throw new IllegalArgumentException("Clone URL cannot be null");
		}
		
		if (cloneUrl.trim().isEmpty()) {
			throw new IllegalArgumentException("Clone URL cannot be empty");
		}

		// SSH URL 형식 처리 (git@github.com:owner/repo.git)
		if (cloneUrl.startsWith("git@")) {
			return parseSshUrl(cloneUrl);
		}

		// HTTPS URL 형식 처리
		return parseHttpsUrl(cloneUrl);
	}

	private static GitRepoData parseSshUrl(String cloneUrl) {
		try {
			// git@github.com:owner/repo.git 형식
			String[] parts = cloneUrl.split(":");
			if (parts.length < 2) {
				throw new IllegalArgumentException("Invalid SSH URL format");
			}
			
			String pathPart = parts[1];
			if (pathPart.endsWith(".git")) {
				pathPart = pathPart.substring(0, pathPart.length() - 4);
			}
			
			String[] pathParts = pathPart.split("/");
			if (pathParts.length < 2) {
				throw new IllegalArgumentException("Invalid SSH URL format");
			}
			
			String owner = pathParts[pathParts.length - 2];
			String repoName = pathParts[pathParts.length - 1];
			
			return new GitRepoData(cloneUrl, owner, repoName);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to parse SSH URL: " + cloneUrl, e);
		}
	}

	private static GitRepoData parseHttpsUrl(String cloneUrl) {
		try {
			String cleanUrl = cloneUrl;
			
			// .git 확장자 제거
			if (cleanUrl.endsWith(".git")) {
				cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 4);
			}

			// URL을 '/'로 분할
			String[] urlParts = cleanUrl.split("/");
			
			if (urlParts.length < 5) {
				throw new IllegalArgumentException("Invalid URL format. Expected format: https://host/owner/repo");
			}

			// 마지막 두 부분이 owner/repo
			String owner = urlParts[urlParts.length - 2];
			String repoName = urlParts[urlParts.length - 1];

			return new GitRepoData(cloneUrl, owner, repoName);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to parse HTTPS URL: " + cloneUrl, e);
		}
	}

	// Builder 클래스에 url 메소드 추가를 위한 커스텀 빌더
	public static class GitRepoDataBuilder {
		public GitRepoDataBuilder url(String url) {
			this.cloneUrl = url;
			return this;
		}
	}

}