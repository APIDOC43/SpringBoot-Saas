package com.hocs.server.pipline_orchestrator.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class ApiDocRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String requestId;

    public ApiDocRequest(String requestId) {
        this.requestId = requestId;
    }

    public void setApiDoc(User user) {
        this.user = user;
    }
}
