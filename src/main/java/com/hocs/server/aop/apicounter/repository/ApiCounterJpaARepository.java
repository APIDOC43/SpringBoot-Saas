package com.hocs.server.aop.apicounter.repository;

import com.hocs.server.aop.apicounter.entity.ApiCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiCounterJpaARepository extends JpaRepository<ApiCounter,String>{

}
