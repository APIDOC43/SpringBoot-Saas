package com.hocs.server.pipline_orchestrator.dto;

import com.hocs.server.api_spec_generator.domain.input.APIMetadata;

public record ApiMetadataResult(PreProcessResult preProcessResult, APIMetadata metadata) {
}
