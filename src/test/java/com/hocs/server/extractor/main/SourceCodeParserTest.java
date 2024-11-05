package com.hocs.server.extractor.main;

import static org.assertj.core.api.Assertions.assertThat;

import com.hocs.server.saas.user.gitapi.domin.GitRepo;
import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class SourceCodeParserTest {

	@Autowired
	private SourceCodeParser sourceCodeParser;

	@Test
	public void create_MetaData() throws Exception {
		String currentPath = System.getProperty("user.dir");


		String userId = UUID.randomUUID().toString();
		File PROJECT_ROOT_DIR = new File(currentPath);
		Path metaData = sourceCodeParser.createMetaData(PROJECT_ROOT_DIR, GitRepo.of(""), userId);

		metaData.toFile().delete();
	}
}