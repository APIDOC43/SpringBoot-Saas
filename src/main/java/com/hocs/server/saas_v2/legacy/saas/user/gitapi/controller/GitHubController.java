package com.hocs.server.saas_v2.legacy.saas.user.gitapi.controller;

import com.hocs.server.saas_v2.legacy.saas.user.gitapi.service.GitHubFacadeService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/github")
@RequiredArgsConstructor
public class GitHubController {

	private final OAuth2AuthorizedClientService authorizedClientService;
	private final GitHubFacadeService gitHubFacadeService;

	@GetMapping("/login")
	public String home(Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
		if (oauth2User != null) {

			// 액세스 토큰 가져오기
			OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
				"github",
				oauth2User.getName()
			);

			if (client == null) {
				return "redirect:/oauth2/authorization/github";
			}

			String accessToken = client.getAccessToken().getTokenValue();

			// 사용자 저장소 가져오기
			List<Map<String, Object>> repos = gitHubFacadeService.getUserRepos(accessToken);
			model.addAttribute("repos", repos);
			model.addAttribute("userName", oauth2User.getAttribute("login"));	// 사용자 이름 가져오기

			return "welcome";
		}
		return "index";
	}
	@GetMapping("/callback")
	public String githubCallback(@AuthenticationPrincipal OAuth2AuthenticationToken authentication) {
		gitHubFacadeService.gitPrivateClone(authentication);

		return "Repository Cloned!";
	}



}
