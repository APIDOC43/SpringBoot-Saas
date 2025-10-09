package com.hocs.server.pipline_orchestrator.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ApiDocRequest> apiDocRequestList = new ArrayList<>();

    public User(String userId) {
        this.userId = userId;
    }

    // 연관관계 편의 메소드
    public void addApiDocRequest(ApiDocRequest request) {
        apiDocRequestList.add(request);
        request.setApiDoc(this);
    }
}
