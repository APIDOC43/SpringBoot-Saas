package com.hocs.server.code_parser.service;

import com.hocs.server.code_parser.domain.APIEntries;
import com.hocs.server.code_parser.repository.ClientProjectOutput;
import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.common.domain.CodingLanguage;
import com.hocs.server.common.domain.ProjectFramework;
import com.hocs.server.common.domain.SpringBootJava;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiEndpointCollectorServiceTest {

    @Mock
    private ClientProjectOutput clientProjectOutput;

    private ApiEndpointCollectorService apiEndpointCollectorService;

    @BeforeEach
    void setUp() {
        apiEndpointCollectorService = new ApiEndpointCollectorService(clientProjectOutput);
    }

    @Test
    @DisplayName("Spring Boot Java 프로젝트에서 컨트롤러 파일들을 찾아 APIEntries를 반환한다")
    void shouldFindControllerFilesForSpringBootJavaProject() {
        // given
        CodingLanguage language = CodingLanguage.JAVA;
        ProjectFramework framework = ProjectFramework.SPRING_BOOT;
        ClientProjectPath rootPath = new ClientProjectPath(Paths.get("/test/project"));

        List<Path> mockFiles = List.of(
            Paths.get("/test/project/src/main/java/com/example/UserController.java"),
            Paths.get("/test/project/src/main/java/com/example/OrderController.java"),
            Paths.get("/test/project/src/main/java/com/example/Service.java") // 이건 필터링됨
        );

        when(clientProjectOutput.findPathList(eq(rootPath.getPath().toFile()), any(SpringBootJava.class)))
            .thenReturn(mockFiles);

        // when
        APIEntries result = apiEndpointCollectorService.findControllerFiles(language, framework, rootPath);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEntries()).hasSize(2); // Controller 파일만 필터링됨
        assertThat(result.getEntries().get(0).getPath().getFileName().toString()).isEqualTo("UserController.java");
        assertThat(result.getEntries().get(1).getPath().getFileName().toString()).isEqualTo("OrderController.java");
    }

    @Test
    @DisplayName("빈 파일 리스트가 반환되면 빈 APIEntries를 반환한다")
    void shouldReturnEmptyAPIEntriesWhenNoFilesFound() {
        // given
        CodingLanguage language = CodingLanguage.JAVA;
        ProjectFramework framework = ProjectFramework.SPRING_BOOT;
        ClientProjectPath rootPath = new ClientProjectPath(Paths.get("/test/empty-project"));

        when(clientProjectOutput.findPathList(eq(rootPath.getPath().toFile()), any(SpringBootJava.class)))
            .thenReturn(List.of());

        // when
        APIEntries result = apiEndpointCollectorService.findControllerFiles(language, framework, rootPath);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEntries()).isEmpty();
    }

    @Test
    @DisplayName("API 엔트리가 아닌 파일들은 필터링된다")
    void shouldFilterOutNonApiEntryFiles() {
        // given
        CodingLanguage language = CodingLanguage.JAVA;
        ProjectFramework framework = ProjectFramework.SPRING_BOOT;
        ClientProjectPath rootPath = new ClientProjectPath(Paths.get("/test/project"));

        List<Path> mockFiles = List.of(
            Paths.get("/test/project/src/main/java/com/example/config/Config.java"),
            Paths.get("/test/project/src/main/java/com/example/service/UserService.java"),
            Paths.get("/test/project/src/main/java/com/example/entity/User.java")
        );

        when(clientProjectOutput.findPathList(eq(rootPath.getPath().toFile()), any(SpringBootJava.class)))
            .thenReturn(mockFiles);

        // when
        APIEntries result = apiEndpointCollectorService.findControllerFiles(language, framework, rootPath);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEntries()).isEmpty(); // 모든 파일이 필터링됨
    }
}
