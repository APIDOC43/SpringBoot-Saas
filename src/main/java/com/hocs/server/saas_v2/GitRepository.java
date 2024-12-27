package com.hocs.server.saas_v2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.util.Strings;

@Getter
@AllArgsConstructor
@ToString
public class GitRepository {
	private String id;
	private String url;
	private String name;
	private String ownerName;

	public String getCloneUrl(){
		return Strings.concat(url,".git");
	}
}
