package com.hocs.server.extractor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.hocs.server.extractor.util.GroupingStrategy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CodeCategorizer {

	private final ClassifiedDataContainer classifiedDataContainer;

	public ClassifiedDataContainer parse(List<File> javaFiles) throws IOException {
		int i = 0;
		for (File file : javaFiles) {
			String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
			try {
				CompilationUnit cu = StaticJavaParser.parse(content);

				// 클래스, 인터페이스, 열거형, 레코드 등을 모두 처리합니다.
				addToListByType(file.getAbsolutePath(), cu);
			} catch (Exception e) {
				System.err.println("Failed to parse file: " + file.getAbsolutePath());
				e.printStackTrace();
			}
		}



		return classifiedDataContainer;
	}



	private void addToListByType(String absolutePath, CompilationUnit cu) {
		cu.findAll(TypeDeclaration.class).forEach(typeDecl -> {
			String typeName = typeDecl.getNameAsString();

			classifiedDataContainer.putClassToFilePath(typeName,absolutePath);

			// 컨트롤러 클래스 식별
			if (GroupingStrategy.isController(typeDecl)) {
				classifiedDataContainer.addControllerClasses(typeName);
			}

			// AOP Aspect 클래스 식별
			if (GroupingStrategy.isAspect(typeDecl)) {
				classifiedDataContainer.addGlobalDependencies(absolutePath);
			}

			// 특수한 어노테이션이 붙은 클래스 식별
			NodeList<AnnotationExpr> annotations = typeDecl.getAnnotations();
			for (AnnotationExpr annotation : annotations) {
				String annotationName = annotation.getNameAsString();
//				Set<String> requiredFiles = new HashSet<>();
				if (classifiedDataContainer.getSpecialAnnotations().containsKey(annotationName)) {
					String category = classifiedDataContainer.getSpecialAnnotations().get(annotationName);
					classifiedDataContainer.getGlobalDependencies().get(category).add(absolutePath);
//					if (typeName.equals("GlobalExceptionHandler")) {
//						System.out.println();
//					}
//
//					try {
//						new ApiCodeExtractor(classifiedDataContainer).collectClassDependencies(typeName, requiredFiles, new HashSet<>());
//					} catch (Exception e) {
//						throw new RuntimeException(e);
//					}
//					for (String requiredFile : requiredFiles) {
//						classifiedDataContainer.addGlobalDependencies(requiredFile);
//					}
				}
			}

			// Filter 및 Interceptor 클래스 식별
			if (GroupingStrategy.isClassOrInterfacee(typeDecl)) {
				ClassOrInterfaceDeclaration cid = (ClassOrInterfaceDeclaration) typeDecl;

				// 인터페이스 구현체를 수집합니다.
				if (!cid.isInterface()) {
					cid.getImplementedTypes().forEach(implType -> {
						String interfaceName = implType.getNameAsString();
						classifiedDataContainer.getInterfaceImplementations().computeIfAbsent(interfaceName, k -> new HashSet<>()).add(typeName);

						// Filter 인터페이스 구현 여부 확인
						if (interfaceName.equals("Filter")) {
							classifiedDataContainer.getGlobalDependencies().get("Filter").add(absolutePath);
						}

						// HandlerInterceptor 인터페이스 구현 여부 확인
						if (interfaceName.equals("HandlerInterceptor") || interfaceName.equals("AsyncHandlerInterceptor") || interfaceName.equals("HandlerInterceptorAdapter")) {
							classifiedDataContainer.getGlobalDependencies().get("Interceptor").add(absolutePath);
						}
					});
				}

				// 상속을 통한 Filter 및 Interceptor 식별
				cid.getExtendedTypes().forEach(extendedType -> {
					String parentClassName = extendedType.getNameAsString();

					// Filter 상속 여부 확인
					if (parentClassName.equals("GenericFilterBean") || parentClassName.equals("OncePerRequestFilter")) {
						classifiedDataContainer.getGlobalDependencies().get("Filter").add(absolutePath);
					}

					// HandlerInterceptor 상속 여부 확인
					if (parentClassName.equals("HandlerInterceptorAdapter")) {
						classifiedDataContainer.getGlobalDependencies().get("Interceptor").add(absolutePath);
					}
				});
			}
		});
	}




}
