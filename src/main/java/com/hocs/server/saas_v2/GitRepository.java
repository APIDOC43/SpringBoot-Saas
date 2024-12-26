package com.hocs.server.saas_v2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.logging.log4j.util.Strings;

@Getter
@AllArgsConstructor
public class GitRepository {
	private String id;
	private String url;
	private String name;

	public String getCloneUrl(){
		return Strings.concat(url,".git");
	}
}
