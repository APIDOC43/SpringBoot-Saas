package com.hocs.server.code_parser.extractor;

import static org.junit.jupiter.api.Assertions.*;

import com.hocs.server.code_parser.core.config.GlobalJavaParser;
import com.hocs.server.code_parser.service.ApiExcludeService;
import com.hocs.server.pipline_orchestrator.domain.ControllerFile;
import com.hocs.server.common.domain.ApiInfo;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;


public class ApiInfoInPiplineExtractorServiceTest {
	private GlobalJavaParser globalJavaParser = new GlobalJavaParser();

	private final ApiExcludeService apiExcludeService = new ApiExcludeService(globalJavaParser);

	private File sampleControllerFile;

	@BeforeEach
	public void setUp() throws Exception {
		// 테스트에 사용할 임시 Controller 파일 생성
		String controllerContent = """
                package com.example;

                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.RequestMapping;
                import org.springframework.web.bind.annotation.RestController;

                @RestController
                @RequestMapping("/api")
                public class SampleController {

                    @GetMapping("/test")
                    public String testEndpoint(int a, int b) {
                        return "Hello, World!";
                    }

                    @GetMapping("/another")
                    public String anotherEndpoint() {
                        return "Another Endpoint!";
                    }
                }
                """;

		// 임시 파일 작성
		sampleControllerFile = File.createTempFile("SampleController", ".java");
		Files.write(Paths.get(sampleControllerFile.toURI()), controllerContent.getBytes());
	}

	@Test
	public void testExtractApiInfo() {
		// Given
		List<File> controllers = List.of(sampleControllerFile);

		// When
		Map<ControllerFile, List<ApiInfo>> result = apiExcludeService.excludeApi(controllers,new ArrayList<>());

		// Then
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(2, result.get(new ControllerFile(sampleControllerFile.getPath())).size());

		ControllerFile controllerFile = result.keySet().iterator().next();
		assertEquals(sampleControllerFile.getPath(), controllerFile.getPath().toString());
		assertEquals(sampleControllerFile.getName(), controllerFile.getClassName()+".java");

		List<ApiInfo> apiInfos = result.get(controllerFile);
		assertNotNull(apiInfos);
		assertEquals(2, apiInfos.size());

		ApiInfo firstApiInfo = apiInfos.get(0);
		assertEquals("GET", firstApiInfo.getHttpMethod());
		assertEquals("/api/test", firstApiInfo.getEndpoint());
		assertEquals("testEndpoint(int, int)", firstApiInfo.getMethodSignature().getSignature());

		ApiInfo secondApiInfo = apiInfos.get(1);
		assertEquals("GET", secondApiInfo.getHttpMethod());
		assertEquals("/api/another", secondApiInfo.getEndpoint());
		assertEquals("anotherEndpoint()", secondApiInfo.getMethodSignature().getSignature());
	}
}
