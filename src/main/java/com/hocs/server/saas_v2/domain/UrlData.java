package com.hocs.server.saas_v2.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.util.Strings;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class UrlData {
	private String cloneUrl;
	private String ownerName;
	private String repoName;

	public String getCloneUrl(){
		return cloneUrl.endsWith(".git") ? cloneUrl : Strings.concat(cloneUrl,".git");
	}

	public static UrlData of(String cloneUrl) {
		if (cloneUrl.endsWith(".git")) {
			cloneUrl = cloneUrl.substring(0, cloneUrl.indexOf(".git"));
		}

		String[] urlParts = cloneUrl.split("/");
		return new UrlData(cloneUrl,urlParts[3],urlParts[4]);
	}

}