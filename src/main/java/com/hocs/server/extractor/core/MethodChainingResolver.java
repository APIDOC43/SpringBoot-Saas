package com.hocs.server.extractor.core;

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
import com.hocs.server.extractor.core.data.JavaClassifiedDataContainer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 메서드 체이닝을 해결하는 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class MethodChainingResolver {
	private final JavaClassifiedDataContainer javaClassifiedDataContainer;


	/**
	 * 메서드 체이닝의 루트 클래스 이름을 추출합니다.
	 * 주어진 표현식이 여러 메서드가 연결된 체이닝 형태일 경우,
	 * 첫 번째 호출되는 클래스나 객체의 이름을 반환합니다.
	 *
	 * @param expr            메서드 호출 표현식 (Expression 객체)
	 * @param currentClassName 현재 클래스 이름
	 * @return 루트 클래스 이름 (체이닝의 시작점 클래스)
	 * @ex : UUID.randomUUID().toString().replaceAll("-", "") -> UUID
	 */
	public String resolveMethodChainingClassName(Expression expr, String currentClassName) {
		if (expr == null) {
			// 스코프가 없는 경우 현재 클래스에서 메서드를 찾습니다.
			return currentClassName;
		} else if (expr.isNameExpr()) {
			String name = expr.asNameExpr().getNameAsString();
			// 스코프가 클래스 이름인지 확인
			if (javaClassifiedDataContainer.getClassToFilePath().containsKey(name)) {
				return name; // 클래스 이름인 경우
			} else if (javaClassifiedDataContainer.getInterfaceImplementations().containsKey(name)) {
				return name; // 인터페이스 이름인 경우
			} else {
				// 변수 이름인 경우 변수의 타입을 추론
				String varType = resolveVariableType(expr, name, currentClassName);
				return varType;
			}
		} else if (expr.isFieldAccessExpr()) {
			// 재귀적으로 스코프 클래스 이름을 해결
			FieldAccessExpr fieldAccessExpr = expr.asFieldAccessExpr();
			return resolveMethodChainingClassName(fieldAccessExpr.getScope(), currentClassName);
		} else if (expr.isThisExpr()) {
			return currentClassName;
		} else if (expr.isMethodCallExpr()) {
			// 스코프가 메서드 호출인 경우 재귀적으로 처리
			MethodCallExpr methodCallExpr = expr.asMethodCallExpr();
			return resolveMethodChainingClassName(methodCallExpr.getScope().orElse(null), currentClassName);
		} else if (expr.isSuperExpr()) {
			return currentClassName;
		} else if (expr.isObjectCreationExpr()) {
			ObjectCreationExpr objectCreationExpr = expr.asObjectCreationExpr();
			return objectCreationExpr.getType().asString();
		}
		return null;
	}

	private String resolveVariableType(Node node, String varName, String currentClassName) {
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
						return vd.getType().asString();
					}
				}
				// 메서드의 파라미터 검색
				for (Parameter parameter : methodDeclaration.getParameters()) {
					if (parameter.getNameAsString().equals(varName)) {
						return parameter.getType().asString();
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
								return vd.getType().asString();
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 타입을 찾지 못한 경우 null 반환
		return null;
	}
}
