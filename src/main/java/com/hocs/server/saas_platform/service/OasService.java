package com.hocs.server.saas_platform.service;

import com.hocs.server.api_spec_generator.domain.output.OAS;
import com.hocs.server.api_spec_generator.repository.OasRepository;
import com.hocs.server.pipline_orchestrator.entity.ApiDocRequest;
import com.hocs.server.pipline_orchestrator.service.UserService;
import com.hocs.server.saas_platform.common.exception.CustomException;
import com.hocs.server.saas_platform.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OasService {

    private final UserService userService;
    private final OasRepository oasRepository;

    public List<OAS> findAllByUserId(String userId) {
        log.info("findAllByUserId(userId:{})",userId);
        List<ApiDocRequest> requestIds = userService.findRequestIds(userId);

        return requestIds.stream().map(m->getOas(m.getRequestId()))
                .collect(Collectors.toList());
    }

    private OAS getOas(String requestId) {
        log.info("getOas(requestId:{})",requestId);
        return oasRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_EXCEPTION));
    }
}
