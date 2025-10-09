package com.hocs.server.common.service;

import com.hocs.server.saas_platform.domain.GitRepoData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateIdempotencyKeyServiceTest {

    @Test
    @DisplayName("동일한 GitRepoData로 호출하면 같은 idempotency key를 반환한다")
    void shouldGenerateSameKeyForSameGitRepoData() {
        // given
        GitRepoData gitRepoData = new GitRepoData(
            "https://github.com/owner/repo.git",
            "repo",
            "owner"
        );

        // when
        String key1 = GenerateIdempotencyKeyService.generateIdempotencyKey(gitRepoData, 1L);
        String key2 = GenerateIdempotencyKeyService.generateIdempotencyKey(gitRepoData, 1L);

        // then
        assertThat(key1).isEqualTo(key2);
        assertThat(key1).isNotEmpty();
        assertThat(key1).hasSize(32); // MD5 해시는 32자리
    }

    @Test
    @DisplayName("다른 GitRepoData로 호출하면 다른 idempotency key를 반환한다")
    void shouldGenerateDifferentKeyForDifferentGitRepoData() {
        // given
        GitRepoData gitRepoData1 = new GitRepoData(
            "https://github.com/owner1/repo1.git",
            "repo1",
            "owner1"
        );
        GitRepoData gitRepoData2 = new GitRepoData(
            "https://github.com/owner2/repo2.git",
            "repo2",
            "owner2"
        );

        // when
        String key1 = GenerateIdempotencyKeyService.generateIdempotencyKey(gitRepoData1, 1L);
        String key2 = GenerateIdempotencyKeyService.generateIdempotencyKey(gitRepoData2, 1L);

        // then
        assertThat(key1).isNotEqualTo(key2);
        assertThat(key1).hasSize(32);
        assertThat(key2).hasSize(32);
    }
}
