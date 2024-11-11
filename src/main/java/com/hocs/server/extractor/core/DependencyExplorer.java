package com.hocs.server.extractor.core;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.hocs.server.extractor.core.data.JavaClassifiedDataContainer;
import com.hocs.server.extractor.core.util.GenericTypeResolver;
import com.hocs.server.extractor.core.util.GroupingStrategy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 선언된 변수와 메소드호출에 관하여 재귀적으로 의존성을 수집합니다.
 */
@Component
@RequiredArgsConstructor
public class DependencyExplorer {
	private final JavaClassifiedDataContainer javaClassifiedDataContainer;
	private final ExpressionResolver expressionResolver;
	private final GenericTypeResolver genericTypeResolver;


	/**
	 * @RequestBody와 같은 의존성을 포함하여 클래스의 모든 의존성을 재귀적으로 수집합니다.
	 * @param className 추적할 클래스 이름
	 * @param requiredFiles 수집된 파일 경로를 저장할 집합
	 * @param visitedClasses 이미 방문한 클래스를 추적하여 순환 참조를 방지
	 */
	public void findClassDependencies(String className, Set<String> requiredFiles, Set<String> visitedClasses) throws Exception {
		if (className == null || className.isEmpty() || visitedClasses.contains(className)) {
			return;
		}
		visitedClasses.add(className);

		// 제네릭 타입 일경우 실제 타입 인수 추출
		List<String> extractedClassNames = genericTypeResolver.extractClassNamesFromType(className);

		for (String extractedClassName : extractedClassNames) {
			String actualClassName = extractedClassName;
			// 만약 클래스 이름에 패키지 경로가 포함되어 있다면, 단순히 클래스 이름을 사용
			if (actualClassName.contains(".")) {
				actualClassName = actualClassName.substring(actualClassName.lastIndexOf('.') + 1);
			}

			String filePath = javaClassifiedDataContainer.getClassToFilePath().get(actualClassName);
			if (filePath == null) {
				continue;
			}
			requiredFiles.add(filePath);

			String content = new String(Files.readAllBytes(Paths.get(filePath)));
			CompilationUnit cu;
			try {
				cu = StaticJavaParser.parse(content);
			} catch (Exception e) {
				System.err.println("Failed to parse class: " + actualClassName);
				e.printStackTrace();
				continue;
			}

			TypeDeclaration<?> typeDecl = cu.getType(0);
			if (GroupingStrategy.isClassOrInterfacee(typeDecl)) {
				recursiveCaseInClassOrInterface(requiredFiles, visitedClasses, actualClassName,
					(ClassOrInterfaceDeclaration) typeDecl);

			} else if (typeDecl instanceof EnumDeclaration) {
				recursiveCaseInEnum(requiredFiles, visitedClasses, actualClassName,
					(EnumDeclaration) typeDecl);

			} else if (typeDecl instanceof RecordDeclaration) {
				recursiveCaseInRecord(requiredFiles, visitedClasses, actualClassName,
					(RecordDeclaration) typeDecl);
			}

			// 다른 TypeDeclaration 타입도 필요에 따라 처리 가능
		}
	}

	/**
	 * Record 타입의 의존성을 추적합니다.
	 * @param requiredFiles 수집된 파일 경로를 저장할 집합
	 * @param visitedClasses 이미 방문한 클래스를 추적하여 순환 참조를 방지
	 * @param actualClassName 추적 중인 클래스 이름
	 * @param recordDecl Record 타입
	 */
	private void recursiveCaseInRecord(Set<String> requiredFiles, Set<String> visitedClasses,
		String actualClassName, RecordDeclaration recordDecl) throws Exception {

		// Record 필드 타입 추적
		for (Parameter parameter : recordDecl.getParameters()) {
			String parameterType = parameter.getType().asString();
			findClassDependencies(parameterType, requiredFiles, visitedClasses);
		}

		// Record에 정의된 메서드가 있다면 메서드 호출 추적
		methodTrace(requiredFiles, visitedClasses, actualClassName, recordDecl.getMethods());
	}

	/**
	 * Enum 타입의 의존성을 추적합니다.
	 * @param requiredFiles 수집된 파일 경로를 저장할 집합
	 * @param visitedClasses 이미 방문한 클래스를 추적하여 순환 참조를 방지
	 * @param actualClassName 추적 중인 클래스 이름
	 * @param enumDecl Enum 타입
	 */
	private void recursiveCaseInEnum(Set<String> requiredFiles, Set<String> visitedClasses,
		String actualClassName, EnumDeclaration enumDecl) throws Exception {

		// Enum에 정의된 메서드가 있다면 메서드 호출 추적
		methodTrace(requiredFiles, visitedClasses, actualClassName, enumDecl.getMethods());
	}

	/**
	 * Class 또는 Interface 타입의 의존성을 추적합니다.
	 * @param requiredFiles 수집된 파일 경로를 저장할 집합
	 * @param visitedClasses 이미 방문한 클래스를 추적하여 순환 참조를 방지
	 * @param actualClassName 추적 중인 클래스 이름
	 * @param classOrInterfaceDeclaration Class 또는 Interface 타입
	 */
	private void recursiveCaseInClassOrInterface(Set<String> requiredFiles, Set<String> visitedClasses,
		String actualClassName, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) throws Exception {

		// 멤버 필드 타입 추적
		for (FieldDeclaration field : classOrInterfaceDeclaration.getFields()) {
			String fieldType = field.getElementType().asString();
			findClassDependencies(fieldType, requiredFiles, visitedClasses);
		}

		// 메서드 호출을 추적하여 필요한 파일 경로 수집
		methodTrace(requiredFiles, visitedClasses, actualClassName, classOrInterfaceDeclaration.getMethods());
	}




	/**
	 * 메서드 호출을 추적하여 필요한 파일 경로를 수집합니다.
	 * @param requiredFiles 수집된 파일 경로를 저장할 집합
	 * @param visitedClasses 이미 방문한 클래스를 추적하여 순환 참조를 방지
	 * @param actualClassName 추적 중인 클래스 이름
	 * @param methods 추적할 메서드 목록
	 */
	private void methodTrace(Set<String> requiredFiles, Set<String> visitedClasses,
		String actualClassName, List<MethodDeclaration> methods ) throws Exception {
		for (MethodDeclaration method : methods) {
			// 변수 선언에서 타입 추적 추가
			List<String> variableDeclarations = findVariableDeclarations(method);
			for (String variableDeclaration : variableDeclarations) {
				findClassDependencies(variableDeclaration, requiredFiles, visitedClasses);
			}

			findMethodCallDependencies(actualClassName, method.getNameAsString(), requiredFiles, new HashSet<>());
		}
	}

	/**
	 * 메서드 내의 변수 선언에서 타입을 추적합니다.
	 *
	 * @param method         타겟이 되는 메서드
	 * @return 선언된 변수 타입 목록
	 */
	public List<String> findVariableDeclarations(MethodDeclaration method) {
		List<String> varTypes = new ArrayList<>();
		method.getBody().ifPresent(body -> {
			List<VariableDeclarationExpr> vars = body.findAll(VariableDeclarationExpr.class);
			for (VariableDeclarationExpr varDecl : vars) {
				for (VariableDeclarator var : varDecl.getVariables()) {
					String varType = var.getType().asString();
					try {
						varTypes.add(varType);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		return varTypes;
	}

	/**
	 * 주어진 메서드에서 호출되는 메서드를 재귀적으로 추적합니다.
	 * @param className 추적할 클래스 이름
	 * @param methodName 추적할 메서드 이름
	 * @param requiredFiles 수집된 파일 경로를 저장할 집합
	 * @param visitedMethods 순환호출 방지
	 */
	public void findMethodCallDependencies(String className, String methodName, Set<String> requiredFiles, Set<String> visitedMethods) throws Exception {
		String methodSignature = className + "." + methodName;
		if (visitedMethods.contains(methodSignature)) {
			return;
		}
		visitedMethods.add(methodSignature);

		String filePath = javaClassifiedDataContainer.getClassToFilePath().get(className);
		if (filePath == null) {
			return;
		}

		String content = new String(Files.readAllBytes(Paths.get(filePath)));
		CompilationUnit cu;
		try {
			cu = StaticJavaParser.parse(content);
		} catch (Exception e) {
			System.err.println("Failed to parse class: " + className);
			e.printStackTrace();
			return;
		}

		// 지정된 메서드를 찾습니다.
		Optional<MethodDeclaration> methodOpt = cu.findAll(MethodDeclaration.class).stream()
			.filter(m -> m.getNameAsString().equals(methodName))
			.findFirst();

		if (methodOpt.isPresent()) {
			MethodDeclaration method = methodOpt.get();

			// 메서드 본문에서 호출되는 메서드들을 찾습니다.


			method.getBody().ifPresent(body -> {
				List<MethodCallExpr> methodCalls = body.findAll(MethodCallExpr.class);
				for (MethodCallExpr callExpr : methodCalls) {
					try {
						Optional<String> scopeClassNameOpt = expressionResolver.resolveExpressionType(callExpr.getScope().orElse(null), className);

						if (scopeClassNameOpt.isPresent()) {
							String calledMethodName = callExpr.getNameAsString();
							String scopeClassName = scopeClassNameOpt.get();

							String calledFilePath = javaClassifiedDataContainer.getClassToFilePath().get(
								scopeClassName);
							if (calledFilePath != null) {
								requiredFiles.add(calledFilePath);
								// 인터페이스인 경우 구현체를 모두 추적
								if (javaClassifiedDataContainer.getInterfaceImplementations().containsKey(scopeClassName)) {
									Set<String> implementations = javaClassifiedDataContainer.getInterfaceImplementations().getOrDefault(scopeClassName, Collections.emptySet());
									for (String implClass : implementations) {
										String implFilePath = javaClassifiedDataContainer.getClassToFilePath().get(implClass);
										if (implFilePath != null) {
											requiredFiles.add(implFilePath);
											// 구현 클래스의 메서드 호출도 추적
											findMethodCallDependencies(implClass, calledMethodName, requiredFiles, visitedMethods);
										}
									}
								} else {
									// 클래스인 경우 메서드 호출을 재귀적으로 추적
									findMethodCallDependencies(scopeClassName, calledMethodName, requiredFiles, visitedMethods);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				// 필드 접근 표현식(static) 에서 의존성 추적
				List<FieldAccessExpr> fieldAccessExprs = body.findAll(FieldAccessExpr.class);
				for (FieldAccessExpr fieldAccessExpr : fieldAccessExprs) {
					try {
						Optional<String> scopeClassName = expressionResolver.resolveExpressionType(fieldAccessExpr.getScope(), className);
						if (scopeClassName.isPresent()) {
							String fieldFilePath = javaClassifiedDataContainer.getClassToFilePath().get(scopeClassName.get());
							if (fieldFilePath != null) {
								requiredFiles.add(fieldFilePath);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

}
