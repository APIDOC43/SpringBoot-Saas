package com.hocs.server.extractor.respository.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import com.hocs.server.extractor.domain.AOP;
import com.hocs.server.extractor.domain.API;
import com.hocs.server.extractor.domain.APISourceDependencyInfo;
import com.hocs.server.extractor.domain.ApiEndpoint;
import com.hocs.server.extractor.domain.ExceptionHandler;
import com.hocs.server.extractor.domain.GlobalSourceDependency;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

@DataMongoTest
@ActiveProfiles("dev")
class APISourceDependencyRepositoryTest {

	@Autowired
	private APISourceDependencyRepository repository;

	private APISourceDependencyInfo apiDependency;

	@BeforeEach
	public void setUp() {
		repository.deleteAll();  // 테스트 환경 초기화

		// 테스트용 APIConfiguration 객체 생성
		API testAPI = API.create(
			ApiEndpoint.create("api/v1/test", "POST"), Arrays.asList("path1", "path2"));
		GlobalSourceDependency global = GlobalSourceDependency.create(
			UUID.randomUUID().toString(),
			AOP.create(Arrays.asList("aopPath1", "aopPath2")),
			ExceptionHandler.create( List.of("exceptionHandlerPath")),
			Arrays.asList("config1", "config2"),
			Arrays.asList("component1", "component2")
		);

		apiDependency = APISourceDependencyInfo.create(
			UUID.randomUUID().toString(),
			"testUser",
			List.of(testAPI),
			global);
	}

	@Test
	@DisplayName("MongoDB에 APIConfiguration 저장 테스트")
	public void testSaveAPIConfiguration() {
		APISourceDependencyInfo save = repository.save(apiDependency);

		assertThat(save.getId()).isNotNull();
		assertThat(save.getApiSourceDependencies().size()).isEqualTo(1);
		assertThat(save.getGlobal().getConfiguration()).contains("config1", "config2");
	}

	@Test
	@DisplayName("MongoDB에서 모든 APIConfiguration 조회 테스트")
	public void testFindAllAPIConfigurations() {
		repository.save(apiDependency);

		List<APISourceDependencyInfo> apiDependencies = repository.findAll();

		assertThat(apiDependencies).hasSize(1);
		assertThat(apiDependencies.get(0).getApiSourceDependencies().get(0).getApiEndpoint().getApi()).isEqualTo(
			"api/v1/test");
	}

	@Test
	@DisplayName("MongoDB에서 APIConfiguration ID로 조회 테스트")
	public void testFindAPIConfigurationById() {
		APISourceDependencyInfo save = repository.save(apiDependency);

		APISourceDependencyInfo dependencies = repository.findById(save.getId())
			.orElse(null);

		assertThat(dependencies).isNotNull();
		assertThat(dependencies.getGlobal().getComponent()).contains("component1", "component2");
	}

	@Test
	@DisplayName("MongoDB에서 APIConfiguration 삭제 테스트")
	public void testDeleteAPIConfiguration() {
		APISourceDependencyInfo save = repository.save(apiDependency);

		repository.deleteById(save.getId());

		assertThat(repository.findById(save.getId())).isNotPresent();
	}
}