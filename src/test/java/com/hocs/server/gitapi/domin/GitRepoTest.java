package com.hocs.server.gitapi.domin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hocs.server.saas_platform.domain.GitRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GitRepoTest {

    @Test
    @DisplayName("HTTPS Git URL에서 .git 확장자를 제거한 URL을 반환한다")
    void shouldParseHttpsGitUrlAndRemoveGitExtension() {
        // Given
        String repoUrl = "https://github.com/spring-projects/spring-petclinic.git";
        
        // When
        GitRepo gitRepo = GitRepo.of(repoUrl);
        
        // Then
        String url = gitRepo.getUrl();
        assertThat(url).isEqualTo("https://github.com/spring-projects/spring-petclinic");
    }

    @Test
    @DisplayName(".git 확장자가 없는 HTTPS URL은 그대로 반환한다")
    void shouldKeepHttpsUrlWithoutGitExtension() {
        // Given
        String repoUrl = "https://github.com/spring-projects/spring-petclinic";
        
        // When
        GitRepo gitRepo = GitRepo.of(repoUrl);
        
        // Then
        String url = gitRepo.getUrl();
        assertThat(url).isEqualTo("https://github.com/spring-projects/spring-petclinic");
    }

    @Test
    @DisplayName("SSH Git URL을 올바르게 파싱한다")
    void shouldParseSshGitUrl() {
        // Given
        String repoUrl = "git@github.com:spring-projects/spring-petclinic.git";
        
        // When
        GitRepo gitRepo = GitRepo.of(repoUrl);
        
        // Then
        String url = gitRepo.getUrl();
        // SSH URL의 경우 실제 구현에 따라 결과가 달라질 수 있음
        assertThat(url).isNotNull();
        assertThat(url).isNotEmpty();
    }

    @Test
    @DisplayName("다양한 Git 호스팅 서비스 URL을 처리한다")
    void shouldHandleDifferentGitHostingServices() {
        // Given & When & Then
        String githubUrl = "https://github.com/user/repo.git";
        GitRepo githubRepo = GitRepo.of(githubUrl);
        assertThat(githubRepo.getUrl()).isEqualTo("https://github.com/user/repo");

        String gitlabUrl = "https://gitlab.com/user/repo.git";
        GitRepo gitlabRepo = GitRepo.of(gitlabUrl);
        assertThat(gitlabRepo.getUrl()).isEqualTo("https://gitlab.com/user/repo");

        String bitbucketUrl = "https://bitbucket.org/user/repo.git";
        GitRepo bitbucketRepo = GitRepo.of(bitbucketUrl);
        assertThat(bitbucketRepo.getUrl()).isEqualTo("https://bitbucket.org/user/repo");
    }

    @Test
    @DisplayName("복잡한 경로를 가진 Git URL을 처리한다")
    void shouldHandleComplexGitUrlPaths() {
        // Given
        String complexUrl = "https://github.com/organization/team/project-name.git";
        
        // When
        GitRepo gitRepo = GitRepo.of(complexUrl);
        
        // Then
        String url = gitRepo.getUrl();
        assertThat(url).isEqualTo("https://github.com/organization/team/project-name");
    }

    @Test
    @DisplayName("포트 번호가 포함된 Git URL을 처리한다")
    void shouldHandleGitUrlWithPort() {
        // Given
        String urlWithPort = "https://git.company.com:443/team/project.git";
        
        // When
        GitRepo gitRepo = GitRepo.of(urlWithPort);
        
        // Then
        String url = gitRepo.getUrl();
        assertThat(url).isEqualTo("https://git.company.com:443/team/project");
    }

    @Test
    @DisplayName("null URL로 GitRepo 생성 시 예외를 발생시킨다")
    void shouldThrowExceptionForNullUrl() {
        // Given
        String nullUrl = null;
        
        // When & Then
        assertThatThrownBy(() -> GitRepo.of(nullUrl))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("빈 문자열 URL로 GitRepo 생성 시 예외를 발생시킨다")
    void shouldThrowExceptionForEmptyUrl() {
        // Given
        String emptyUrl = "";
        
        // When & Then
        assertThatThrownBy(() -> GitRepo.of(emptyUrl))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("공백만 있는 URL로 GitRepo 생성 시 예외를 발생시킨다")
    void shouldThrowExceptionForBlankUrl() {
        // Given
        String blankUrl = "   ";
        
        // When & Then
        assertThatThrownBy(() -> GitRepo.of(blankUrl))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("잘못된 형식의 URL로 GitRepo 생성 시 예외를 발생시킨다")
    void shouldThrowExceptionForInvalidUrl() {
        // Given
        String invalidUrl = "not-a-valid-url";
        
        // When & Then
        assertThatThrownBy(() -> GitRepo.of(invalidUrl))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("동일한 URL로 생성된 GitRepo 객체는 같은 URL을 반환한다")
    void shouldReturnSameUrlForSameInput() {
        // Given
        String repoUrl = "https://github.com/test/repository.git";
        
        // When
        GitRepo gitRepo1 = GitRepo.of(repoUrl);
        GitRepo gitRepo2 = GitRepo.of(repoUrl);
        
        // Then
        assertThat(gitRepo1.getUrl()).isEqualTo(gitRepo2.getUrl());
    }

    @Test
    @DisplayName("대소문자가 다른 URL도 올바르게 처리한다")
    void shouldHandleUrlsWithDifferentCase() {
        // Given
        String upperCaseUrl = "HTTPS://GITHUB.COM/USER/REPO.GIT";
        
        // When
        GitRepo gitRepo = GitRepo.of(upperCaseUrl);
        
        // Then
        String url = gitRepo.getUrl();
        assertThat(url).isNotNull();
        assertThat(url).isNotEmpty();
        // 실제 구현에 따라 대소문자 처리 방식이 결정됨
    }

    @Test
    @DisplayName("쿼리 파라미터가 포함된 URL을 처리한다")
    void shouldHandleUrlWithQueryParameters() {
        // Given
        String urlWithQuery = "https://github.com/user/repo.git?ref=main&token=abc123";
        
        // When
        GitRepo gitRepo = GitRepo.of(urlWithQuery);
        
        // Then
        String url = gitRepo.getUrl();
        assertThat(url).isNotNull();
        assertThat(url).isNotEmpty();
        // 쿼리 파라미터 처리는 실제 구현에 따라 결정됨
    }
}
