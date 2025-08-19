package com.hocs.server.api_spec_generator.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileManagerTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("파일을 생성하고 내용을 쓸 수 있다")
    void shouldCreateFileAndWriteContent() throws IOException {
        // given
        Path filePath = tempDir.resolve("test.txt");
        String content = "Hello, World!";

        // when
        FileManager.saveToFile(filePath.toString(), content);

        // then
        assertThat(Files.exists(filePath)).isTrue();
        String actualContent = Files.readString(filePath);
        assertThat(actualContent).isEqualTo(content);
    }

    @Test
    @DisplayName("기존 파일의 내용을 덮어쓸 수 있다")
    void shouldOverwriteExistingFile() throws IOException {
        // given
        Path filePath = tempDir.resolve("existing.txt");
        Files.writeString(filePath, "Old content");
        String newContent = "New content";

        // when
        FileManager.saveToFile(filePath.toString(), newContent);

        // then
        String actualContent = Files.readString(filePath);
        assertThat(actualContent).isEqualTo(newContent);
    }

    @Test
    @DisplayName("존재하지 않는 디렉터리에 파일을 생성할 수 있다")
    void shouldCreateFileInNonExistentDirectory() throws IOException {
        // given
        Path nonExistentDir = tempDir.resolve("new/directory");
        Path filePath = nonExistentDir.resolve("test.txt");
        String content = "Test content";

        // when
        FileManager.saveToFile(filePath.toString(), content);

        // then
        assertThat(Files.exists(filePath)).isTrue();
        String actualContent = Files.readString(filePath);
        assertThat(actualContent).isEqualTo(content);
    }

    @Test
    @DisplayName("빈 내용으로 파일을 생성할 수 있다")
    void shouldCreateFileWithEmptyContent() throws IOException {
        // given
        Path filePath = tempDir.resolve("empty.txt");
        String emptyContent = "";

        // when
        FileManager.saveToFile(filePath.toString(), emptyContent);

        // then
        assertThat(Files.exists(filePath)).isTrue();
        String actualContent = Files.readString(filePath);
        assertThat(actualContent).isEmpty();
    }

    @Test
    @DisplayName("null 경로로 파일 쓰기 시도 시 예외를 발생시킨다")
    void shouldThrowExceptionForNullPath() {
        // given
        String nullPath = null;
        String content = "Test content";

        // when & then
        assertThatThrownBy(() -> FileManager.saveToFile(nullPath, content))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
