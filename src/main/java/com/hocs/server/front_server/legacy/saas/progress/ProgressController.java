package com.hocs.server.front_server.legacy.saas.progress;

import com.hocs.server.openai.util.MemoryProcessPercentage;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ProgressController {
	@GetMapping("/demo/progress")
	public String progress(@RequestParam(name = "repoUrl") String param, Model model) {
		model.addAttribute("param", param);

		return "progress";
	}

	@ResponseBody
	@GetMapping("/demo/progress/{userId}")
	public String progressDemo(@PathVariable(name = "userId") String userId) {

		return String.valueOf(MemoryProcessPercentage.get(userId));
	}

	@GetMapping("/demo/metadata")
	public String insertMetadata(@RequestParam(name = "repoUrl") String param, Model model) {
		model.addAttribute("param", param);
		List<String> languages = Arrays.asList("JAVA");
		List<String> frameworks = Arrays.asList("SPRINGBOOT");

		model.addAttribute("languages", languages);
		model.addAttribute("frameworks", frameworks);
		return "metadata";
	}
}
