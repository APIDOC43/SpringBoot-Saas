package com.hocs.server.extractor.main;

import static org.assertj.core.api.Assertions.assertThat;

import com.hocs.server.custom_rag.legacy.extractor.core.client.SpringJavaApiCodeClient;
import com.hocs.server.custom_rag.legacy.extractor.domain.APISourceDependencyInfo;
import com.hocs.server.custom_rag.legacy.extractor.domain.ClientProjectType;
import com.hocs.server.front_server.legacy.saas.user.gitapi.domin.GitRepo;
import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles("dev")
class SpringJavaApiCodeManagerTest {

	@Autowired
	private SpringJavaApiCodeClient springJavaApiCodeManager;

	@Test
	public void create_MetaData() throws Exception {
		String currentPath = System.getProperty("user.dir");


		String userId = UUID.randomUUID().toString();
		File PROJECT_ROOT_DIR = new File(currentPath);
		ClientProjectType clientProjectType = ClientProjectType.SPRING_JAVA;
		Path SOURCE_ROOT = new File(PROJECT_ROOT_DIR, clientProjectType.srcRootPath()).toPath();
		APISourceDependencyInfo metaData = springJavaApiCodeManager.findDependencyInfo(
			ClientProjectType.SPRING_JAVA, SOURCE_ROOT, GitRepo.of("https://github.com/givanthak/spring-boot-rest-api-tutorial.githttps://github.com/givanthak/spring-boot-rest-api-tutorial.git"), userId);

		assertThat(metaData).isNotNull();
	}
}