package com.hocs.server.front_server.legacy.saas.user.gitapi.port.out;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;


public interface PrivateRepositoryClonePort {

	void gitPrivateClone(OAuth2AuthenticationToken authentication);

}
