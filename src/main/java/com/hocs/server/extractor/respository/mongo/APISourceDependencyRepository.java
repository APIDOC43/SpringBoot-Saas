package com.hocs.server.extractor.respository.mongo;

import com.hocs.server.extractor.domain.APISourceDependencyInfo;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface APISourceDependencyRepository extends MongoRepository<APISourceDependencyInfo, String> {

	Optional<APISourceDependencyInfo> findByUserId(String userId);
}
