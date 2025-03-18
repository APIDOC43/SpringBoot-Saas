package com.hocs.server.common.aop;

import com.hocs.server.api_spec_generator.llm.exception.ApiEntriesNullException;
import com.hocs.server.api_spec_generator.llm.exception.LLMException;
import com.hocs.server.common.exception.GithubCloneException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(LLMException.class)
	public String handleNullPointerException(LLMException ex) {
		log.info("LLMException: {}", ex.getMessage());
		return "redirect:/demo/error/busy";
	}

	@ExceptionHandler(IllegalStateException.class)
	public String handleIllegal(IllegalStateException ex) {
		log.info("handleIllegal: {}", ex.getMessage());
		return "redirect:/demo/error/project";
	}

	@ExceptionHandler(ApiEntriesNullException.class)
	public String apiEntriesNullException(ApiEntriesNullException ex) {
		log.info("ApiEntriesNullException: {}", ex.getMessage());
		return "redirect:/demo/error/project";
	}

	@ExceptionHandler(GithubCloneException.class)
	public String githubCloneException(GithubCloneException ex) {
		log.info("GithubCloneException: {}", ex.getMessage());
		return "redirect:/demo/error/git";
	}

}
