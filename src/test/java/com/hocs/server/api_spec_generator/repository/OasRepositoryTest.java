package com.hocs.server.api_spec_generator.repository;

import com.hocs.server.api_spec_generator.domain.output.MediaType;
import com.hocs.server.api_spec_generator.domain.output.OAS;
import com.hocs.server.api_spec_generator.domain.output.OasInfo;
import com.hocs.server.api_spec_generator.domain.output.Operation;
import com.hocs.server.api_spec_generator.domain.output.Parameter;
import com.hocs.server.api_spec_generator.domain.output.PathItem;
import com.hocs.server.api_spec_generator.domain.output.RequestBody;
import com.hocs.server.api_spec_generator.domain.output.Response;
import com.hocs.server.api_spec_generator.domain.output.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("dev")
class OasRepositoryTest {

	@Autowired
	private OasRepository oasRepository;

	private OAS oas;

	@BeforeEach
	void setUp() {
		OasInfo info = OasInfo.create("user1", "3.0.1", "Info", "Title", "Description", "1.0");

		Schema schema = Schema.create("string", null, "Sample schema", "default", true, null, null, null, null, null, null, null);

		MediaType mediaType = MediaType.create(schema, "Sample media type");

		RequestBody requestBody = RequestBody.create("Request body description", true, Map.of("application/json", mediaType));

		Response response = Response.create("Response description", Map.of("application/json", mediaType), null);

		Operation operation = Operation.create(
			"Sample operation",
			"operationId",
			"Description",
			List.of("tag1"),
			List.of("audience1"),
			List.of(Parameter.create("param1", "query", "A query parameter", true, "example", schema)),
			requestBody,
			Map.of("200", response),
			Map.of("x-extension", "value")
		);

		PathItem pathItem = PathItem.create(operation, null, null, null, null, null, null, null, "link", Map.of("x-field", "fieldValue"));
		Map<String, List<Map<String, PathItem>>> pathList = Map.of("paths", List.of(Map.of("/test", pathItem)));

		oas = OAS.create("1", "1",info, pathList, Map.of("components", List.of(schema)));
	}

	@Test
	void testOasSaveAndRetrieve() {
		// 저장
		OAS savedOas = oasRepository.save(oas);

		// 검증
		assertThat(savedOas).isNotNull();
		assertThat(savedOas.getInfo().getTitle()).isEqualTo("Title");
		assertThat(savedOas.getSchemasMap()).isNotEmpty();

		// 조회
		OAS foundOas = oasRepository.findById(savedOas.getId()).orElse(null);
		assertThat(foundOas).isNotNull();
		assertThat(foundOas.getInfo().getTitle()).isEqualTo("Title");
		assertThat(foundOas.getPathList()).isNotEmpty();
	}
}