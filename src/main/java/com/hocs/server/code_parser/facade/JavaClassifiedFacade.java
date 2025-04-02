package com.hocs.server.code_parser.facade;

import com.hocs.server.code_parser.core.config.ExtractorConfig;
import com.hocs.server.code_parser.core.dataobject.JavaClassifiedDataContainer;
import com.hocs.server.code_parser.core.dataobject.JavaClassifiedDataGenerator;
import com.hocs.server.code_parser.core.dataobject.JavaClassifiedStore;
import com.hocs.server.code_parser.core.domain.ClientProjectType;
import com.hocs.server.code_parser.core.service.SrcFileCollector;
import com.hocs.server.saas_platform.common.annotation.Facade;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Facade
@RequiredArgsConstructor
@Slf4j
public class JavaClassifiedFacade {
	private final SrcFileCollector srcFileCollector;
	private final JavaClassifiedDataGenerator javaCodeCategorizer;
	public JavaClassifiedDataContainer initJavaClassifiedDataContainer(Path clonedDir, String requestId) {
		Path SOURCE_ROOT = new File(clonedDir.toFile(),
			ClientProjectType.SPRING_JAVA.srcRootPath()).toPath();
		String SOURCE_ROOT_STR = SOURCE_ROOT.toString();

		//paser config 설정
		ExtractorConfig extractorConfig = new ExtractorConfig();
		extractorConfig.setConfig(SOURCE_ROOT_STR);

		// 모든 Java 파일의 경로를 수집합니다.
		List<File> files = srcFileCollector.collectFiles(new File(SOURCE_ROOT_STR),
			ClientProjectType.SPRING_JAVA.srcSuffix());

		// 각 타입별로 파일 경로를 매핑합니다.
		try {
			JavaClassifiedDataContainer init = javaCodeCategorizer.init(files);
			JavaClassifiedStore.put(requestId,init);
			return init;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}