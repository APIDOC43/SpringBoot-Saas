package com.hocs.server.saas_v2.repository;

import com.hocs.server.common.domain.DocGeneratePiplineTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocGeneratePiplineTaskRepository extends JpaRepository<DocGeneratePiplineTask,Long> {

}
