package com.hocs.server.saas_v2.domain;

import java.nio.file.Path;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientProjectPath {
	private Path url;
}