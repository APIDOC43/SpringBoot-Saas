package com.hocs.server.saas_platform.controller;


import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping("/")
	public void index(HttpServletResponse response) throws IOException {
		response.sendRedirect("https://apidoc43.softr.app");
	}










}

