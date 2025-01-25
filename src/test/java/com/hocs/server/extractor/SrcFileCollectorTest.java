package com.hocs.server.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hocs.server.code_resolver.legacy.extractor.core.SrcFileCollector;
import com.hocs.server.code_resolver.legacy.extractor.domain.ClientProjectType;
import com.hocs.server.code_resolver.legacy.extractor.domain.SrcSuffix;
import com.hocs.server.openai.domain.input.APIMetadata;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
public class SrcFileCollectorTest {

	@Autowired
	private SrcFileCollector srcFileCollector;

	@TempDir
	Path tempDir;

	private Path file1;
	private Path file2;
	private Path file3;


	private List<APIMetadata> apiEntries;

	@BeforeEach
	public void setUp() throws IOException {
		Path src = Files.createDirectory(tempDir.resolve("src"));
		Path main = Files.createDirectory(src.resolve("main"));
		Path java = Files.createDirectory(main.resolve("java"));

		file1 = Files.createFile(java.resolve("Test1.java"));
		file2 = Files.createFile(java.resolve("Test2.java"));
		file3 = Files.createFile(java.resolve("Test3.java"));

		apiEntries = new ArrayList<>();
		APIMetadata entry = APIMetadata.create("test", "test", new ArrayList<>(), new ArrayList<>(), "test");
		apiEntries.add(entry);

	}

	@Test
	public void testGetJAVANotUsedSrc() {
		List<String> paths = apiEntries.get(0).getPaths();
		paths.add(file1.toAbsolutePath().toString());

		String projectRoot = tempDir.toString();
		List<File> notUsedSrcFiles = srcFileCollector.getNotUesedSrc(apiEntries, projectRoot,
			ClientProjectType.SPRING_JAVA);

		assertEquals(2, notUsedSrcFiles.size());

	}

	@Test
	public void testCollectFiles() throws IOException {
		Path subDir = Files.createDirectory(tempDir.resolve("sub"));
		Files.createFile(subDir.resolve("IgnoredFile.txt"));

		List<File> collectedFiles = srcFileCollector.collectFiles(tempDir.toFile(), SrcSuffix.JAVA);

		assertEquals(3, collectedFiles.size());
	}
}
