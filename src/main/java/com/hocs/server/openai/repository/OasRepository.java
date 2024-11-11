package com.hocs.server.openai.repository;

import com.hocs.server.openai.domain.output.OAS;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OasRepository extends MongoRepository<OAS,String> {

}
