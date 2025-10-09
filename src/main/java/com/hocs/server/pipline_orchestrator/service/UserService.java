package com.hocs.server.pipline_orchestrator.service;

import com.hocs.server.pipline_orchestrator.entity.ApiDocRequest;
import com.hocs.server.pipline_orchestrator.entity.User;
import com.hocs.server.pipline_orchestrator.repository.UserRepository;
import com.hocs.server.saas_platform.common.exception.CustomException;
import com.hocs.server.saas_platform.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public void addRequest(String userId, String requestId) {
        User user = new User(userId);
        ApiDocRequest request = new ApiDocRequest(requestId);
        user.addApiDocRequest(request);

        userRepository.save(user);
    }

    public List<ApiDocRequest> findRequestIds(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_EXCEPTION))
                .getApiDocRequestList();
    }
}
