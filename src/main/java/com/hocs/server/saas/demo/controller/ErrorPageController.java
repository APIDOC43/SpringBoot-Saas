package com.hocs.server.saas.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController {
	@GetMapping("demo/error/git")
	public String demoError(Model model) {
		model.addAttribute("message", "생성 실패");
		model.addAttribute("messageBody", "유효하지 않은 GIT URL이 입력되었습니다. 확인 후 다시 시도해주세요.");
		return "error";
	}

	@GetMapping("demo/error/project")
	public String demoProejctError(Model model) {
		model.addAttribute("message", "생성 실패");
		model.addAttribute("messageBody", "죄송합니다. 현재는 Springboot - Java, REST API 프로젝트만 지원됩니다.");
		return "error";
	}

	@GetMapping("demo/error/busy")
	public String demoBusyError(Model model) {
		model.addAttribute("message", "생성 실패");
		model.addAttribute("messageBody", "죄송합니다. 현재 이용자가 많아 생성할 수 없습니다. 잠시 후 시도해주세요.");
		return "error";
	}

}
