package com.hocs.server.api_spec_generator.repository;

import com.hocs.server.api_spec_generator.domain.output.OAS;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OasRepository extends MongoRepository<OAS,String> {

}
