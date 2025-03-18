package com.hocs.server.saas_platform.service.external.git.port;


import com.hocs.server.saas_platform.domain.GitRepo;
import java.nio.file.Path;
import java.util.Optional;

public interface PublicRepositoryClonePort {
	Optional<Path> gitClone(GitRepo gitRepo);
}
