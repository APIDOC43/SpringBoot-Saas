package com.hocs.server.extractor.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.hocs.server.code_parser.core.service.ExpressionResolver;
import com.hocs.server.code_parser.core.dataobject.JavaClassifiedDataContainer;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ExpressionResolver 클래스의 테스트 코드를 작성합니다.
 */
@ExtendWith(MockitoExtension.class)
public class ExpressionResolverTest {

	@InjectMocks
	private ExpressionResolver expressionResolver;

	@Test
	public void testResolveExpressionType_NullExpression_ReturnsCurrentClassName() {
		String currentClassName = "TestClass";
		Optional<String> result = expressionResolver.resolveExpressionType(null, currentClassName, new JavaClassifiedDataContainer());
		assertTrue(result.isPresent());
		assertEquals(currentClassName, result.get());
	}

	@Test
	public void testResolveExpressionType_NameExpr_ClassName() {
		String className = "TestClass";
		NameExpr nameExpr = new NameExpr(className);
		JavaClassifiedDataContainer dataContainer = new JavaClassifiedDataContainer();
		dataContainer.getClassToFilePath().put(className, "path/to/TestClass.java");

		Optional<String> result = expressionResolver.resolveExpressionType(nameExpr, "CurrentClass",
			dataContainer);

		assertTrue(result.isPresent());
		assertEquals(className, result.get());
	}

	@Test
	public void testResolveExpressionType_NameExpr_InterfaceName() {
		String interfaceName = "TestInterface";
		NameExpr nameExpr = new NameExpr(interfaceName);
		JavaClassifiedDataContainer dataContainer = new JavaClassifiedDataContainer();
		dataContainer.getInterfaceImplementations().put(interfaceName,Set.of("Impl1", "Impl2"));

		Optional<String> result = expressionResolver.resolveExpressionType(nameExpr, "CurrentClass", dataContainer);

		assertTrue(result.isPresent());
		assertEquals(interfaceName, result.get());
	}

	@Test
	public void testResolveExpressionType_NameExpr_VariableNameInMethodParameter() {
		String code = "void testMethod(String paramName) { System.out.println(paramName); }";
		MethodDeclaration methodDeclaration = StaticJavaParser.parseMethodDeclaration(code);

		NameExpr nameExpr = methodDeclaration
			.findFirst(NameExpr.class, ne -> ne.getNameAsString().equals("paramName"))
			.get();

		nameExpr.setParentNode(methodDeclaration.getBody().orElse(null));

		Optional<String> result = expressionResolver.resolveExpressionType(nameExpr, "CurrentClass",new JavaClassifiedDataContainer());

		assertTrue(result.isPresent());
		assertEquals("String", result.get());
	}

	@Test
	public void testResolveExpressionType_FieldAccessExpr() {
		FieldAccessExpr fieldAccessExpr = new FieldAccessExpr(new ThisExpr(), "fieldName");

		String currentClassName = "CurrentClass";
		Optional<String> result = expressionResolver.resolveExpressionType(fieldAccessExpr, currentClassName, new JavaClassifiedDataContainer());

		assertTrue(result.isPresent());
		assertEquals(currentClassName, result.get());
	}

	@Test
	public void testResolveExpressionType_ThisExpr() {
		ThisExpr thisExpr = new ThisExpr();
		String currentClassName = "CurrentClass";

		Optional<String> result = expressionResolver.resolveExpressionType(thisExpr, currentClassName, new JavaClassifiedDataContainer());

		assertTrue(result.isPresent());
		assertEquals(currentClassName, result.get());
	}

	@Test
	public void testResolveExpressionType_SuperExpr() {
		SuperExpr superExpr = new SuperExpr();
		String currentClassName = "CurrentClass";

		Optional<String> result = expressionResolver.resolveExpressionType(superExpr, currentClassName, new JavaClassifiedDataContainer());

		assertTrue(result.isPresent());
		assertEquals(currentClassName, result.get());
	}

	@Test
	public void testResolveExpressionType_ObjectCreationExpr() {
		ObjectCreationExpr objectCreationExpr = new ObjectCreationExpr();
		objectCreationExpr.setType(StaticJavaParser.parseClassOrInterfaceType("ArrayList<String>"));

		Optional<String> result = expressionResolver.resolveExpressionType(objectCreationExpr, "CurrentClass", new JavaClassifiedDataContainer());

		assertTrue(result.isPresent());
		assertEquals("ArrayList<String>", result.get());
	}

	@Test
	public void testResolveExpressionType_MethodCallExpr_WithScope() {
		String code =
			"class CurrentClass { void testMethod() { HelperClass helper = new HelperClass(); helper.performAction(); } }";
		CompilationUnit cu = StaticJavaParser.parse(code);
		MethodDeclaration methodDeclaration = cu
			.findFirst(MethodDeclaration.class, md -> md.getNameAsString().equals("testMethod"))
			.get();

		MethodCallExpr methodCallExpr = methodDeclaration
			.findFirst(MethodCallExpr.class, mce -> mce.getNameAsString().equals("performAction"))
			.get();

		Optional<String> result = expressionResolver.resolveExpressionType(methodCallExpr, "CurrentClass", new JavaClassifiedDataContainer());

		assertTrue(result.isPresent());
		assertEquals("HelperClass", result.get());
	}

	@Test
	public void testResolveExpressionType_UnrecognizedExpression() {
		BooleanLiteralExpr booleanLiteralExpr = new BooleanLiteralExpr(true);

		Optional<String> result = expressionResolver.resolveExpressionType(booleanLiteralExpr, "CurrentClass", new JavaClassifiedDataContainer());

		assertFalse(result.isPresent());
	}
}
