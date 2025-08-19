package com.hocs.server.common.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SpringBootJavaTest {

    private SpringBootJava springBootJava;

    @BeforeEach
    void setUp() {
        springBootJava = new SpringBootJava();
    }

    @Test
    @DisplayName("Controller로 끝나는 Java 파일은 API Entry로 인식한다")
    void shouldRecognizeControllerFilesAsApiEntry() {
        // given
        File controllerFile = new File("UserController.java");

        // when
        boolean result = springBootJava.isApiEntry(controllerFile.toPath());

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("RestController로 끝나는 Java 파일은 API Entry로 인식한다")
    void shouldRecognizeRestControllerFilesAsApiEntry() {
        // given
        File restControllerFile = new File("UserRestController.java");

        // when
        boolean result = springBootJava.isApiEntry(restControllerFile.toPath());

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Controller가 포함되지 않은 Java 파일은 API Entry로 인식하지 않는다")
    void shouldNotRecognizeNonControllerFilesAsApiEntry() {
        // given
        File serviceFile = new File("UserService.java");

        // when
        boolean result = springBootJava.isApiEntry(serviceFile.toPath());

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Java 파일이 아닌 경우 API Entry로 인식하지 않는다")
    void shouldNotRecognizeNonJavaFilesAsApiEntry() {
        // given
        File txtFile = new File("UserController.txt");

        // when
        boolean result = springBootJava.isApiEntry(txtFile.toPath());

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("경로 중간에 Controller가 있어도 파일명이 Controller로 끝나지 않으면 API Entry로 인식하지 않는다")
    void shouldNotRecognizeWhenControllerIsNotInFileName() {
        // given
        File file = new File("/controller/UserService.java");

        // when
        boolean result = springBootJava.isApiEntry(file.toPath());

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("대소문자를 구분하여 controller는 API Entry로 인식하지 않는다")
    void shouldBeCaseSensitive() {
        // given
        File file = new File("Usercontroller.java");

        // when
        boolean result = springBootJava.isApiEntry(file.toPath());

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("확장자가 없는 파일은 API Entry로 인식하지 않는다")
    void shouldNotRecognizeFilesWithoutExtension() {
        // given
        File file = new File("UserController");

        // when
        boolean result = springBootJava.isApiEntry(file.toPath());

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("null path에 대해서는 false를 반환한다")
    void shouldReturnFalseForNullFile() {
        // given
        Path path = null;

        // when
        boolean result = springBootJava.isApiEntry(path);

        // then
        assertThat(result).isFalse();
    }
}
