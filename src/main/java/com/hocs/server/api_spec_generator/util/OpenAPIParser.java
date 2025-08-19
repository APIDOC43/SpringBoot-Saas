package com.hocs.server.api_spec_generator.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hocs.server.api_spec_generator.domain.output.PathAndComponents;
import com.hocs.server.api_spec_generator.domain.output.PathItem;
import com.hocs.server.api_spec_generator.domain.output.Schema;

public class OpenAPIParser {
    
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    
    public static PathAndComponents parse(String oasContent) throws JsonProcessingException {
        if (oasContent == null) {
            throw new IllegalArgumentException("OAS content cannot be null");
        }
        if (oasContent.trim().isEmpty()) {
            throw new IllegalArgumentException("OAS content cannot be empty");
        }
        
        try {
            return yamlMapper.readValue(oasContent, PathAndComponents.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse OAS content", e);
        }
    }

    // 테스트 호환성을 위한 메소드 - JSON과 YAML 모두 지원
    public PathAndComponents parsePathAndComponents(String content) throws JsonProcessingException {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
        if (content.trim().isEmpty()) {
            throw new RuntimeException("Content cannot be empty");
        }
        
        try {
            // JSON 형식인지 확인 (첫 번째 문자가 { 또는 [인 경우)
            String trimmedContent = content.trim();
            if (trimmedContent.startsWith("{") || trimmedContent.startsWith("[")) {
                return jsonMapper.readValue(content, PathAndComponents.class);
            } else {
                return yamlMapper.readValue(content, PathAndComponents.class);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse content", e);
        }
    }

    public static Schema parseToSchema(String oasYaml) throws JsonProcessingException {
        if (oasYaml == null || oasYaml.trim().isEmpty()) {
            throw new IllegalArgumentException("OAS YAML cannot be null or empty");
        }
        
        try {
            return yamlMapper.readValue(oasYaml, Schema.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse schema", e);
        }
    }

    public static PathItem parseToPath(String integrationPath) {
        if (integrationPath == null || integrationPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Integration path cannot be null or empty");
        }
        
        try {
            return yamlMapper.readValue(integrationPath, PathItem.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse path", e);
        }
    }
}
