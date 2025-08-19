package com.hocs.server;

// 먼저 build.gradle에 추가해야 할 의존성:
// implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'
// implementation 'io.swagger.core.v3:swagger-annotations:2.2.15'

// src/test/java/com/hocs/server/OpenApiGeneratorTest.java

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.springframework.ai.openai.api.OpenAiApi.ChatModel.GPT_4_O;

@SpringBootTest
public class OpenApiGeneratorTest {

    @Value("${openai.api-key:}")
    private String apiKey;

    // 가짜 코드 예제들
    private final String FAKE_CONTROLLER_CODE = """
        @RestController
        @RequestMapping("/api/users")
        public class UserController {
            
            @Autowired
            private UserService userService;
            
            @GetMapping
            public ResponseEntity<List<User>> getAllUsers() {
                List<User> users = userService.getAllUsers();
                return ResponseEntity.ok(users);
            }
            
            @GetMapping("/{id}")
            public ResponseEntity<User> getUserById(@PathVariable Long id) {
                User user = userService.getUserById(id);
                return ResponseEntity.ok(user);
            }
            
            @PostMapping
            public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
                User user = userService.createUser(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(user);
            }
            
            @PutMapping("/{id}")
            public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
                User user = userService.updateUser(id, request);
                return ResponseEntity.ok(user);
            }
            
            @DeleteMapping("/{id}")
            public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
                userService.deleteUser(id);
                return ResponseEntity.noContent().build();
            }
        }
        """;

    private final String FAKE_SERVICE_CODE = """
        @Service
        @RequiredArgsConstructor
        public class UserService {
            
            private final UserRepository userRepository;
            
            public List<User> getAllUsers() {
                return userRepository.findAll();
            }
            
            public User getUserById(Long id) {
                return userRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
            }
            
            public User createUser(CreateUserRequest request) {
                User user = User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .age(request.getAge())
                    .build();
                return userRepository.save(user);
            }
            
            public User updateUser(Long id, UpdateUserRequest request) {
                User user = getUserById(id);
                user.setName(request.getName());
                user.setEmail(request.getEmail());
                user.setAge(request.getAge());
                return userRepository.save(user);
            }
            
            public void deleteUser(Long id) {
                User user = getUserById(id);
                userRepository.delete(user);
            }
        }
        """;

    private final String FAKE_REPOSITORY_CODE = """
        @Repository
        public interface UserRepository extends JpaRepository<User, Long> {
            
            Optional<User> findByEmail(String email);
            
            List<User> findByAgeGreaterThan(Integer age);
            
            List<User> findByNameContainingIgnoreCase(String name);
            
            @Query("SELECT u FROM User u WHERE u.createdAt > :date")
            List<User> findRecentUsers(@Param("date") LocalDateTime date);
        }
        """;

    private final String FAKE_ENTITY_CODE = """
        @Entity
        @Table(name = "users")
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public class User {
            
            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long id;
            
            @Column(nullable = false)
            private String name;
            
            @Column(nullable = false, unique = true)
            private String email;
            
            @Column
            private Integer age;
            
            @CreationTimestamp
            private LocalDateTime createdAt;
            
            @UpdateTimestamp
            private LocalDateTime updatedAt;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public class CreateUserRequest {
            private String name;
            private String email;
            private Integer age;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public class UpdateUserRequest {
            private String name;
            private String email;
            private Integer age;
        }
        """;

    // OpenAPI JSON Schema 정의
    private String createOpenApiJsonSchema() {
        return """
        {
          "type": "object",
          "required": ["openapi", "info", "paths"],
          "properties": {
            "openapi": {
              "type": "string",
              "pattern": "^3\\.0\\.\\d(-.+)?$",
              "description": "The semantic version number of the OpenAPI Specification."
            },
            "info": {
              "type": "object",
              "required": ["title", "version"],
              "properties": {
                "title": { "type": "string" },
                "description": { "type": "string" },
                "version": { "type": "string" }
              },
              "additionalProperties": true
            },
            "paths": {
              "type": "object",
              "additionalProperties": {
                "type": "object",
                "properties": {
                  "get": { "$ref": "#/$defs/operation" },
                  "put": { "$ref": "#/$defs/operation" },
                  "post": { "$ref": "#/$defs/operation" },
                  "delete": { "$ref": "#/$defs/operation" },
                  "patch": { "$ref": "#/$defs/operation" }
                },
                "additionalProperties": false
              }
            },
            "components": {
              "type": "object",
              "properties": {
                "schemas": {
                  "type": "object",
                  "additionalProperties": {
                    "type": "object",
                    "properties": {
                      "type": { "type": "string" },
                      "properties": { "type": "object" },
                      "required": { 
                        "type": "array",
                        "items": { "type": "string" }
                      }
                    }
                  }
                }
              },
              "additionalProperties": true
            }
          },
          "$defs": {
            "operation": {
              "type": "object",
              "properties": {
                "summary": { "type": "string" },
                "description": { "type": "string" },
                "parameters": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "name": { "type": "string" },
                      "in": { "type": "string", "enum": ["query", "header", "path", "cookie"] },
                      "required": { "type": "boolean" },
                      "description": { "type": "string" },
                      "schema": {
                        "type": "object",
                        "properties": {
                          "type": { "type": "string" }
                        }
                      }
                    },
                    "required": ["name", "in"]
                  }
                },
                "responses": {
                  "type": "object",
                  "additionalProperties": {
                    "type": "object",
                    "properties": {
                      "description": { "type": "string" },
                      "content": {
                        "type": "object",
                        "additionalProperties": {
                          "type": "object",
                          "properties": {
                            "schema": {
                              "type": "object",
                              "properties": {
                                "type": { "type": "string" },
                                "$ref": { "type": "string" }
                              }
                            }
                          }
                        }
                      }
                    },
                    "required": ["description"]
                  }
                }
              },
              "required": ["responses"],
              "additionalProperties": true
            }
          },
          "additionalProperties": true
        }
        """;
    }

    private OpenAiApi.ChatCompletionRequest.ResponseFormat createStructuredResponseFormat(String name, String schema) {
        try {
            return new OpenAiApi.ChatCompletionRequest.ResponseFormat(
                    OpenAiApi.ChatCompletionRequest.ResponseFormat.Type.JSON_SCHEMA,
                    schema
            );
        } catch (Exception e) {
            throw new RuntimeException("JSON Schema 파싱 오류: " + e.getMessage(), e);
        }
    }

    @Test
    public void testGenerateOpenApiFromCode() {
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("OpenAI API 키가 설정되지 않았습니다. application.properties에 openai.api-key를 설정해주세요.");
            return;
        }

        System.out.println("=== OpenAPI 문서 생성 테스트 시작 ===");

        OpenAiApi openAiApi = new OpenAiApi(apiKey);
        String jsonSchema = createOpenApiJsonSchema();

        ChatModel chatModel = new OpenAiChatModel(openAiApi,
                OpenAiChatOptions.builder()
                        .withModel(GPT_4_O)
                        .withResponseFormat(createStructuredResponseFormat("openapi_spec", jsonSchema))
                        .withTemperature(0.3F)
                        .build());

        ChatClient client = ChatClient.builder(chatModel).build();

        String prompt = createPromptForOpenApiGeneration();

        try {
            // OpenAPI 객체로 직접 변환 시도
            OpenAPI result = client.prompt()
                    .user(prompt)
                    .call()
                    .entity(OpenAPI.class);

            System.out.println("생성된 OpenAPI 문서:");
            System.out.println("Title: " + result.getInfo().getTitle());
            System.out.println("Version: " + result.getInfo().getVersion());
            System.out.println("OpenAPI Version: " + result.getOpenapi());
            System.out.println("Paths: " + result.getPaths().keySet());

            // 각 경로의 상세 정보 출력
            result.getPaths().forEach((path, pathItem) -> {
                System.out.println("\nPath: " + path);
                if (pathItem.getGet() != null) {
                    System.out.println("  GET - " + pathItem.getGet().getSummary());
                }
                if (pathItem.getPost() != null) {
                    System.out.println("  POST - " + pathItem.getPost().getSummary());
                }
                if (pathItem.getPut() != null) {
                    System.out.println("  PUT - " + pathItem.getPut().getSummary());
                }
                if (pathItem.getDelete() != null) {
                    System.out.println("  DELETE - " + pathItem.getDelete().getSummary());
                }
            });

        } catch (Exception e) {
            System.err.println("OpenAPI 생성 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String createPromptForOpenApiGeneration() {
        return String.format("""
            다음 Spring Boot 코드들을 분석하여 OpenAPI 3.0 명세서를 생성해주세요.
            
            Controller 코드:
            %s
            
            Service 코드:
            %s
            
            Repository 코드:
            %s
            
            Entity 코드:
            %s
            
            요구사항:
            1. 모든 API 엔드포인트를 포함해야 합니다
            2. 각 엔드포인트의 HTTP 메서드, 경로, 파라미터, 응답을 정확히 정의해야 합니다
            3. Request/Response 스키마를 components.schemas에 정의해야 합니다
            4. 적절한 HTTP 상태 코드와 응답 형식을 포함해야 합니다
            5. API 제목은 "User Management API", 버전은 "1.0.0"으로 설정해주세요
            6. OpenAPI 버전은 "3.0.1"을 사용해주세요
            
            JSON Schema에 맞는 정확한 OpenAPI 문서를 생성해주세요.
            """,
                FAKE_CONTROLLER_CODE,
                FAKE_SERVICE_CODE,
                FAKE_REPOSITORY_CODE,
                FAKE_ENTITY_CODE
        );
    }

    @Test
    public void testManualOpenApiCreation() {
        System.out.println("=== 수동 OpenAPI 객체 생성 테스트 ===");

        // 수동으로 OpenAPI 객체 생성하여 구조 확인
        OpenAPI openAPI = new OpenAPI()
                .openapi("3.0.1")
                .info(new Info()
                        .title("User Management API")
                        .description("사용자 관리를 위한 REST API")
                        .version("1.0.0"))
                .paths(new Paths()
                        .addPathItem("/api/users", new PathItem()
                                .get(new Operation()
                                        .summary("모든 사용자 조회")
                                        .description("시스템에 등록된 모든 사용자를 조회합니다")
                                        .responses(new ApiResponses()
                                                .addApiResponse("200", new ApiResponse()
                                                        .description("성공적으로 사용자 목록을 조회했습니다")
                                                        .content(new Content()
                                                                .addMediaType("application/json", new MediaType()
                                                                        .schema(new Schema<>()
                                                                                .type("array")
                                                                                .items(new Schema<>().$ref("#/components/schemas/User"))))))))
                                .post(new Operation()
                                        .summary("새 사용자 생성")
                                        .description("새로운 사용자를 생성합니다")
                                        .responses(new ApiResponses()
                                                .addApiResponse("201", new ApiResponse()
                                                        .description("사용자가 성공적으로 생성되었습니다")
                                                        .content(new Content()
                                                                .addMediaType("application/json", new MediaType()
                                                                        .schema(new Schema<>().$ref("#/components/schemas/User"))))))))
                        .addPathItem("/api/users/{id}", new PathItem()
                                .get(new Operation()
                                        .summary("특정 사용자 조회")
                                        .description("ID로 특정 사용자를 조회합니다")
                                        .addParametersItem(new Parameter()
                                                .name("id")
                                                .in("path")
                                                .required(true)
                                                .description("사용자 ID")
                                                .schema(new Schema<>().type("integer").format("int64")))
                                        .responses(new ApiResponses()
                                                .addApiResponse("200", new ApiResponse()
                                                        .description("사용자 조회 성공")
                                                        .content(new Content()
                                                                .addMediaType("application/json", new MediaType()
                                                                        .schema(new Schema<>().$ref("#/components/schemas/User")))))))));

        System.out.println("수동 생성된 OpenAPI:");
        System.out.println("Title: " + openAPI.getInfo().getTitle());
        System.out.println("Version: " + openAPI.getInfo().getVersion());
        System.out.println("OpenAPI Version: " + openAPI.getOpenapi());
        System.out.println("Paths: " + openAPI.getPaths().keySet());

        // 구조 검증
        assert openAPI.getOpenapi().equals("3.0.1");
        assert openAPI.getInfo().getTitle().equals("User Management API");
        assert openAPI.getPaths().containsKey("/api/users");
        assert openAPI.getPaths().containsKey("/api/users/{id}");

        System.out.println("수동 OpenAPI 객체 생성 및 검증 완료!");
    }
}
