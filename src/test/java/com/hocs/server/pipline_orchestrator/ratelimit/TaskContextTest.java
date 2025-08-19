package com.hocs.server.pipline_orchestrator.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.common.domain.CodingLanguage;
import com.hocs.server.common.domain.ProjectFramework;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.saas_platform.domain.GitRepoData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class TaskContextTest {

    private TaskContext taskContext;
    private ProjectMetaData projectMetaData;

    @BeforeEach
    void setUp() {
        GitRepoData gitRepoData = GitRepoData.builder()
                .cloneUrl("https://github.com/test/repo")
                .ownerName("123")
                .build();

        projectMetaData = ProjectMetaData.builder()
                .projectRootPath(new ClientProjectPath(Path.of("/test/project")))
                .codingLanguage(CodingLanguage.JAVA)
                .projectFramework(ProjectFramework.SPRINGBOOT)
                .gitRepoData(gitRepoData)
                .build();

        String[] exceptionFiles = {"Exception1.java", "Exception2.java"};

        taskContext = new TaskContext(
                "user123",
                "main",
                exceptionFiles,
                projectMetaData,
                TaskType.HEAVY,
                5
        );
    }

    @Test
    @DisplayName("TaskContext 생성 시 모든 필드가 올바르게 초기화된다")
    void shouldInitializeAllFieldsCorrectly() {
        // Then
        assertThat(taskContext.getUserId()).isEqualTo("user123");
        assertThat(taskContext.getDefaultBranchName()).isEqualTo("main");
        assertThat(taskContext.getFilenamesRelatedException()).containsExactly("Exception1.java", "Exception2.java");
        assertThat(taskContext.getProjectMetaData()).isEqualTo(projectMetaData);
        assertThat(taskContext.getTaskType()).isEqualTo(TaskType.HEAVY);
        assertThat(taskContext.getTaskSize()).isEqualTo(5);
        assertThat(taskContext.getCompleteCount().get()).isEqualTo(0);
    }

    @Test
    @DisplayName("incrementAndGetCompleteCount 호출 시 완료 카운트가 증가한다")
    void shouldIncrementCompleteCountWhenCalled() {
        // Given
        assertThat(taskContext.getCompleteCount().get()).isEqualTo(0);

        // When
        int firstIncrement = taskContext.incrementAndGetCompleteCount();
        int secondIncrement = taskContext.incrementAndGetCompleteCount();

        // Then
        assertThat(firstIncrement).isEqualTo(1);
        assertThat(secondIncrement).isEqualTo(2);
        assertThat(taskContext.getCompleteCount().get()).isEqualTo(2);
    }

    @Test
    @DisplayName("여러 스레드에서 동시에 incrementAndGetCompleteCount 호출 시 올바르게 동작한다")
    void shouldHandleConcurrentIncrementsCorrectly() throws InterruptedException {
        // Given
        int numberOfThreads = 10;
        int incrementsPerThread = 100;
        Thread[] threads = new Thread[numberOfThreads];

        // When
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    taskContext.incrementAndGetCompleteCount();
                }
            });
            threads[i].start();
        }

        // 모든 스레드가 완료될 때까지 대기
        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        assertThat(taskContext.getCompleteCount().get()).isEqualTo(numberOfThreads * incrementsPerThread);
    }

    @Test
    @DisplayName("null 예외 파일 배열로도 TaskContext 생성이 가능하다")
    void shouldCreateTaskContextWithNullExceptionFiles() {
        // Given & When
        TaskContext contextWithNullFiles = new TaskContext(
                "user456",
                "develop",
                null,
                projectMetaData,
                TaskType.FAST,
                3
        );

        // Then
        assertThat(contextWithNullFiles.getUserId()).isEqualTo("user456");
        assertThat(contextWithNullFiles.getDefaultBranchName()).isEqualTo("develop");
        assertThat(contextWithNullFiles.getFilenamesRelatedException()).isNull();
        assertThat(contextWithNullFiles.getTaskType()).isEqualTo(TaskType.FAST);
        assertThat(contextWithNullFiles.getTaskSize()).isEqualTo(3);
    }

    @Test
    @DisplayName("빈 예외 파일 배열로도 TaskContext 생성이 가능하다")
    void shouldCreateTaskContextWithEmptyExceptionFiles() {
        // Given & When
        TaskContext contextWithEmptyFiles = new TaskContext(
                "user789",
                "feature/test",
                new String[0],
                projectMetaData,
                TaskType.HEAVY,
                10
        );

        // Then
        assertThat(contextWithEmptyFiles.getUserId()).isEqualTo("user789");
        assertThat(contextWithEmptyFiles.getDefaultBranchName()).isEqualTo("feature/test");
        assertThat(contextWithEmptyFiles.getFilenamesRelatedException()).isEmpty();
        assertThat(contextWithEmptyFiles.getTaskType()).isEqualTo(TaskType.HEAVY);
        assertThat(contextWithEmptyFiles.getTaskSize()).isEqualTo(10);
    }
}
