package com.hocs.server.saas_platform.service;


import org.springframework.stereotype.Service;

@Service
public interface ApiDocsUiService {

	void generateStaticHtml(String userId);
}
