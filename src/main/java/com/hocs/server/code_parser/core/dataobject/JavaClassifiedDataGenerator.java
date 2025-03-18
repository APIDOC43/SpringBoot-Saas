package com.hocs.server.code_parser.core.dataobject;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.hocs.server.code_parser.core.util.GroupingStrategy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * JavaClassifiedDataContainer을 초기화하는 책임을 가지고 있는 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class JavaClassifiedDataGenerator {

	public JavaClassifiedDataContainer init(List<File> files) throws IOException {
		JavaClassifiedDataContainer container = new JavaClassifiedDataContainer();
		for (File file : files) {
			String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
			try {
				CompilationUnit cu = StaticJavaParser.parse(content);
				// 클래스, 인터페이스, 열거형, 레코드 등을 모두 처리합니다.
				SortAndSave(file.getAbsolutePath(), cu, container);
			} catch (Exception e) {
				System.err.println("Failed to parse file: " + file.getAbsolutePath());
				e.printStackTrace();
			}
		}
		container.setStatusToInit();
		return container;
	}



	private void SortAndSave(String absolutePath, CompilationUnit cu,
		JavaClassifiedDataContainer javaClassifiedDataContainer) {
		cu.findAll(TypeDeclaration.class).forEach(typeDecl -> {
			String typeName = typeDecl.getNameAsString();

			javaClassifiedDataContainer.putClassToFilePath(typeName,absolutePath);

			// 컨트롤러 클래스 식별
			if (GroupingStrategy.isController(typeDecl)) {
				javaClassifiedDataContainer.addControllerClasses(typeName);
			}

			// AOP Aspect 클래스 식별
			if (GroupingStrategy.isAspect(typeDecl)) {
				javaClassifiedDataContainer.addGlobalDependencies(absolutePath);
			}

			// 특수한 어노테이션이 붙은 클래스 식별
			NodeList<AnnotationExpr> annotations = typeDecl.getAnnotations();
			for (AnnotationExpr annotation : annotations) {
				String annotationName = annotation.getNameAsString();
				if (javaClassifiedDataContainer.getSpecialAnnotations().containsKey(annotationName)) {
					String category = javaClassifiedDataContainer.getSpecialAnnotations().get(annotationName);
					javaClassifiedDataContainer.getGlobalDependencies().get(category).add(absolutePath);
				}
			}

			// Filter 및 Interceptor 클래스 식별
			if (GroupingStrategy.isClassOrInterfacee(typeDecl)) {
				ClassOrInterfaceDeclaration cid = (ClassOrInterfaceDeclaration) typeDecl;

				// 인터페이스 구현체를 수집합니다.
				if (!cid.isInterface()) {
					cid.getImplementedTypes().forEach(implType -> {
						String interfaceName = implType.getNameAsString();
						javaClassifiedDataContainer.getInterfaceImplementations().computeIfAbsent(interfaceName, k -> new ConcurrentSkipListSet<>()).add(typeName);

						// Filter 인터페이스 구현 여부 확인
						if (interfaceName.equals("Filter")) {
							javaClassifiedDataContainer.getGlobalDependencies().get("Filter").add(absolutePath);
						}

						// HandlerInterceptor 인터페이스 구현 여부 확인
						if (interfaceName.equals("HandlerInterceptor") || interfaceName.equals("AsyncHandlerInterceptor") || interfaceName.equals("HandlerInterceptorAdapter")) {
							javaClassifiedDataContainer.getGlobalDependencies().get("Interceptor").add(absolutePath);
						}
					});
				}

				// 상속을 통한 Filter 및 Interceptor 식별
				cid.getExtendedTypes().forEach(extendedType -> {
					String parentClassName = extendedType.getNameAsString();

					// Filter 상속 여부 확인
					if (parentClassName.equals("GenericFilterBean") || parentClassName.equals("OncePerRequestFilter")) {
						javaClassifiedDataContainer.getGlobalDependencies().get("Filter").add(absolutePath);
					}

					// HandlerInterceptor 상속 여부 확인
					if (parentClassName.equals("HandlerInterceptorAdapter")) {
						javaClassifiedDataContainer.getGlobalDependencies().get("Interceptor").add(absolutePath);
					}
				});
			}
		});
	}
}
