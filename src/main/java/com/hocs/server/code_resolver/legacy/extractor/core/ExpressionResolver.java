package com.hocs.server.code_resolver.legacy.extractor.core;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.hocs.server.code_resolver.legacy.extractor.core.data.JavaClassifiedDataContainer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Expression의 타입을 추론하는 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class ExpressionResolver {
	private final JavaClassifiedDataContainer javaClassifiedDataContainer;


	/**
	 * 이 코드는 주어진 코드 조각(표현식)이 어떤 타입인지를 찾아내는 역할을 합니다.
	 * 표현식(Expression): 코드의 한 부분으로, 변수, 메서드 호출, 객체 생성 등을 의미합니다.
	 * 스코프(Scope): 그 표현식이나 변수가 어디에서 선언되었고 어디까지 유효한지를 나타냅니다.
	 */

	/**
	 * 표현식(Expression)이란?
	 * 정의: 코드에서 하나의 값이나 동작을 나타내는 부분입니다.
	 * 예시:
	 * 변수 이름 (number, localVar)
	 * 메서드 호출 (helper.performAction())
	 * 객체 생성 (new ArrayList<>())
	 * 리터럴 값 (10, "Hello")
	 */

	/**
	 * 스코프(Scope)란?
	 * 정의: 변수나 표현식이 유효하게 사용될 수 있는 범위입니다.
	 * 종류:
	 * 클래스 스코프: 클래스 전체에서 유효한 변수나 메서드 (private int number; 등)
	 * 메서드 스코프: 메서드 내부에서만 유효한 변수 (int localVar = 10; 등)
	 * 블록 스코프: {}로 감싸진 블록 내부에서만 유효한 변수
	 */

	public Optional<String> resolveExpressionType(Expression expr, String currentClassName) {
		if (expr == null) {
			// 스코프가 없는 경우 현재 클래스에서 메서드를 찾습니다.
			return Optional.of(currentClassName);
		} else if (expr.isNameExpr()) {
			String name = expr.asNameExpr().getNameAsString();
			// 스코프가 클래스 이름인지 확인
			if (javaClassifiedDataContainer.getClassToFilePath().containsKey(name)) {
				return Optional.of(name); // 클래스 이름인 경우
			} else if (javaClassifiedDataContainer.getInterfaceImplementations().containsKey(name)) {
				return Optional.of(name); // 인터페이스 이름인 경우
			} else {
				// 변수 이름인 경우 변수의 타입을 추론
				return resolveVariableType(expr, name, currentClassName);
			}
		} else if (expr.isFieldAccessExpr()) {
			// 재귀적으로 스코프 클래스 이름을 해결
			FieldAccessExpr fieldAccessExpr = expr.asFieldAccessExpr();
			return resolveExpressionType(fieldAccessExpr.getScope(), currentClassName);
		} else if (expr.isThisExpr()) {
			return Optional.of(currentClassName);
		} else if (expr.isMethodCallExpr()) {
			// 스코프가 메서드 호출인 경우 재귀적으로 처리
			MethodCallExpr methodCallExpr = expr.asMethodCallExpr();
			return resolveExpressionType(methodCallExpr.getScope().orElse(null), currentClassName);
		} else if (expr.isSuperExpr()) {
			return Optional.of(currentClassName);
		} else if (expr.isObjectCreationExpr()) {
			ObjectCreationExpr objectCreationExpr = expr.asObjectCreationExpr();
			return Optional.of(objectCreationExpr.getType().asString());
		}
		return Optional.empty();
	}

	private Optional<String> resolveVariableType(Node node, String varName, String currentClassName) {
		// 현재 노드에서 부모를 탐색하여 변수 선언을 찾습니다.
		Optional<Node> parentNode = node.getParentNode();
		while (parentNode.isPresent()) {
			Node parent = parentNode.get();
			if (parent instanceof MethodDeclaration) {
				MethodDeclaration methodDeclaration = (MethodDeclaration) parent;
				// 메서드 내의 변수 선언 검색
				List<VariableDeclarator> variableDeclarators = methodDeclaration.findAll(VariableDeclarator.class);
				for (VariableDeclarator vd : variableDeclarators) {
					if (vd.getNameAsString().equals(varName)) {
						return Optional.of(vd.getType().asString());
					}
				}
				// 메서드의 파라미터 검색
				for (Parameter parameter : methodDeclaration.getParameters()) {
					if (parameter.getNameAsString().equals(varName)) {
						return Optional.of(parameter.getType().asString());
					}
				}
				break;
			}
			parentNode = parent.getParentNode();
		}
		// 클래스 필드에서 변수 선언 검색
		String filePath = javaClassifiedDataContainer.getClassToFilePath().get(currentClassName);
		if (filePath != null) {
			try {
				String content = new String(Files.readAllBytes(Paths.get(filePath)));
				CompilationUnit cu = StaticJavaParser.parse(content);
				Optional<ClassOrInterfaceDeclaration> classOpt = cu.getClassByName(currentClassName);
				if (classOpt.isPresent()) {
					ClassOrInterfaceDeclaration classDecl = classOpt.get();
					List<FieldDeclaration> fieldDeclarations = classDecl.getFields();
					for (FieldDeclaration fd : fieldDeclarations) {
						for (VariableDeclarator vd : fd.getVariables()) {
							if (vd.getNameAsString().equals(varName)) {
								return Optional.of(vd.getType().asString());
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 타입을 찾지 못한 경우 null 반환
		return Optional.empty();
	}
}
