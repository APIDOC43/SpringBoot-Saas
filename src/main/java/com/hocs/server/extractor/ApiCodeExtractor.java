package com.hocs.server.extractor;


import static com.hocs.server.extractor.util.HttpMethodManager.extractHttpMethod;
import static com.hocs.server.extractor.util.HttpMethodManager.isHttpMethod;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.hocs.server.extractor.util.GroupingStrategy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ApiCodeExtractor {


	private ClassifiedDataContainer classifiedDataContainer;

	public ApiCodeExtractor(ClassifiedDataContainer classifiedDataContainer) {
		this.classifiedDataContainer = classifiedDataContainer;
	}

	/**
	 * 특정 컨트롤러 클래스의 모든 HTTP 메서드를 추적하여 API 단위로 필요한 파일 경로를 수집합니다.
	 */
	public void  traceControllerApis(String entryPath,String className, List<Map<String, Object>> outputData) throws Exception {


		String filePath = classifiedDataContainer.getClassToFilePath().get(className);
		if (filePath == null) {
			return;
		}

		String content = new String(Files.readAllBytes(Paths.get(filePath)));
		CompilationUnit cu;
		try {
			cu = StaticJavaParser.parse(content);
		} catch (Exception e) {
			System.err.println("Failed to parse controller class: " + className);
			e.printStackTrace();
			return;
		}

		TypeDeclaration<?> typeDecl = cu.getType(0);
		String basePath = "";

		// 클래스 수준의 @RequestMapping 경로 추출
		if (typeDecl.isAnnotationPresent("RequestMapping")) {
			AnnotationExpr requestMapping = typeDecl.getAnnotationByName("RequestMapping").get();
			basePath = extractPathFromAnnotation(requestMapping);
		}

		// 클래스의 모든 HTTP 메서드를 순회합니다.
		for (MethodDeclaration method : typeDecl.getMethods()) {
			if (isHttpMethod(method)) {
				// 메서드 수준의 매핑 경로 추출
				String methodPath = "";
				String httpMethod = "";
				for (AnnotationExpr annotation : method.getAnnotations()) {
					String annotationName = annotation.getNameAsString();
					if (annotationName.endsWith("Mapping")) {
						httpMethod = extractHttpMethod(annotationName, annotation);
						methodPath = extractPathFromAnnotation(annotation);
						break;
					}
				}

				String fullApiPath = combinePaths(basePath, methodPath);

				// API별로 필요한 파일 경로를 추적합니다.
				Set<String> requiredFiles = new HashSet<>();
				Set<String> visitedClasses = new HashSet<>(); // 재귀적 추적을 위한 방문한 클래스 집합
				requiredFiles.add(filePath); // 컨트롤러 파일 자체 추가

				// 컨트롤러 클래스의 의존성도 추적
				collectClassDependencies(className, requiredFiles, visitedClasses);

				// 메서드 호출을 추적하여 필요한 파일 경로 수집
				traceMethodCalls(className, method.getNameAsString(), requiredFiles, new HashSet<>());

				// @RequestBody 파라미터를 포함한 클래스의 의존성도 추적
				for (Parameter param : method.getParameters()) {
					for (AnnotationExpr paramAnnotation : param.getAnnotations()) {
						if (paramAnnotation.getNameAsString().equals("RequestBody")) {
							String requestType = param.getType().asString();
							collectClassDependencies(requestType, requiredFiles, visitedClasses);
						}
					}
				}

				// 메서드의 반환 타입에서 의존성 수집
				if (method.getType() != null) {
					String returnType = method.getType().asString();
					collectClassDependencies(returnType, requiredFiles, visitedClasses);
				}

				// API 엔트리를 생성하여 출력 데이터에 추가합니다.
				Map<String, Object> apiEntry = new LinkedHashMap<>();
				apiEntry.put("API", fullApiPath);
				apiEntry.put("method", httpMethod);
				// 중복을 방지하고 정렬된 리스트로 변환
				List<String> sortedPaths = new ArrayList<>(requiredFiles);
				Collections.sort(sortedPaths);
				apiEntry.put("paths", sortedPaths);
				apiEntry.put("absolutePath", entryPath);
				outputData.add(apiEntry);
			}
		}


	}

	/**
	 * @RequestBody와 같은 의존성을 포함하여 클래스의 모든 의존성을 재귀적으로 수집합니다.
	 * @param className 추적할 클래스 이름
	 * @param requiredFiles 수집된 파일 경로를 저장할 집합
	 * @param visitedClasses 이미 방문한 클래스를 추적하여 순환 참조를 방지
	 */
	public void collectClassDependencies(String className, Set<String> requiredFiles, Set<String> visitedClasses) throws Exception {
		if (className == null || className.isEmpty() || visitedClasses.contains(className)) {
			return;
		}
		visitedClasses.add(className);

		// 제네릭 타입에서 실제 타입 인수 추출
		List<String> extractedClassNames = extractClassNamesFromType(className);

		for (String extractedClassName : extractedClassNames) {
			String actualClassName = extractedClassName;
			// 만약 클래스 이름에 패키지 경로가 포함되어 있다면, 단순히 클래스 이름을 사용
			if (actualClassName.contains(".")) {
				actualClassName = actualClassName.substring(actualClassName.lastIndexOf('.') + 1);
			}

			String filePath = classifiedDataContainer.getClassToFilePath().get(actualClassName);
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
				ClassOrInterfaceDeclaration cid = (ClassOrInterfaceDeclaration) typeDecl;

				// 필드 타입 추적
				for (FieldDeclaration field : cid.getFields()) {
					String fieldType = field.getElementType().asString();
					collectClassDependencies(fieldType, requiredFiles, visitedClasses);
				}

				// 메서드 호출을 추적하여 필요한 파일 경로 수집
				List<MethodDeclaration> methods = cid.getMethods();
				for (MethodDeclaration method : methods) {
					// 변수 선언에서 타입 추적 추가
					processVariableDeclarationsInMethod(method, requiredFiles, visitedClasses);

					traceMethodCalls(actualClassName, method.getNameAsString(), requiredFiles, new HashSet<>());
				}
			} else if (typeDecl instanceof EnumDeclaration) {
				EnumDeclaration enumDecl = (EnumDeclaration) typeDecl;

				// Enum에 정의된 메서드가 있다면 메서드 호출 추적
				for (MethodDeclaration method : enumDecl.getMethods()) {
					// 변수 선언에서 타입 추적 추가
					processVariableDeclarationsInMethod(method, requiredFiles, visitedClasses);

					traceMethodCalls(actualClassName, method.getNameAsString(), requiredFiles, new HashSet<>());
				}
			} else if (typeDecl instanceof RecordDeclaration) {
				RecordDeclaration recordDecl = (RecordDeclaration) typeDecl;

				// Record 필드 타입 추적
				for (Parameter parameter : recordDecl.getParameters()) {
					String parameterType = parameter.getType().asString();
					collectClassDependencies(parameterType, requiredFiles, visitedClasses);
				}

				// Record에 정의된 메서드가 있다면 메서드 호출 추적
				for (MethodDeclaration method : recordDecl.getMethods()) {
					// 변수 선언에서 타입 추적 추가
					processVariableDeclarationsInMethod(method, requiredFiles, visitedClasses);

					traceMethodCalls(actualClassName, method.getNameAsString(), requiredFiles, new HashSet<>());
				}
			}
			// 다른 TypeDeclaration 타입도 필요에 따라 처리 가능
		}
	}

	// 메서드 내의 변수 선언을 처리하여 의존성 수집
	private void processVariableDeclarationsInMethod(MethodDeclaration method, Set<String> requiredFiles, Set<String> visitedClasses) {
		method.getBody().ifPresent(body -> {
			List<VariableDeclarationExpr> vars = body.findAll(VariableDeclarationExpr.class);
			for (VariableDeclarationExpr varDecl : vars) {
				for (VariableDeclarator var : varDecl.getVariables()) {
					String varType = var.getType().asString();
					try {
						collectClassDependencies(varType, requiredFiles, visitedClasses);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * 주어진 타입 문자열에서 클래스 이름을 추출합니다.
	 * 예: "List<ProductResponse>" -> ["List", "ProductResponse"]
	 */
	private List<String> extractClassNamesFromType(String typeStr) {
		List<String> classNames = new ArrayList<>();
		if (typeStr.contains("<") && typeStr.contains(">")) {
			// 제네릭 타입 처리
			int start = typeStr.indexOf('<');
			int end = typeStr.lastIndexOf('>');
			String mainType = typeStr.substring(0, start).trim();
			String genericTypes = typeStr.substring(start + 1, end).trim();
			classNames.add(mainType);
			// 제네릭 타입이 여러 개인 경우 쉼표로 분리
			String[] generics = genericTypes.split(",");
			for (String generic : generics) {
				classNames.add(generic.trim());
			}
		} else {
			classNames.add(typeStr);
		}
		return classNames;
	}





	/**
	 * 어노테이션에서 경로(Path)를 추출합니다.
	 */
	private String extractPathFromAnnotation(AnnotationExpr annotation) {
		if (annotation.isSingleMemberAnnotationExpr()) {
			return annotation.asSingleMemberAnnotationExpr().getMemberValue().toString().replaceAll("\"", "");
		} else if (annotation.isNormalAnnotationExpr()) {
			// 'value' 또는 'path' 속성에서 경로를 추출
			return annotation.asNormalAnnotationExpr().getPairs().stream()
				.filter(pair -> pair.getNameAsString().equals("value") || pair.getNameAsString().equals("path"))
				.map(pair -> pair.getValue().toString().replaceAll("\"", ""))
				.findFirst()
				.orElse("");
		}
		return "";
	}

	/**
	 * 두 개의 경로를 결합하여 전체 API 경로를 생성합니다.
	 * basePath : "/store-owner" + "/sign-up"
	 * == Controller Level path + Method Level path
	 */
	private String combinePaths(String basePath, String methodPath) {
		if (basePath == null) basePath = "";
		if (methodPath == null) methodPath = "";

		if (!basePath.endsWith("/") && !methodPath.startsWith("/")) {
			return basePath + "/" + methodPath;
		} else if (basePath.endsWith("/") && methodPath.startsWith("/")) {
			return basePath + methodPath.substring(1);
		} else {
			return basePath + methodPath;
		}
	}

	/**
	 * 특정 메서드 내에서 호출되는 메서드를 추적하여 필요한 파일 경로를 수집합니다.
//	 */
//	private void traceMethodCalls(String className, String methodName, Set<String> requiredFiles, Set<String> visitedMethods) throws Exception {
//		String methodSignature = className + "." + methodName;
//		if (visitedMethods.contains(methodSignature)) {
//			return;
//		}
//		visitedMethods.add(methodSignature);
//
//		String filePath = classifiedDataContainer.getClassToFilePath().get(className);
//		if (filePath == null) {
//			return;
//		}
//
//		String content = new String(Files.readAllBytes(Paths.get(filePath)));
//		CompilationUnit cu;
//		try {
//			cu = StaticJavaParser.parse(content);
//		} catch (Exception e) {
//			System.err.println("Failed to parse class: " + className);
//			e.printStackTrace();
//			return;
//		}
//
//		// 지정된 메서드를 찾습니다.
//		Optional<MethodDeclaration> methodOpt = cu.findAll(MethodDeclaration.class).stream()
//			.filter(m -> m.getNameAsString().equals(methodName))
//			.findFirst();
//
//		if (methodOpt.isPresent()) {
//			MethodDeclaration method = methodOpt.get();
//
//			// 메서드 본문에서 호출되는 메서드들을 찾습니다.
//			method.getBody().ifPresent(body -> {
//				List<MethodCallExpr> methodCalls = body.findAll(MethodCallExpr.class);
//				for (MethodCallExpr callExpr : methodCalls) {
//					try {
//						String scopeClassName = resolveScopeClassName(callExpr, className);
//						if (scopeClassName != null) {
//							String calledMethodName = callExpr.getNameAsString();
//							String calledFilePath = classifiedDataContainer.getClassToFilePath().get(scopeClassName);
//							if (calledFilePath != null) {
//								requiredFiles.add(calledFilePath);
//								// 인터페이스인 경우 구현체를 모두 추적
//								if (classifiedDataContainer.getInterfaceImplementations().containsKey(scopeClassName)) {
//									Set<String> implementations = classifiedDataContainer.getInterfaceImplementations().getOrDefault(scopeClassName, Collections.emptySet());
//									for (String implClass : implementations) {
//										String implFilePath = classifiedDataContainer.getClassToFilePath().get(implClass);
//										if (implFilePath != null) {
//											requiredFiles.add(implFilePath);
//											// 구현 클래스의 메서드 호출도 추적
//											traceMethodCalls(implClass, calledMethodName, requiredFiles, visitedMethods);
//										}
//									}
//								} else {
//									// 클래스인 경우 메서드 호출을 재귀적으로 추적
//									traceMethodCalls(scopeClassName, calledMethodName, requiredFiles, visitedMethods);
//								}
//							}
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			});
//		}
//	}

	private void traceMethodCalls(String className, String methodName, Set<String> requiredFiles, Set<String> visitedMethods) throws Exception {
		String methodSignature = className + "." + methodName;
		if (visitedMethods.contains(methodSignature)) {
			return;
		}
		visitedMethods.add(methodSignature);

		String filePath = classifiedDataContainer.getClassToFilePath().get(className);
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
						String scopeClassName = resolveScopeClassName(callExpr.getScope().orElse(null), className);
						if (scopeClassName != null) {
							String calledMethodName = callExpr.getNameAsString();
							String calledFilePath = classifiedDataContainer.getClassToFilePath().get(scopeClassName);
							if (calledFilePath != null) {
								requiredFiles.add(calledFilePath);
								// 인터페이스인 경우 구현체를 모두 추적
								if (classifiedDataContainer.getInterfaceImplementations().containsKey(scopeClassName)) {
									Set<String> implementations = classifiedDataContainer.getInterfaceImplementations().getOrDefault(scopeClassName, Collections.emptySet());
									for (String implClass : implementations) {
										String implFilePath = classifiedDataContainer.getClassToFilePath().get(implClass);
										if (implFilePath != null) {
											requiredFiles.add(implFilePath);
											// 구현 클래스의 메서드 호출도 추적
											traceMethodCalls(implClass, calledMethodName, requiredFiles, visitedMethods);
										}
									}
								} else {
									// 클래스인 경우 메서드 호출을 재귀적으로 추적
									traceMethodCalls(scopeClassName, calledMethodName, requiredFiles, visitedMethods);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				// 필드 접근 표현식에서 의존성 추적
				List<FieldAccessExpr> fieldAccessExprs = body.findAll(FieldAccessExpr.class);
				for (FieldAccessExpr fieldAccessExpr : fieldAccessExprs) {
					try {
						String scopeClassName = resolveScopeClassName(fieldAccessExpr.getScope(), className);
						if (scopeClassName != null) {
							String fieldFilePath = classifiedDataContainer.getClassToFilePath().get(scopeClassName);
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



	/**
	 * 메서드 호출의 스코프를 확인하여 해당 클래스 이름을 반환합니다.
	 */
//	private String resolveScopeClassName(MethodCallExpr callExpr, String currentClassName) {
//		// 메서드 호출의 스코프를 확인합니다.
//		Optional<com.github.javaparser.ast.expr.Expression> scopeOpt = callExpr.getScope();
//		if (scopeOpt.isPresent()) {
//			com.github.javaparser.ast.expr.Expression scope = scopeOpt.get();
//			if (scope.isNameExpr()) {
//				String name = scope.asNameExpr().getNameAsString();
//				// 스코프가 클래스 이름인지 확인
//				if (classifiedDataContainer.getInterfaceImplementations().containsKey(name)) {
//					return name; // 클래스 이름인 경우
//				} else {
//					// 변수 이름인 경우 변수의 타입을 추론
//					String varType = resolveVariableType(callExpr, name, currentClassName);
//					return varType;
//				}
//			} else if (scope.isFieldAccessExpr()) {
//				String name = scope.asFieldAccessExpr().getNameAsString();
//				// 필드 접근인 경우 필드의 타입을 추론
//				String varType = resolveVariableType(callExpr, name, currentClassName);
//				return varType;
//			} else if (scope.isThisExpr()) {
//				return currentClassName;
//			} else if (scope.isMethodCallExpr()) {
//				// 스코프가 메서드 호출인 경우 재귀적으로 처리
//				return resolveScopeClassName(scope.asMethodCallExpr(), currentClassName);
//			} else if (scope.isSuperExpr()) {
//				return currentClassName;
//			}
//		} else {
//			// 스코프가 없는 경우 현재 클래스에서 메서드를 찾습니다.
//			return currentClassName;
//		}
//		return null;
//	}

	private String resolveScopeClassName(Expression expr, String currentClassName) {
		if (expr == null) {
			// 스코프가 없는 경우 현재 클래스에서 메서드를 찾습니다.
			return currentClassName;
		} else if (expr.isNameExpr()) {
			String name = expr.asNameExpr().getNameAsString();
			// 스코프가 클래스 이름인지 확인
			if (classifiedDataContainer.getClassToFilePath().containsKey(name)) {
				return name; // 클래스 이름인 경우
			} else if (classifiedDataContainer.getInterfaceImplementations().containsKey(name)) {
				return name; // 인터페이스 이름인 경우
			} else {
				// 변수 이름인 경우 변수의 타입을 추론
				String varType = resolveVariableType(expr, name, currentClassName);
				return varType;
			}
		} else if (expr.isFieldAccessExpr()) {
			// 재귀적으로 스코프 클래스 이름을 해결
			FieldAccessExpr fieldAccessExpr = expr.asFieldAccessExpr();
			return resolveScopeClassName(fieldAccessExpr.getScope(), currentClassName);
		} else if (expr.isThisExpr()) {
			return currentClassName;
		} else if (expr.isMethodCallExpr()) {
			// 스코프가 메서드 호출인 경우 재귀적으로 처리
			MethodCallExpr methodCallExpr = expr.asMethodCallExpr();
			return resolveScopeClassName(methodCallExpr.getScope().orElse(null), currentClassName);
		} else if (expr.isSuperExpr()) {
			return currentClassName;
		} else if (expr.isObjectCreationExpr()) {
			ObjectCreationExpr objectCreationExpr = expr.asObjectCreationExpr();
			return objectCreationExpr.getType().asString();
		}
		return null;
	}
	/**
	 * 변수의 타입을 추론하여 반환합니다.
	 */
//	private String resolveVariableType(MethodCallExpr callExpr, String varName, String currentClassName) {
//		// 변수의 선언 위치를 찾아 타입을 추론합니다.
//		Optional<com.github.javaparser.ast.Node> parentOpt = callExpr.getParentNode();
//		com.github.javaparser.ast.Node node = callExpr;
//		while (parentOpt.isPresent()) {
//			node = parentOpt.get();
//			if (node instanceof MethodDeclaration) {
//				MethodDeclaration method = (MethodDeclaration) node;
//				// 메서드의 매개변수에서 변수 타입을 찾습니다.
//				for (Parameter param : method.getParameters()) {
//					if (param.getNameAsString().equals(varName)) {
//						return param.getType().asString();
//					}
//				}
//				// 메서드 내의 변수 선언에서 타입을 찾습니다.
//				List<VariableDeclarationExpr> vars = method.findAll(VariableDeclarationExpr.class);
//				for (VariableDeclarationExpr varDecl : vars) {
//					for (VariableDeclarator var : varDecl.getVariables()) {
//						if (var.getNameAsString().equals(varName)) {
//							return var.getType().asString();
//						}
//					}
//				}
//			} else if (node instanceof ClassOrInterfaceDeclaration) {
//				ClassOrInterfaceDeclaration clazz = (ClassOrInterfaceDeclaration) node;
//				for (FieldDeclaration field : clazz.getFields()) {
//					for (VariableDeclarator var : field.getVariables()) {
//						if (var.getNameAsString().equals(varName)) {
//							return var.getType().asString();
//						}
//					}
//				}
//			}
//			parentOpt = node.getParentNode();
//		}
//		return null;
//	}
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
		String filePath = classifiedDataContainer.getClassToFilePath().get(currentClassName);
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

