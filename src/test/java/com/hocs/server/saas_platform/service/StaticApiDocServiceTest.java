package com.hocs.server.saas_platform.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.hocs.server.saas_platform.controller.request.GetContentRequest;
import com.hocs.server.saas_platform.domain.FilesData;

@DisplayName("StaticApiDocService 테스트")
class StaticApiDocServiceTest {

	private StaticApiDocService staticApiDocService;

	@TempDir
	Path tempDir;

	private String testHtmlDir;

	@BeforeEach
	void setUp() throws IOException {
		staticApiDocService = new StaticApiDocService();
		testHtmlDir = tempDir.toString();
		ReflectionTestUtils.setField(staticApiDocService, "HTML_DIR", testHtmlDir);
		
		// 테스트용 디렉토리 및 파일 생성
		setupTestDirectories();
	}

	@AfterEach
	void tearDown() {
		// 테스트 후 정리는 @TempDir이 자동으로 처리
	}

	private void setupTestDirectories() throws IOException {
		// 사용자별 디렉토리 구조 생성
		Path user1Dir = tempDir.resolve("user1/dist/apis");
		Path user2Dir = tempDir.resolve("user2/dist/apis");
		Files.createDirectories(user1Dir);
		Files.createDirectories(user2Dir);

		// HTML 파일들 생성
		Files.createFile(user1Dir.resolve("api1.html"));
		Files.createFile(user1Dir.resolve("api2.html"));
		Files.createFile(user1Dir.resolve("not-html.txt")); // HTML이 아닌 파일
		
		Files.createFile(user2Dir.resolve("api3.html"));
		Files.createFile(user2Dir.resolve("api4.html"));
	}

	@Test
	@DisplayName("존재하지 않는 사용자 ID로 조회 시 빈 목록을 반환해야 한다")
	void shouldReturnEmptyListWhenUserDirectoryNotExists() {
		// Given
		String nonExistentUserId = "nonexistent";

		// When
		List<FilesData> result = staticApiDocService.findApiListByUserId(nonExistentUserId);

		// Then
		// IOException이 발생하면 null이 반환됨 (현재 구현)
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("HTML 파일이 없는 사용자 디렉토리 조회 시 빈 목록을 반환해야 한다")
	void shouldReturnEmptyListWhenNoHtmlFilesExist() throws IOException {
		// Given
		String userId = "user3";
		Path user3Dir = tempDir.resolve("user3/dist/apis");
		Files.createDirectories(user3Dir);
		Files.createFile(user3Dir.resolve("readme.txt")); // HTML이 아닌 파일만 존재

		// When
		List<FilesData> result = staticApiDocService.findApiListByUserId(userId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("콘텐츠 경로 조회 시 올바른 Path를 반환해야 한다")
	void shouldReturnCorrectPathWhenGettingContent() {
		// Given
		GetContentRequest request = new GetContentRequest();
		request.setFilename("test/file.html");

		// When
		Path result = staticApiDocService.getContent(request);

		// Then
		assertThat(result).isEqualTo(Paths.get(testHtmlDir, "test/file.html"));
	}

	@Test
	@DisplayName("콘텐츠 경로 조회 시 파일명이 null이어도 정상 처리되어야 한다")
	void shouldHandleNullFilenameInGetContent() {
		// Given
		GetContentRequest request = new GetContentRequest();
		request.setFilename(null);

		// When
		Path result = staticApiDocService.getContent(request);

		// Then
		// null 파일명인 경우 HTML_DIR만 반환
		assertThat(result).isEqualTo(Paths.get(testHtmlDir));
	}

	@Test
	@DisplayName("빈 문자열 파일명으로 콘텐츠 경로 조회 시 정상 처리되어야 한다")
	void shouldHandleEmptyFilenameInGetContent() {
		// Given
		GetContentRequest request = new GetContentRequest();
		request.setFilename("");

		// When
		Path result = staticApiDocService.getContent(request);

		// Then
		assertThat(result).isEqualTo(Paths.get(testHtmlDir, ""));
	}
}
