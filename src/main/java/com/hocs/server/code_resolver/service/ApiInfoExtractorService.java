package com.hocs.server.code_resolver.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.hocs.server.code_resolver.common.EndpointPathUtil;
import com.hocs.server.api_doc_pipline.domain.ControllerFile;
import com.hocs.server.code_resolver.legacy.extractor.domain.ApiEndpoint;
import com.hocs.server.common.ApiInfo;
import com.hocs.server.common.MethodInformation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApiInfoExtractorService {

	public Map<ControllerFile, List<ApiInfo>> extractApiInfo(List<File> controllerFiles) {
		Map<ControllerFile, List<ApiInfo>> apiInfos = new HashMap<>();


		for (File controller : controllerFiles) {
			String srcContent = null;
			try {
				srcContent = new String(Files.readAllBytes(Paths.get(controller.toURI())));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			CompilationUnit srcContentUnit = StaticJavaParser.parse(srcContent);
			TypeDeclaration<?> classDeclaration = srcContentUnit.getType(0);
			String baseUrl = EndpointPathUtil.findRequestMappingValue(classDeclaration);
			for (MethodDeclaration method : classDeclaration.getMethods()) {
				ApiEndpoint apiEndpoint = EndpointPathUtil.generateApiEndpoint(baseUrl, method);

				List<ApiInfo> orDefault = apiInfos.getOrDefault(new ControllerFile(controller.getPath()),
					new ArrayList<>());

				orDefault.add(new ApiInfo(apiEndpoint.getMethod(), apiEndpoint.getApi(),new MethodInformation(method)));
				apiInfos.put(new ControllerFile(controller.getPath()),orDefault);
			}
		}

		return apiInfos;
	}
}
