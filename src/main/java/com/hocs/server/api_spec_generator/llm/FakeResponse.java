package com.hocs.server.api_spec_generator.llm;



public class FakeResponse {

	public static String pathContent() {
		try {
			Thread.sleep(3000);

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		String original = """
			```yaml
			paths:
			  /api/s3/:
			    post:
			      summary: "파일을 S3에 업로드합니다."
			      x_audience: ["FE", "BE"]
			      requestBody:
			        content:
			          multipart/form-data:
			            schema:
			              type: object
			              properties:
			                file:
			                  type: string
			                  format: binary
			                  description: "업로드할 파일"
			      responses:
			        '201':
			          description: "파일 업로드 성공"
			          content:
			            application/json:
			              schema:
			                $ref: "#/components/schemas/DataResponse"
			        '400':
			          description: "잘못된 요청"
			          content:
			            application/json:
			              schema:
			                type: object
			                properties:
			                  status:
			                    type: string
			                    example: "400"
			                  data:
			                    type: object
			                    properties:
			                      error:
			                        type: string
			                        example: "IOException"
			                      message:
			                        type: string
			                        example: "IOException"
			   
			components:
			  schemas:
			    DataResponse:
			      type: object
			      properties:
			        status:
			          type: integer
			          description: "HTTP 상태 코드"
			          example: 201
			        data:
			          type: object
			          properties:
			            url:
			              type: string
			              description: "업로드된 파일의 URL"
			              example: "https://bucket-name.s3.amazonaws.com/uuid.png"
			```
			""";
		return new String(original.toCharArray());
	}

	public static String fomatValid() {
		try {
			Thread.sleep(3000);

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		String original = """
			yaml
			paths:
			  /api/s3/:
			    post:
			      summary: "S3에 파일 업로드"
			      x_audience: ["FE", "BE"]
			      requestBody:
			        content:
			          multipart/form-data:
			            schema:
			              type: object
			              properties:
			                file:
			                  type: string
			                  format: binary
			                  description: "업로드할 파일 (예: image.png)"
			      responses:
			        '201':
			          description: "파일 업로드 성공"
			          content:
			            application/json:
			              schema:
			                $ref: "#/components/schemas/DataResponse"
			        '400':
			          description: "잘못된 요청"
			          content:
			            application/json:
			              schema:
			                type: object
			                properties:
			                  status:
			                    type: string
			                    description: "HTTP 상태 코드 (예: 400)"
			                    example: "400"
			                  data:
			                    type: object
			                    properties:
			                      error:
			                        type: string
			                        description: "오류 유형 (예: HTTP 상태 이름)"
			                        example: "BAD_REQUEST"
			                      message:
			                        type: string
			                        description: "오류 메시지 (예: 오류 세부사항)"
			                        example: "잘못된 요청입니다."
			   
			components:
			  schemas:
			    DataResponse:
			      type: object
			      properties:
			        status:
			          type: integer
			          description: "HTTP 상태 코드 (예: 201)"
			          example: 201
			        data:
			          type: object
			          properties:
			            url:
			              type: string
			              description: "업로드된 파일의 URL (예: https://bucket-name.s3.amazonaws.com/uuid.png)"
			              example: "https://bucket-name.s3.amazonaws.com/uuid.png"
			```
			""";
		return new String(original.toCharArray());
	}

	public static String createDescrionion() {

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		String original = """
			```yaml
			paths:
			  /api/s3/:
			    post:
			      summary: "S3에 파일 업로드"
			      x_audience: ["FE", "BE"]
			      requestBody:
			        content:
			          multipart/form-data:
			            schema:
			              type: object
			              properties:
			                file:
			                  type: string
			                  format: binary
			                  description: "업로드할 파일 (예: image.png)"
			      responses:
			        '201':
			          description: "파일 업로드 성공"
			          content:
			            application/json:
			              schema:
			                $ref: "#/components/schemas/DataResponse"
			        '400':
			          description: "잘못된 요청"
			          content:
			            application/json:
			              schema:
			                type: object
			                properties:
			                  status:
			                    type: string
			                    description: "HTTP 상태 코드 (예: 400)"
			                    example: "400"
			                  data:
			                    type: object
			                    properties:
			                      error:
			                        type: string
			                        description: "오류 유형 (예: IOException)"
			                        example: "IOException"
			                      message:
			                        type: string
			                        description: "오류 메시지 (예: IOException)"
			                        example: "IOException"
			   
			components:
			  schemas:
			    DataResponse:
			      type: object
			      properties:
			        status:
			          type: integer
			          description: "HTTP 상태 코드 (예: 201)"
			          example: 201
			        data:
			          type: object
			          properties:
			            url:
			              type: string
			              description: "업로드된 파일의 URL (예: https://bucket-name.s3.amazonaws.com/uuid.png)"
			              example: "https://bucket-name.s3.amazonaws.com/uuid.png"
			""";
		return new String(original.toCharArray());
	}
}


