package com.hocs.server.saas_v2.service.out.git.port;

import com.hocs.server.saas_v2.domain.GitRepository;
import com.hocs.server.saas_v2.domain.ClientProjectPath;
import com.hocs.server.saas_v2.domain.UrlData;
import java.nio.file.Path;
import java.util.List;


public interface GitApiPort {
	List<GitRepository> findRepositories(String accessToken);
	String getDefaultBranchName(UrlData urlData);
	ClientProjectPath gitClone(UrlData urlData, Path path);
}
