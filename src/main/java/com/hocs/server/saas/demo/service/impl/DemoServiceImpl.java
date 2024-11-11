package com.hocs.server.saas.demo.service.impl;

import com.hocs.server.extractor.domain.APISourceDependencyInfo;
import com.hocs.server.extractor.domain.ClientProjectType;
import com.hocs.server.extractor.service.APISourceDependencyService;
import com.hocs.server.openai.domain.input.APIEndpoint;
import com.hocs.server.openai.service.GenerateOasFacadeService;
import com.hocs.server.openai.util.HttpClient;
import com.hocs.server.openai.util.MemoryProcessPercentage;
import com.hocs.server.saas.apidoc.service.impl.StaticApiDocServiceImpl;
import com.hocs.server.saas.demo.controller.exception.GithubCloneException;
import com.hocs.server.saas.demo.mapper.APISourceDependencyInfoToAPIEndpoint;
import com.hocs.server.saas.demo.service.DemoFacadeService;
import com.hocs.server.saas.user.gitapi.domin.GitRepo;
import com.hocs.server.saas.user.gitapi.service.GitHubFacadeService;
import com.hocs.server.saas.user.oauth.dto.FilesData;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemoServiceImpl implements DemoFacadeService {


	private final GitHubFacadeService gitHubFacadeService;
	private final GenerateOasFacadeService llmService;
	private final APISourceDependencyService apiSourceDependencyService;
	private final StaticApiDocServiceImpl staticApiDocServiceImpl;


	public void generateApiDoc(GitRepo gitRepo, String userId, Model model) throws Exception {
		MemoryProcessPercentage.clear(userId);
		MemoryProcessPercentage.save(userId,1,10);

		Path clonedDir = gitHubFacadeService.gitClone(gitRepo)
			.orElseThrow(() -> new GithubCloneException("Clone 실패"));
		log.info("clonedDir absolute path = {}",clonedDir.toAbsolutePath());

		APISourceDependencyInfo apiSourceDependencyInfo = apiSourceDependencyService.extractApiSourceDependencyInfo(
			ClientProjectType.SPRING_JAVA, clonedDir.toFile(), gitRepo, userId);

		List<APIEndpoint> apiEndpoints = APISourceDependencyInfoToAPIEndpoint
			.mapToAPIEndpoint(apiSourceDependencyInfo);

		llmService.generate(userId,apiEndpoints,clonedDir.toFile());
		MemoryProcessPercentage.clear(userId);

		HttpClient.toSaas(clonedDir.toFile(), userId);
		List<FilesData> htmlFiles = staticApiDocServiceImpl.findApiListByUserId(userId);

		model.addAttribute("htmlFiles", htmlFiles);

		String response = HttpClient.findHtmlRequest(htmlFiles.get(0).getFilePath());
		model.addAttribute("content", response);
	}



}
