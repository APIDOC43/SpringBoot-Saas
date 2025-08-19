package com.hocs.server.code_parser.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.MethodInformation;
import com.hocs.server.pipline_orchestrator.domain.ControllerFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApiExcludeServiceTest {

    private ApiExcludeService apiExcludeService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        apiExcludeService = new ApiExcludeService();
    }

    @Test
    @DisplayName("컨트롤러 파일에서 API 정보를 정상적으로 추출한다")
    void shouldExtractApiInfoFromControllerFiles() throws IOException {
        // Given
        String controllerContent = """
            package com.example.controller;
            
            import org.springframework.web.bind.annotation.*;
            
            @RestController
            @RequestMapping("/api/users")
            public class UserController {
                
                @GetMapping("/{id}")
                public String getUser(@PathVariable Long id) {
                    return "user";
                }
                
                @PostMapping
                public String createUser(@RequestBody String user) {
                    return "created";
                }
                
                @DeleteMapping("/{id}")
                public void deleteUser(@PathVariable Long id) {
                    // delete logic
                }
            }
            """;

        File controllerFile = createTempFile("UserController.java", controllerContent);
        List<File> controllerFiles = Arrays.asList(controllerFile);

        // When
        Map<ControllerFile, List<ApiInfo>> result = apiExcludeService.excludeApi(controllerFiles, null);

        // Then
        assertThat(result).hasSize(1);
        
        ControllerFile expectedControllerFile = new ControllerFile(controllerFile.getPath());
        assertThat(result).containsKey(expectedControllerFile);
        
        List<ApiInfo> apiInfos = result.get(expectedControllerFile);
        assertThat(apiInfos).hasSize(3);
        
        // API 정보 검증
        assertThat(apiInfos).extracting(ApiInfo::getHttpMethod)
                .containsExactlyInAnyOrder("GET", "POST", "DELETE");
        assertThat(apiInfos).extracting(ApiInfo::getEndpoint)
                .containsExactlyInAnyOrder("/api/users/{id}", "/api/users", "/api/users/{id}");
    }

    @Test
    @DisplayName("제외할 API 목록이 있을 때 해당 API를 제외한다")
    void shouldExcludeSpecifiedApis() throws IOException {
        // Given
        String controllerContent = """
            package com.example.controller;
            
            import org.springframework.web.bind.annotation.*;
            
            @RestController
            @RequestMapping("/api/users")
            public class UserController {
                
                @GetMapping("/{id}")
                public String getUser(@PathVariable Long id) {
                    return "user";
                }
                
                @PostMapping
                public String createUser(@RequestBody String user) {
                    return "created";
                }
            }
            """;

        File controllerFile = createTempFile("UserController.java", controllerContent);
        List<File> controllerFiles = Arrays.asList(controllerFile);
        
        // 제외할 API 정보 (POST /api/users 제외)
        List<ApiInfo> excludeApis = Arrays.asList(
                ApiInfo.builder()
                        .httpMethod("POST")
                        .endpoint("/api/users")
                        .methodSignature(new MethodInformation("createUser(String)")) // String signature 사용
                        .build()
        );

        // When
        Map<ControllerFile, List<ApiInfo>> result = apiExcludeService.excludeApi(controllerFiles, excludeApis);

        // Then
        assertThat(result).hasSize(1);
        
        ControllerFile expectedControllerFile = new ControllerFile(controllerFile.getPath());
        List<ApiInfo> apiInfos = result.get(expectedControllerFile);
        
        // POST API가 제외되어 GET API만 남아있어야 함
        assertThat(apiInfos).hasSize(1);
        assertThat(apiInfos.get(0).getHttpMethod()).isEqualTo("GET");
        assertThat(apiInfos.get(0).getEndpoint()).isEqualTo("/api/users/{id}");
    }

    @Test
    @DisplayName("RequestMapping이 없는 컨트롤러도 정상 처리한다")
    void shouldHandleControllerWithoutRequestMapping() throws IOException {
        // Given
        String controllerContent = """
            package com.example.controller;
            
            import org.springframework.web.bind.annotation.*;
            
            @RestController
            public class SimpleController {
                
                @GetMapping("/health")
                public String health() {
                    return "OK";
                }
            }
            """;

        File controllerFile = createTempFile("SimpleController.java", controllerContent);
        List<File> controllerFiles = Arrays.asList(controllerFile);

        // When
        Map<ControllerFile, List<ApiInfo>> result = apiExcludeService.excludeApi(controllerFiles, null);

        // Then
        assertThat(result).hasSize(1);
        
        ControllerFile expectedControllerFile = new ControllerFile(controllerFile.getPath());
        List<ApiInfo> apiInfos = result.get(expectedControllerFile);
        
        assertThat(apiInfos).hasSize(1);
        assertThat(apiInfos.get(0).getHttpMethod()).isEqualTo("GET");
        assertThat(apiInfos.get(0).getEndpoint()).isEqualTo("/health");
    }

    @Test
    @DisplayName("빈 컨트롤러 파일 목록으로 빈 결과를 반환한다")
    void shouldReturnEmptyResultForEmptyControllerFiles() {
        // Given
        List<File> emptyControllerFiles = Arrays.asList();

        // When
        Map<ControllerFile, List<ApiInfo>> result = apiExcludeService.excludeApi(emptyControllerFiles, null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 파일에 대해 예외를 발생시킨다")
    void shouldThrowExceptionForNonExistentFile() {
        // Given
        File nonExistentFile = new File("/non/existent/file.java");
        List<File> controllerFiles = Arrays.asList(nonExistentFile);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            apiExcludeService.excludeApi(controllerFiles, null);
        });
    }

    @Test
    @DisplayName("여러 컨트롤러 파일을 한 번에 처리한다")
    void shouldProcessMultipleControllerFiles() throws IOException {
        // Given
        String userControllerContent = """
            package com.example.controller;
            
            import org.springframework.web.bind.annotation.*;
            
            @RestController
            @RequestMapping("/api/users")
            public class UserController {
                
                @GetMapping
                public String getUsers() {
                    return "users";
                }
            }
            """;

        String orderControllerContent = """
            package com.example.controller;
            
            import org.springframework.web.bind.annotation.*;
            
            @RestController
            @RequestMapping("/api/orders")
            public class OrderController {
                
                @GetMapping
                public String getOrders() {
                    return "orders";
                }
                
                @PostMapping
                public String createOrder() {
                    return "created";
                }
            }
            """;

        File userController = createTempFile("UserController.java", userControllerContent);
        File orderController = createTempFile("OrderController.java", orderControllerContent);
        List<File> controllerFiles = Arrays.asList(userController, orderController);

        // When
        Map<ControllerFile, List<ApiInfo>> result = apiExcludeService.excludeApi(controllerFiles, null);

        // Then
        assertThat(result).hasSize(2);
        
        // UserController 검증
        ControllerFile userControllerFile = new ControllerFile(userController.getPath());
        assertThat(result.get(userControllerFile)).hasSize(1);
        
        // OrderController 검증
        ControllerFile orderControllerFile = new ControllerFile(orderController.getPath());
        assertThat(result.get(orderControllerFile)).hasSize(2);
    }

    private File createTempFile(String fileName, String content) throws IOException {
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, content.getBytes());
        return filePath.toFile();
    }
}
