package com.hocs.server.saas_v2.api;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hocs.server.saas.user.config.SecurityConfig;
import com.hocs.server.saas_v2.api.request.CodingLanguage;
import com.hocs.server.saas_v2.api.request.GenerationRequest;
import com.hocs.server.saas_v2.api.request.ProjectFramework;
import com.hocs.server.saas_v2.service.DocumentGenerateRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(value = "dev")
@WebMvcTest(controllers = DocumentGenerateRequestController.class)
@Import(SecurityConfig.class)
class DocumentGenerateRequestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private DocumentGenerateRequestService service;

	@Test
	void testDocumentGenerationReceipt_ValidRequest_ShouldReturnAccepted() throws Exception {
		GenerationRequest request = new GenerationRequest();
		request.setLanguage(CodingLanguage.JAVA);
		request.setProjectFramework(ProjectFramework.SPRINGBOOT);
		request.setCoreSrcRootPath("/src/main/java");
		request.setUserId("user123");

		mockMvc.perform(post("/apis/document/generation/receipt/v1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isAccepted());
	}

	@Test
	void testDocumentGenerationReceipt_InvalidRequest_ShouldReturnBadRequest() throws Exception {
		GenerationRequest request = new GenerationRequest();
		request.setLanguage(null);
		request.setProjectFramework(null);

		mockMvc.perform(post("/apis/document/generation/receipt/v1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}
}