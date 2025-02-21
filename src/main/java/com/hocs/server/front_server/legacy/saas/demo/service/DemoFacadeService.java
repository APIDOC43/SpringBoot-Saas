package com.hocs.server.front_server.legacy.saas.demo.service;

import com.hocs.server.front_server.legacy.saas.user.gitapi.domin.GitRepo;
import org.springframework.ui.Model;

public interface DemoFacadeService {
	void
	generateApiDoc(GitRepo gitRepo, String userId, Model model) throws Exception;
}
