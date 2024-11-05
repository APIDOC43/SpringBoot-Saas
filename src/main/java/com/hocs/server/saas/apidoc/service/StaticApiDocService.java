package com.hocs.server.saas.apidoc.service;

import com.hocs.server.saas.user.oauth.dto.FilesData;
import com.hocs.server.saas.user.oauth.dto.GetContentRequest;
import java.nio.file.Path;
import java.util.List;

public interface StaticApiDocService {

	List<FilesData> findApiListByUserId(String userId);

	Path getContent(GetContentRequest request);
}
