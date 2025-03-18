package com.hocs.server.util;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hocs.server.api_spec_generator.domain.output.PathAndComponents;
import com.hocs.server.api_spec_generator.util.OpenAPIParser;
import com.hocs.server.api_spec_generator.domain.output.MediaType;
import com.hocs.server.api_spec_generator.domain.output.Operation;
import com.hocs.server.api_spec_generator.domain.output.Parameter;
import com.hocs.server.api_spec_generator.domain.output.PathItem;
import com.hocs.server.api_spec_generator.domain.output.Response;
import com.hocs.server.api_spec_generator.domain.output.Schema;
import org.junit.jupiter.api.Test;

public class PathAndComponentsParserTest {

	@Test
	public void testParseOpenAPI() throws JsonProcessingException {
		String oasYaml = """
			paths:
			  /example:
			    get:
			      summary: "Example GET"
			      description: "Get example item"
			      operationId: "getExample"
			      tags:
			        - "examples"
			      parameters:
			        - name: "id"
			          in: "query"
			          description: "ID of the example"
			          required: true
			          schema:
			            type: "string"
			      responses:
			        '200':
			          description: "successful operation"
			          content:
			            application/json:
			              schema:
			                $ref: "#/components/schemas/ExampleSchema"
			components:
			  schemas:
			    ExampleSchema:
			      type: "object"
			      description: "Example schema"
			      properties:
			        name:
			          type: "string"
			        value:
			          type: "integer"
			""";

		PathAndComponents pathAndComponents = OpenAPIParser.parse(oasYaml);

//		assertNotNull(pathAndComponents);
//		assertEquals("Sample API", pathAndComponents.getInfo().getTitle());
//		assertEquals("This is a sample API", pathAndComponents.getInfo().getDescription());
//		assertEquals("1.0.0", pathAndComponents.getInfo().getVersion());

		assertTrue(pathAndComponents.getPaths().containsKey("/example"));
		PathItem examplePath = pathAndComponents.getPaths().get("/example");
		assertNotNull(examplePath.getGet());
		assertEquals("Example GET", examplePath.getGet().getSummary());
		assertEquals("Get example item", examplePath.getGet().getDescription());
		assertEquals("getExample", examplePath.getGet().getOperationId());
		assertEquals("examples", examplePath.getGet().getTags().get(0));

		assertNotNull(examplePath.getGet().getParameters());
		Parameter param = examplePath.getGet().getParameters().get(0);
		assertEquals("id", param.getName());
		assertEquals("query", param.getIn());
		assertTrue(param.isRequired());
		assertEquals("string", param.getSchema().getType());

		Response response200 = examplePath.getGet().getResponses().get("200");
		assertNotNull(response200);
		assertEquals("successful operation", response200.getDescription());
		assertNotNull(response200.getContent().get("application/json").getSchema());
		assertEquals("#/components/schemas/ExampleSchema",
			response200.getContent().get("application/json").getSchema().getRef());

		Schema exampleSchema = pathAndComponents.getComponents().getSchemas().get("ExampleSchema");
		assertNotNull(exampleSchema);
		assertEquals("object", exampleSchema.getType());
		assertEquals("Example schema", exampleSchema.getDescription());
		assertEquals("string", exampleSchema.getProperties().get("name").getType());
		assertEquals("integer", exampleSchema.getProperties().get("value").getType());
	}

	@Test
	public void testParseToSchema() throws JsonProcessingException {
		String schemaYaml = """
			type: "object"
			description: "Detailed Example Schema"
			properties:
			  name:
			    type: "string"
			    description: "Name of the example"
			  value:
			    type: "integer"
			    description: "Value of the example"
			  nestedSchema:
			    type: "object"
			    properties:
			      id:
			        type: "string"
			        format: "uuid"
			      details:
			        type: "string"
			""";

		Schema schema = OpenAPIParser.parseToSchema(schemaYaml);

		assertNotNull(schema);
		assertEquals("object", schema.getType());
		assertEquals("Detailed Example Schema", schema.getDescription());
		assertNotNull(schema.getProperties());

		Schema nameProperty = schema.getProperties().get("name");
		assertEquals("string", nameProperty.getType());
		assertEquals("Name of the example", nameProperty.getDescription());

		Schema valueProperty = schema.getProperties().get("value");
		assertEquals("integer", valueProperty.getType());
		assertEquals("Value of the example", valueProperty.getDescription());

		Schema nestedSchema = schema.getProperties().get("nestedSchema");
		assertEquals("object", nestedSchema.getType());

		Schema nestedId = nestedSchema.getProperties().get("id");
		assertEquals("string", nestedId.getType());
		assertEquals("uuid", nestedId.getFormat());

		Schema nestedDetails = nestedSchema.getProperties().get("details");
		assertEquals("string", nestedDetails.getType());
	}

	@Test
	public void testParseToPath() {
		String pathYaml = """
			get:
			  summary: "Example GET"
			  description: "Retrieve an example item"
			  operationId: "getExampleItem"
			  tags:
			    - "example"
			  parameters:
			    - name: "id"
			      in: "path"
			      required: true
			      schema:
			        type: "string"
			  responses:
			    '200':
			      description: "Successful retrieval"
			      content:
			        application/json:
			          schema:
			            type: "object"
			            properties:
			              name:
			                type: "string"
			              value:
			                type: "integer"
			""";

		PathItem pathItem = OpenAPIParser.parseToPath(pathYaml);

		assertNotNull(pathItem);
		Operation getOperation = pathItem.getGet();
		assertNotNull(getOperation);
		assertEquals("Example GET", getOperation.getSummary());
		assertEquals("Retrieve an example item", getOperation.getDescription());
		assertEquals("getExampleItem", getOperation.getOperationId());
		assertEquals("example", getOperation.getTags().get(0));

		Parameter param = getOperation.getParameters().get(0);
		assertEquals("id", param.getName());
		assertEquals("path", param.getIn());
		assertTrue(param.isRequired());
		assertEquals("string", param.getSchema().getType());

		Response response200 = getOperation.getResponses().get("200");
		assertNotNull(response200);
		assertEquals("Successful retrieval", response200.getDescription());

		MediaType jsonContent = response200.getContent().get("application/json");
		assertNotNull(jsonContent);

		Schema responseSchema = jsonContent.getSchema();
		assertEquals("object", responseSchema.getType());
		assertEquals("string", responseSchema.getProperties().get("name").getType());
		assertEquals("integer", responseSchema.getProperties().get("value").getType());
	}
}

