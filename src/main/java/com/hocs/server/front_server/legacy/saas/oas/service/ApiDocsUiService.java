package com.hocs.server.front_server.legacy.saas.oas.service;


import org.springframework.stereotype.Service;

@Service
public interface ApiDocsUiService {

	void generateStaticHtml(String userId);
}
