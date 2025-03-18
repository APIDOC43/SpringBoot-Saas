package com.hocs.server.saas_platform.repository;

import com.hocs.server.common.domain.ProjectMetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientProjectMetadataRepository extends JpaRepository<ProjectMetaData,Long> {

}