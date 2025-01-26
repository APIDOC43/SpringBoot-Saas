package com.hocs.server.saas_v2.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.logging.log4j.util.Strings;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
@ToString
public class GitRepoData {

	private String cloneUrl;
	private String ownerName;
	private String repoName;

	public String getCloneUrl(){
		return cloneUrl.endsWith(".git") ? cloneUrl : Strings.concat(cloneUrl,".git");
	}

	private GitRepoData(String cloneUrl, String ownerName, String repoName) {
		this.cloneUrl = cloneUrl;
		this.ownerName = ownerName;
		this.repoName = repoName;
	}

	public static GitRepoData of(String cloneUrl) {
		if (cloneUrl.endsWith(".git")) {
			cloneUrl = cloneUrl.substring(0, cloneUrl.indexOf(".git"));
		}

		String[] urlParts = cloneUrl.split("/");
		return new GitRepoData(cloneUrl,urlParts[3],urlParts[4]);
	}

}