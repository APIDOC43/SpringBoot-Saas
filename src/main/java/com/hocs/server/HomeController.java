package com.hocs.server;


import com.hocs.server.saas_v2.legacy.saas.user.gitapi.service.GitHubFacadeService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@Autowired
	private OAuth2AuthorizedClientService authorizedClientService;

	@Autowired
	private GitHubFacadeService gitHubFacadeService;


	@GetMapping("/")
	public void index(HttpServletResponse response) throws IOException {
		response.sendRedirect("https://apidoc43.softr.app");
	}










}

