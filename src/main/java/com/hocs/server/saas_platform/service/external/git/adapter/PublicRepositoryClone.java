package com.hocs.server.saas_platform.service.external.git.adapter;

import com.hocs.server.saas_platform.domain.GitRepo;
import com.hocs.server.saas_platform.service.external.git.port.PublicRepositoryClonePort;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicRepositoryClone implements PublicRepositoryClonePort {

	@Override
	public Optional<Path> gitClone(GitRepo gitRepo){
		try {
			Path path = Files.createTempDirectory("cloneRepo");

			Git.cloneRepository()
				.setURI(gitRepo.getUrl())
				.setDirectory(path.toFile())
				.call();

			return Optional.of(path);
		} catch (GitAPIException gitAPIException) {
			gitAPIException.printStackTrace();
			log.error("gitHubService.clone throw GitAPIException");
		}catch (IOException ioException){
			ioException.printStackTrace();
			log.error("gitHubService.clone throw ioException");
		}

		return Optional.empty();
	}
}
