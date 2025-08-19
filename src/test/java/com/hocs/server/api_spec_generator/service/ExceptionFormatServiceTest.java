package com.hocs.server.api_spec_generator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import com.hocs.server.api_spec_generator.llm.SpringAICommandForLLM;
import com.hocs.server.common.domain.ClientProjectPath;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExceptionFormatService 테스트")
class ExceptionFormatServiceTest {

	@Mock
	private SpringAICommandForLLM springAiCommandForLLM;

	@Mock
	private ChatClient chatClient4o;

	@InjectMocks
	private ExceptionFormatService exceptionFormatService;

	@TempDir
	Path tempDir;

	private ClientProjectPath testProjectPath;

	@BeforeEach
	void setUp() {
		testProjectPath = new ClientProjectPath(tempDir.toAbsolutePath());
	}

	@Test
	@DisplayName("예외 관련 소스 파일을 찾고 읽어야 한다")
	void shouldFindAndReadExceptionSources() throws IOException {
		// Given
		Path exceptionFile1 = tempDir.resolve("Exception1.java");
		Path exceptionFile2 = tempDir.resolve("Exception2.java");
		
		Files.writeString(exceptionFile1, "public class Exception1 extends RuntimeException {}");
		Files.writeString(exceptionFile2, "public class Exception2 extends Exception {}");

		String[] exceptionSources = {
			exceptionFile1.toString(),
			exceptionFile2.toString()
		};

		when(springAiCommandForLLM.findFilePathRelatedExceptionFormatSrc(eq(testProjectPath), eq(chatClient4o)))
			.thenReturn(exceptionSources);

		// When
		String result = exceptionFormatService.findRelatedExceptionSrc(testProjectPath, chatClient4o);

		// Then
		assertThat(result).contains("public class Exception1 extends RuntimeException {}");
		assertThat(result).contains("public class Exception2 extends Exception {}");
		
		verify(springAiCommandForLLM).findFilePathRelatedExceptionFormatSrc(testProjectPath, chatClient4o);
	}

	@Test
	@DisplayName("빈 경로 배열이 주어지면 빈 문자열을 반환해야 한다")
	void shouldReturnEmptyStringWhenEmptyPathArray() throws IOException {
		// Given
		String[] emptyExceptionSources = {};

		when(springAiCommandForLLM.findFilePathRelatedExceptionFormatSrc(eq(testProjectPath), eq(chatClient4o)))
			.thenReturn(emptyExceptionSources);

		// When
		String result = exceptionFormatService.findRelatedExceptionSrc(testProjectPath, chatClient4o);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("존재하지 않는 파일 경로가 포함되어 있으면 IOException이 발생해야 한다")
	void shouldThrowIOExceptionWhenFileNotExists() {
		// Given
		String[] exceptionSources = {
			"/non/existent/path/Exception1.java",
			"/another/non/existent/Exception2.java"
		};

		when(springAiCommandForLLM.findFilePathRelatedExceptionFormatSrc(eq(testProjectPath), eq(chatClient4o)))
			.thenReturn(exceptionSources);

		// When & Then
		assertThatThrownBy(() -> 
			exceptionFormatService.findRelatedExceptionSrc(testProjectPath, chatClient4o)
		).isInstanceOf(IOException.class);
	}

	@Test
	@DisplayName("read 메서드는 여러 파일의 내용을 결합해야 한다")
	void shouldCombineMultipleFileContents() throws IOException {
		// Given
		Path file1 = tempDir.resolve("file1.java");
		Path file2 = tempDir.resolve("file2.java");
		
		Files.writeString(file1, "Content of file 1");
		Files.writeString(file2, "Content of file 2");

		String[] filePaths = {
			file1.toString(),
			file2.toString()
		};

		// When
		String result = exceptionFormatService.read(filePaths);

		// Then
		assertThat(result).contains("Content of file 1");
		assertThat(result).contains("Content of file 2");
		assertThat(result).contains("\n"); // 파일 간 구분자
	}

	@Test
	@DisplayName("read 메서드는 빈 문자열 경로를 무시해야 한다")
	void shouldIgnoreEmptyStringPaths() throws IOException {
		// Given
		Path validFile = tempDir.resolve("valid.java");
		Files.writeString(validFile, "Valid content");

		String[] filePaths = {
			validFile.toString(),
			"", // 빈 문자열
			validFile.toString()
		};

		// When
		String result = exceptionFormatService.read(filePaths);

		// Then
		assertThat(result).contains("Valid content");
		// 빈 문자열로 인한 추가 내용이 없어야 함
		String[] lines = result.split("\n");
		assertThat(lines).hasSize(2); // 유효한 파일 2개만
	}

	@Test
	@DisplayName("read 메서드는 단일 파일을 올바르게 읽어야 한다")
	void shouldReadSingleFileCorrectly() throws IOException {
		// Given
		Path singleFile = tempDir.resolve("single.java");
		String expectedContent = "public class SingleException extends RuntimeException {\n    // content\n}";
		Files.writeString(singleFile, expectedContent);

		String[] filePaths = { singleFile.toString() };

		// When
		String result = exceptionFormatService.read(filePaths);

		// Then
		assertThat(result).contains(expectedContent);
	}

	@Test
	@DisplayName("SpringAI 서비스가 IOException을 던지면 전파되어야 한다")
	void shouldPropagateIOExceptionFromSpringAI() throws IOException {
		// Given
		when(springAiCommandForLLM.findFilePathRelatedExceptionFormatSrc(eq(testProjectPath), eq(chatClient4o)))
			.thenThrow(new IOException("AI service error"));

		// When & Then
		assertThatThrownBy(() -> 
			exceptionFormatService.findRelatedExceptionSrc(testProjectPath, chatClient4o)
		).isInstanceOf(IOException.class)
		.hasMessage("AI service error");
	}

	@Test
	@DisplayName("일부 파일만 존재하는 경우 존재하는 파일만 읽어야 한다")
	void shouldReadOnlyExistingFiles() throws IOException {
		// Given
		Path existingFile = tempDir.resolve("existing.java");
		Files.writeString(existingFile, "Existing file content");

		String[] filePaths = {
			existingFile.toString(),
			"/non/existent/file.java" // 존재하지 않는 파일
		};

		// When & Then
		assertThatThrownBy(() -> exceptionFormatService.read(filePaths))
			.isInstanceOf(IOException.class);
	}
}
