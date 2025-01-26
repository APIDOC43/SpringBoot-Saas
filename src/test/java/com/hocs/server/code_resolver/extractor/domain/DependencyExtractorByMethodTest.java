package com.hocs.server.code_resolver.extractor.domain;


import static org.junit.jupiter.api.Assertions.*;

import com.hocs.server.code_resolver.domain.DependencyExtractorByMethod;
import com.hocs.server.code_resolver.legacy.extractor.core.SrcFileCollector;
import com.hocs.server.code_resolver.legacy.extractor.core.config.ExtractorConfig;
import com.hocs.server.code_resolver.legacy.extractor.core.data.JavaClassifiedDataGenerator;
import com.hocs.server.code_resolver.legacy.extractor.domain.ClientProjectType;
import com.hocs.server.common.domain.MethodInformation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hocs.server.code_resolver.legacy.extractor.core.data.JavaClassifiedDataContainer;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@SpringBootTest
@ActiveProfiles("dev")
public class DependencyExtractorByMethodTest {

	@Autowired
	private JavaClassifiedDataGenerator javaCodeCategorizer;

	@Autowired
	private SrcFileCollector srcFileCollector;

	@Autowired
	private DependencyExtractorByMethod dependencyExtractorByMethod;

	private static final String CONTROLLER_CLASS_NAME = "MyController";
	private static final String CONTROLLER_FILE_PATH = "src/main/java/com/example/MyController.java";
	private static final String CONTROLLER_FILE_CONTENT = """
            package com.example;

            import org.springframework.web.bind.annotation.GetMapping;
            import org.springframework.web.bind.annotation.RestController;
            import lombok.RequiredArgsConstructor;

            @RestController
            @RequiredArgsConstructor
            public class MyController {

                private final HelloService helloService;

                @GetMapping("/hello")
                public String sayHello() {
                    return helloService.getGreeting();
                }
            }
            """;

	private static final String SERVICE_CLASS_NAME = "HelloService";
	private static final String SERVICE_FILE_PATH = "src/main/java/com/example/HelloService.java";
	private static final String SERVICE_FILE_CONTENT = """
            package com.example;

            import org.springframework.stereotype.Service;

            @Service
            public class HelloService {

                public String getGreeting() {
                    return "Hello, World!";
                }
            }
            """;

	@BeforeEach
	void setUp() throws Exception {
		Files.createDirectories(Paths.get("src/main/java/com/example"));
		Files.writeString(Paths.get(CONTROLLER_FILE_PATH), CONTROLLER_FILE_CONTENT);
		Files.writeString(Paths.get(SERVICE_FILE_PATH), SERVICE_FILE_CONTENT);
	}

	@Test
	void testFindDependency() throws Exception {
		// Arrange
		JavaClassifiedDataContainer dataContainer = initJavaClassifiedDataContainer(Path.of(System.getProperty("user.dir")));
		dataContainer.getClassToFilePath().put(CONTROLLER_CLASS_NAME, CONTROLLER_FILE_PATH);
		dataContainer.getClassToFilePath().put(SERVICE_CLASS_NAME, SERVICE_FILE_PATH);

		// Act
		Map<MethodInformation, List<String>> result = dependencyExtractorByMethod.findDependency(
			CONTROLLER_CLASS_NAME,
			dataContainer);

		// Assert
		assertNotNull(result, "Result should not be null");
		assertEquals(1, result.size(), "There should be exactly one dependency");
		assertEquals(2, result.get(new MethodInformation("sayHello()")).size(), "There should be exactly two dependency");


		MethodInformation methodSignature = result.keySet().iterator().next();
		List<String> dependencies = result.get(methodSignature);

		assertTrue(dependencies.contains(SERVICE_FILE_PATH), "Dependencies should contain the service file path");
	}

	private JavaClassifiedDataContainer initJavaClassifiedDataContainer(Path clonedDir)
		throws IOException {
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
		return javaCodeCategorizer.init(files);
	}
}
