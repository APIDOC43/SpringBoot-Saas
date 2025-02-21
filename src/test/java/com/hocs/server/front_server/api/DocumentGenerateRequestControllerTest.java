package com.hocs.server.front_server.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hocs.server.common.domain.CodingLanguage;
import com.hocs.server.front_server.api.request.FindApiInfoClientRequest;
import com.hocs.server.common.domain.ProjectFramework;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(value = "dev")
@SpringBootTest
@AutoConfigureMockMvc
class DocumentGenerateRequestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void testFindApiInfo_ValidRequest_ShouldReturnAccepted() throws Exception {
		FindApiInfoClientRequest request = new FindApiInfoClientRequest();
		request.setLanguage(CodingLanguage.JAVA);
		request.setProjectFramework(ProjectFramework.SPRINGBOOT);
		request.setGitCloneUrl("https://github.com/seokjun7410/HaksikMatnam_Backend.git");
		request.setCoreSrcRootPath("/src/main/java");

		mockMvc.perform(post("/apis/document/endpoint/demo/v1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());
	}

	@Test
	void testFindApiInfo_InvalidRequest_ShouldReturnBadRequest() throws Exception {
		FindApiInfoClientRequest request = new FindApiInfoClientRequest();
		request.setLanguage(null);
		request.setProjectFramework(null);

		mockMvc.perform(post("/apis/document/endpoint/demo/v1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}
}