package com.hocs.server.common.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LanguageFrameworkFactoryTest {

    @Test
    @DisplayName("Java와 Spring Boot 조합으로 SpringBootJava를 생성한다")
    void shouldCreateSpringBootJavaForJavaAndSpringBoot() {
        // given
        CodingLanguage language = CodingLanguage.JAVA;
        ProjectFramework framework = ProjectFramework.SPRING_BOOT;

        // when
        LanguageFramework result = LanguageFrameworkFactory.create(language, framework);

        // then
        assertThat(result).isInstanceOf(SpringBootJava.class);
    }

    @Test
    @DisplayName("JavaScript와 Node.js 조합으로 NodeJs를 생성한다")
    void shouldCreateNodeJsForJavaScriptAndNodeJs() {
        // given
        CodingLanguage language = CodingLanguage.JAVASCRIPT;
        ProjectFramework framework = ProjectFramework.NODE_JS;

        // when
        LanguageFramework result = LanguageFrameworkFactory.create(language, framework);

        // then
        assertThat(result).isInstanceOf(NodeJs.class);
    }

    @Test
    @DisplayName("지원되지 않는 언어-프레임워크 조합에 대해 예외를 발생시킨다")
    void shouldThrowExceptionForUnsupportedCombination() {
        // given
        CodingLanguage language = CodingLanguage.JAVA;
        ProjectFramework framework = ProjectFramework.NODE_JS; // 지원되지 않는 조합

        // when & then
        assertThatThrownBy(() -> LanguageFrameworkFactory.create(language, framework))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported combination");
    }

    @Test
    @DisplayName("null 언어로 생성 시도 시 예외를 발생시킨다")
    void shouldThrowExceptionForNullLanguage() {
        // given
        CodingLanguage language = null;
        ProjectFramework framework = ProjectFramework.SPRING_BOOT;

        // when & then
        assertThatThrownBy(() -> LanguageFrameworkFactory.create(language, framework))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("null 프레임워크로 생성 시도 시 예외를 발생시킨다")
    void shouldThrowExceptionForNullFramework() {
        // given
        CodingLanguage language = CodingLanguage.JAVA;
        ProjectFramework framework = null;

        // when & then
        assertThatThrownBy(() -> LanguageFrameworkFactory.create(language, framework))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("동일한 조합으로 여러 번 호출해도 같은 타입의 객체를 반환한다")
    void shouldReturnSameTypeForSameCombination() {
        // given
        CodingLanguage language = CodingLanguage.JAVA;
        ProjectFramework framework = ProjectFramework.SPRING_BOOT;

        // when
        LanguageFramework result1 = LanguageFrameworkFactory.create(language, framework);
        LanguageFramework result2 = LanguageFrameworkFactory.create(language, framework);

        // then
        assertThat(result1.getClass()).isEqualTo(result2.getClass());
        assertThat(result1).isInstanceOf(SpringBootJava.class);
        assertThat(result2).isInstanceOf(SpringBootJava.class);
    }
}
