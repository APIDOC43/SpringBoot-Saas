package com.hocs.server.saas_platform.service.external.git.port;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;


public interface PrivateRepositoryClonePort {

	void gitPrivateClone(OAuth2AuthenticationToken authentication);

}
