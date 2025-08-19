package com.hocs.server.saas_platform.service;

import com.hocs.server.common.domain.CodingLanguage;
import com.hocs.server.common.domain.ProjectFramework;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.saas_platform.domain.GitRepoData;
import com.hocs.server.saas_platform.repository.ClientProjectMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectMetaDataServiceTest {

    @Mock
    private ClientProjectMetadataRepository repository;

    private ProjectMetaDataService projectMetaDataService;

    @BeforeEach
    void setUp() {
        projectMetaDataService = new ProjectMetaDataService(repository);
    }

    @Test
    @DisplayName("프로젝트 메타데이터를 생성할 수 있다")
    void shouldCreateProjectMetaData() {
        // given
        GitRepoData gitRepoData = new GitRepoData(
            "https://github.com/owner/repo.git",
            "repo",
            "owner"
        );
        Path clonePath = Paths.get("/tmp/cloned/repo");
        Path projectRootPath = Paths.get("/tmp/cloned/repo/src");
        CodingLanguage language = CodingLanguage.JAVA;
        ProjectFramework framework = ProjectFramework.SPRING_BOOT;

        // when
        ProjectMetaData result = projectMetaDataService.createProjectMetaData(
            gitRepoData, clonePath, projectRootPath, language, framework
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getGitRepoData()).isEqualTo(gitRepoData);
        assertThat(result.getClonePath()).isEqualTo(clonePath);
        assertThat(result.getSrcRootPath()).isEqualTo(projectRootPath.toString());
        assertThat(result.getCodingLanguage()).isEqualTo(language);
        assertThat(result.getProjectFramework()).isEqualTo(framework);
    }

    @Test
    @DisplayName("프로젝트 메타데이터를 저장할 수 있다")
    void shouldSaveProjectMetaData() {
        // given
        ProjectMetaData metaData = createSampleProjectMetaData();
        when(repository.save(any())).thenReturn(metaData);

        // when
        ProjectMetaData result = projectMetaDataService.save(metaData);

        // then
        assertThat(result).isEqualTo(metaData);
        verify(repository).save(metaData);
    }

    @Test
    @DisplayName("null GitRepoData로 메타데이터 생성 시 예외를 발생시킨다")
    void shouldThrowExceptionForNullGitRepoData() {
        // given
        GitRepoData gitRepoData = null;
        Path clonePath = Paths.get("/tmp/cloned/repo");
        Path projectRootPath = Paths.get("/tmp/cloned/repo/src");
        CodingLanguage language = CodingLanguage.JAVA;
        ProjectFramework framework = ProjectFramework.SPRING_BOOT;

        // when & then
        assertThatThrownBy(() -> projectMetaDataService.createProjectMetaData(
            gitRepoData, clonePath, projectRootPath, language, framework
        )).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("null clonePath로 메타데이터 생성 시 예외를 발생시킨다")
    void shouldThrowExceptionForNullClonePath() {
        // given
        GitRepoData gitRepoData = new GitRepoData(
            "https://github.com/owner/repo.git",
            "repo",
            "owner"
        );
        Path clonePath = null;
        Path projectRootPath = Paths.get("/tmp/cloned/repo/src");
        CodingLanguage language = CodingLanguage.JAVA;
        ProjectFramework framework = ProjectFramework.SPRING_BOOT;

        // when & then
        assertThatThrownBy(() -> projectMetaDataService.createProjectMetaData(
            gitRepoData, clonePath, projectRootPath, language, framework
        )).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("null 언어로 메타데이터 생성 시 예외를 발생시킨다")
    void shouldThrowExceptionForNullLanguage() {
        // given
        GitRepoData gitRepoData = new GitRepoData(
            "https://github.com/owner/repo.git",
            "repo",
            "owner"
        );
        Path clonePath = Paths.get("/tmp/cloned/repo");
        Path projectRootPath = Paths.get("/tmp/cloned/repo/src");
        CodingLanguage language = null;
        ProjectFramework framework = ProjectFramework.SPRING_BOOT;

        // when & then
        assertThatThrownBy(() -> projectMetaDataService.createProjectMetaData(
            gitRepoData, clonePath, projectRootPath, language, framework
        )).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("프로젝트 프레임워크 정보가 올바르게 설정된다")
    void shouldSetProjectFrameworkCorrectly() {
        // given
        GitRepoData gitRepoData = new GitRepoData(
            "https://github.com/owner/repo.git",
            "repo",
            "owner"
        );
        Path clonePath = Paths.get("/tmp/cloned/repo");
        Path projectRootPath = Paths.get("/tmp/cloned/repo/src");
        CodingLanguage language = CodingLanguage.JAVASCRIPT;
        ProjectFramework framework = ProjectFramework.NODE_JS;

        // when
        ProjectMetaData result = projectMetaDataService.createProjectMetaData(
            gitRepoData, clonePath, projectRootPath, language, framework
        );

        // then
        assertThat(result.getCodingLanguage()).isEqualTo(CodingLanguage.JAVASCRIPT);
        assertThat(result.getProjectFramework()).isEqualTo(ProjectFramework.NODE_JS);
    }

    @Test
    @DisplayName("동일한 정보로 생성된 메타데이터는 같은 값을 가진다")
    void shouldHaveSameValuesForSameInput() {
        // given
        GitRepoData gitRepoData = new GitRepoData(
            "https://github.com/owner/repo.git",
            "repo",
            "owner"
        );
        Path projectRootPath = Paths.get("/tmp/cloned/repo");
        Path clonePath = Paths.get("/tmp/cloned/repo/src");
        CodingLanguage language = CodingLanguage.JAVA;
        ProjectFramework framework = ProjectFramework.SPRING_BOOT;

        // when
        ProjectMetaData result1 = projectMetaDataService.createProjectMetaData(
            gitRepoData, clonePath, projectRootPath, language, framework
        );
        ProjectMetaData result2 = projectMetaDataService.createProjectMetaData(
            gitRepoData, clonePath, projectRootPath, language, framework
        );

        // then
        assertThat(result1.getGitRepoData()).isEqualTo(result2.getGitRepoData());
        assertThat(result1.getSrcRootPath()).isEqualTo(result2.getSrcRootPath());
        assertThat(result1.getCodingLanguage()).isEqualTo(result2.getCodingLanguage());
        assertThat(result1.getProjectFramework()).isEqualTo(result2.getProjectFramework());
        // ClientProjectPath의 path 필드를 직접 비교
        assertThat(result1.getProjectRootPath().getPath()).isEqualTo(result2.getProjectRootPath().getPath());
    }

    private ProjectMetaData createSampleProjectMetaData() {
        GitRepoData gitRepoData = new GitRepoData(
            "https://github.com/owner/repo.git",
            "repo",
            "owner"
        );
        return projectMetaDataService.createProjectMetaData(
            gitRepoData,
            Paths.get("/tmp/cloned/repo"),
            Paths.get("/tmp/cloned/repo/src"),
            CodingLanguage.JAVA,
            ProjectFramework.SPRING_BOOT
        );
    }
}
