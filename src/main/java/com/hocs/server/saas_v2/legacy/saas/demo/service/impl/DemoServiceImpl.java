package com.hocs.server.saas_v2.legacy.saas.demo.service.impl;

import com.hocs.server.code_resolver.legacy.extractor.domain.APISourceDependencyInfo;
import com.hocs.server.code_resolver.legacy.extractor.domain.ClientProjectType;
import com.hocs.server.code_resolver.legacy.extractor.service.APISourceDependencyService;
import com.hocs.server.openai.domain.input.APIMetadata;
import com.hocs.server.openai.service.GenerateOasFacadeService;
import com.hocs.server.openai.util.HttpClient;
import com.hocs.server.openai.util.MemoryProcessPercentage;
import com.hocs.server.saas_v2.legacy.saas.apidoc.service.impl.StaticApiDocServiceImpl;
import com.hocs.server.saas_v2.legacy.saas.demo.controller.exception.GithubCloneException;
import com.hocs.server.saas_v2.legacy.saas.demo.mapper.APISourceDependencyInfoToAPIEndpoint;
import com.hocs.server.saas_v2.legacy.saas.demo.service.DemoFacadeService;
import com.hocs.server.saas_v2.legacy.saas.user.gitapi.domin.GitRepo;
import com.hocs.server.saas_v2.legacy.saas.user.gitapi.service.GitHubFacadeService;
import com.hocs.server.saas_v2.legacy.saas.user.oauth.dto.FilesData;
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

		List<APIMetadata> apiMetadata = APISourceDependencyInfoToAPIEndpoint
			.mapToAPIEndpoint(apiSourceDependencyInfo);

		llmService.generate(userId, apiMetadata,clonedDir.toFile());
		MemoryProcessPercentage.clear(userId);

		HttpClient.toSaas(clonedDir.toFile(), userId);
		List<FilesData> htmlFiles = staticApiDocServiceImpl.findApiListByUserId(userId);

		model.addAttribute("htmlFiles", htmlFiles);

		String response = HttpClient.findHtmlRequest(htmlFiles.get(0).getFilePath());
		model.addAttribute("content", response);
	}



}
