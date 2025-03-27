package com.hocs.server.code_parser.repository;

import com.hocs.server.code_parser.core.domain.APISourceDependencyInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface APISourceDependencyRepository extends MongoRepository<APISourceDependencyInfo,String> {
}