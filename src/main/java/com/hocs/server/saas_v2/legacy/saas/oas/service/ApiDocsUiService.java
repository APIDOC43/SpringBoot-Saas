package com.hocs.server.saas_v2.legacy.saas.oas.service;


import org.springframework.stereotype.Service;

@Service
public interface ApiDocsUiService {

	void generateStaticHtml(String userId);
}
