package com.hocs.server.openai.llm;


import com.hocs.server.openai.domain.APIEntry;
import com.hocs.server.saas.model.Schema;
import java.util.List;

public class PromptMessageHub {

	public static String createOasDescriptionDetail(APIEntry apiEntry, String oas) {
		return "API: " + apiEntry.getMethod() + " \'" + apiEntry.getAPI()+"\' 에 대한\n"+"""
			OAS(OpenAPI Specification) 3.0.0 파일을 수정하려고 한다.
			\n"""+oas+"\n"+"""
						
			1. 답변은 yaml코드만 한다.
						
			2. description은 반드시 첨부된 소스코드에 근거하여 작성한다.
			3. description에 첨부된 소스코드 enum 데이터를 활용해서 가능한 값들을 나열한다.
			4. description에 참조된 소스코드에서 얻을 수 있는 데이터를 활용한다.(enum등) 예시값. 주의사항. 추가사항. 명세에 노출되지 않는 데이터타입(ex:LocalDateTime)예시 등.
						
						
			5. 모든 description 값은  이런식으로 `쌍따옴표 표현식`을 반드시 지킨다. 예시 : `description: "사용자 ID (예: 12345)"` , description 예시는 첨부된 소스코드에서 찾는다.
			6. properties 아래 형식을 지킨다 value는 첨부된 소스코드를 이용해 채운다.
			7. properties:
			        code:
			          type: 
			          description: 
			          example: 
						
			8. components.schemas에서 아래와 같이 내부에 또다른 schema를 참조하는 경우 OAS형식을 무시하고 description을 포함한다.
			         address:
			           $ref: "#/components/schemas/Address"
			           description: "주소"
			9. properties는 첨부된 소스코드를 통해 example필드를 작성한다.
			properties:
			        code:
			          type: 
			          description:
			          example: 
			request/response 명세는 참조형식으로 "$ref:" 키워드를 이용하여 구성한다.
			10. summary의 description은 30자이내로 요약해서 작성한다.
						
						
			""";
	}


	public static String createOasPathSection(APIEntry apiEntry, String srcRelationErrorForamt) {
		String promptPath = """
			목표 :
			```
			""" + "API: " + apiEntry.getMethod() + " \'" + apiEntry.getAPI()+"\' 에 대해서\n" + """
			OAS(Open api spec) 3.0.0 파일의 path만을 작성하려고 한다.
			```

			조건:
			```

			1. OAS 구성요소 중 상단 "Info" 등 명세 메타 데이터는 작성하지 않는다.
			2. 소스코드 기반으로 근거있게 작성하고, description같은건 너가 추측해서 한글로 작성가능
			3. description 작성할때. 첨부된 소스코드 enum을 활용해서 가능한 값들을 나열한다.
			4. 단 summary의 description은 30자이내로 요약해서 작성한다.
			4. 답변은 yaml코드만 한다.
			5. "post:|get|patch|option|delete|put" 등, HTTP Method 표현 요소 아래 "x_audience" 추측해서 작성한다. "x_audience"는 문서 대상 독자 "FE,BE,ADMIN,ALL"중 선택하고 배열형식으로 표현한다.
			x_audience: ["FE"]
			6. description은 참조된 소스코드에서 얻을 수 있는 데이터를 활용한다. 예시값. 주의사항. 추가사항. 명세에 노출되지 않는 데이터타입(ex: LocalDateTime)예시 등.
			7. request/response 명세는 참조형식으로 "$ref:" 키워드를 이용하여 구성한다.
			
			8. responses 하위 계층은 아래 계층 구조를 반드시 지켜야 한다. '{}'안에는 너가 채워야해
			          '{Http Status code}':
			            description: {description}
			            content:
			              application/json:
			                schema:
			                  $ref: "#/components/schemas/{your schema name}}"
			9. 단 예외명세는 ref-component기능을 이용하지 않는다. path마다 개별적인 excpetion
			10. schema 이름은 첨부된 소스코드의 DTO 클래스명을 활용한다.
			11. 모든 description 값은  이런식으로 `쌍따옴표 표현식`을 반드시 지킨다. 예시 : `description: "사용자 ID (예: 12345)"` , description 예시는 첨부된 소스코드에서 찾는다.
			12. $ref에 매칭되는 component는 반드시 작성되어야 한다.
			13. components.schemas에서 아래와 같이 내부에 또다른 schema를 참조하는 경우 OAS형식을 무시하고 description을 포함한다.
			         address:
			           $ref: "#/components/schemas/Address"
			           description: "주소"
			14. properties는 첨부된 소스코드를 통해 example필드를 작성한다.'{}' 안에 내용은 너가 채워야한다.
			properties:
			        {propertyName}:
			          type: {propertyType}
			          description: {description}
			          example: {example}

			15. Multipart/form-data 같은 특수한 형식의 Request는 예제 request를 작성하지 않는다.
			```

			데이터
			```
			아래는 """ + " "+apiEntry.getMethod() + " \'" + apiEntry.getAPI()+"\' API가 실행될때 필요한 소스코드들이다."+
			"\n"+apiEntry.getSrc()+"\n "+srcRelationErrorForamt+"```";
		return promptPath;
	}

	public static String createOasBasedSnippet(String totalContent) {
		return """
			목표 :
			```
			조각난 OAS yaml파일을 조합하여 OAS(open api spec) 3.0.0 파일 전체를 생성한다.
			```

			조건:
			```
			1.데이터를 기반으로 적절하게 재구성하여 OAS 3.0.0 문법에 호환되도록 구성한다.
			2.생략없이 전체코드를 답변하라.
			3.답변은 yaml파일만 답변하라.
			4.단 x_audience 키는 예외로 반드시 유지하라.(특이사항)
			5.중복되는 요소는 하나로 중복을 제거한다.
			6.responses 하위 계층은 아래 계층 구조를 반드시 지켜야 한다.
			               '{Http Status code}':
			            description: {description}
			            content:
			              application/json:
			                schema:
			                  $ref: "#/components/schemas/{your schema name}}"
			7. components.schemas에서 아래와 같이 내부에 또다른 schema를 참조하는 경우 OAS형식을 무시하고 description을 포함한다.(특이사항)
			         address:
			           $ref: "#/components/schemas/Address"
			           description: "주소"
			8.마지막점검으로 특이사항을 제외하고 OAS문법을 준수했는지 확인한다.
						
			```

			데이터
			```
			아래 yaml을 기반으로 조합하여 으로 OAS파일 전체를 생성한다. 설명은 한글로 작성
			"""+"\n"+totalContent+"\n```";
	}

	public static String integrationSchema(List<Schema> schema) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < schema.size(); i++){
			sb.append(i+1).append("번 schema").append("\n")
				.append(schema.get(i).toString()).append("\n\n");
		}

		return """
			   
						아래는 OAS(open api spec) 3.0.0문법을 가지는 데이터 에서 Components 일부분이다.
						
						1. 첨부되는 데이터는 동일한 Components에 대한 schema이다.
						그러므로 여러개의 schema를 통합하여 하나의 schema로 만들어라.
						
						2. 병합시, 누락되는 것 없이 (일종의 union연산) 진행하라.
						3. 모든 description 값은 `description: "{description}"` 이런식으로 쌍따옴표 표현식을 반드시 지킨다. 
						4. 결과물에 ref는 $ref로 표현한다.
						5. 답변은 병합된 yaml코드만 한다.
						6. description에 " 따옴표 String 표현식으로 구성해야한다.
						6. description에 " 따옴표 String 표현식으로 구성해야한다.
						6. description에 " 따옴표 String 표현식으로 구성해야한다.
						
			   
			"""+sb;
	}

	public static String vaildErrorResponseFormat(String srcRelatedError, String result) {
		return """
						
			아래는 API 하나에 대한 OAS 명세이다.
			"""+
			result+
			"""			
			추가로 첨부된 소스코드에서 API OAS명세를 개선하라.
						
			
			예외
			를 담당하는 명세를 소스코드와 일치하도록 수정하라.		
			소스코드에서 실제 실행시 반환하게 되는 response 형태를 확인하여 일치시킨다..
			excpetion response 명세는 ref를 사용하지 않고 코드기반의 정확한 명세를 개별적으로 진핸한다.
			
			답변은 개선된 yaml파일만 한다.
			'
			paths:
			components:
			'
			만 생성한다.
			
			
			소스코드:			
			"""+"apiEntry.getSrc()"+"\n"+srcRelatedError;
	}

	public static String findRelationExceptionFormat(String output) {
		return """
   			아래는 Java SpringBoot 프로젝트의 src/java 디렉토리의 구조에 대한 정보이다.
   			예외발생시 반환되는 response 형식을 찾으려고 한다.
   			
			1. 첨부된 데이터를 참고하여 예외발생시 응답 형식을 지정하는 것과 관련있는 클래스를 찾아라.
			3. 답변은 해당하는 클래스의 절대경로만 답변한다.
			4. 답변에 파일명 구분자는 ',' 를 이용한다.
			
			답변은 파일 경로만 한다. 어떤한 문장도  포함하지 않는다.
			오직 구분자(',')를 포함한 파일 절대경로만 답변한다.
			"""+"\n"+output;
	}
}


