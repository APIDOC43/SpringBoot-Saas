package com.hocs.server.api_spec_generator.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hocs.server.api_spec_generator.domain.output.PathAndComponents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenAPIParserTest {

    private OpenAPIParser openAPIParser;

    @BeforeEach
    void setUp() {
        openAPIParser = new OpenAPIParser();
    }

    @Test
    @DisplayName("유효한 JSON 문자열을 PathAndComponents로 파싱할 수 있다")
    void shouldParseValidJsonToPathAndComponents() throws JsonProcessingException {
        // given
        String validJson = """
            {
                "paths": {
                    "/users": {
                        "get": {
                            "summary": "Get users",
                            "operationId": "getUsers"
                        }
                    }
                },
                "components": {
                    "schemas": {
                        "User": {
                            "type": "object",
                            "properties": {
                                "id": {
                                    "type": "integer"
                                }
                            }
                        }
                    }
                }
            }
            """;

        // when
        PathAndComponents result = openAPIParser.parsePathAndComponents(validJson);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPaths()).isNotNull();
        assertThat(result.getComponents()).isNotNull();
    }

    @Test
    @DisplayName("빈 JSON 객체를 파싱할 수 있다")
    void shouldParseEmptyJsonObject() throws JsonProcessingException {
        // given
        String emptyJson = "{}";

        // when
        PathAndComponents result = openAPIParser.parsePathAndComponents(emptyJson);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("잘못된 JSON 형식에 대해 예외를 발생시킨다")
    void shouldThrowExceptionForInvalidJson() {
        // given
        String invalidJson = "{ invalid json }";

        // when & then
        assertThatThrownBy(() -> openAPIParser.parsePathAndComponents(invalidJson))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("null 입력에 대해 예외를 발생시킨다")
    void shouldThrowExceptionForNullInput() {
        // given
        String nullJson = null;

        // when & then
        assertThatThrownBy(() -> openAPIParser.parsePathAndComponents(nullJson))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("빈 문자열 입력에 대해 예외를 발생시킨다")
    void shouldThrowExceptionForEmptyString() {
        // given
        String emptyString = "";

        // when & then
        assertThatThrownBy(() -> openAPIParser.parsePathAndComponents(emptyString))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("paths만 있는 JSON을 파싱할 수 있다")
    void shouldParseJsonWithPathsOnly() throws JsonProcessingException {
        // given
        String jsonWithPathsOnly = """
            {
                "paths": {
                    "/health": {
                        "get": {
                            "summary": "Health check"
                        }
                    }
                }
            }
            """;

        // when
        PathAndComponents result = openAPIParser.parsePathAndComponents(jsonWithPathsOnly);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPaths()).isNotNull();
    }

    @Test
    @DisplayName("components만 있는 JSON을 파싱할 수 있다")
    void shouldParseJsonWithComponentsOnly() throws JsonProcessingException {
        // given
        String jsonWithComponentsOnly = """
            {
                "components": {
                    "schemas": {
                        "Error": {
                            "type": "object"
                        }
                    }
                }
            }
            """;

        // when
        PathAndComponents result = openAPIParser.parsePathAndComponents(jsonWithComponentsOnly);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getComponents()).isNotNull();
    }
}
