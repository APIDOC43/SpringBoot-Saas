package com.hocs.server.saas.oas.service;


import org.springframework.stereotype.Service;

@Service
public interface ApiDocsUiService {

	void generateStaticHtml(String userId);
}
