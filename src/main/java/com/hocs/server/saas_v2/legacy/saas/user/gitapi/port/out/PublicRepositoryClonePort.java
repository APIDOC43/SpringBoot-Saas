package com.hocs.server.saas_v2.legacy.saas.user.gitapi.port.out;


import com.hocs.server.saas_v2.legacy.saas.user.gitapi.domin.GitRepo;
import java.nio.file.Path;
import java.util.Optional;

public interface PublicRepositoryClonePort {
	Optional<Path> gitClone(GitRepo gitRepo);
}
