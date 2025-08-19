package com.hocs.server.common.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestEntity {
    private String id;
    private String data;

    public String getId() {
        return this.id;
    }
}