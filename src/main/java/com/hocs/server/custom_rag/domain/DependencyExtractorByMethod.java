package com.hocs.server.custom_rag.domain;

import static com.hocs.server.custom_rag.legacy.extractor.core.util.ParameterSupportAnnotations.supportedAnnotations;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.hocs.server.custom_rag.legacy.extractor.core.DependencyExplorer;
import com.hocs.server.custom_rag.legacy.extractor.core.data.JavaClassifiedDataContainer;
import com.hocs.server.common.domain.MethodInformation;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor

/**
 * 소스코드의 각 메소드를 기준으로 재귀적으로 의존성을 수집합니다.
 */
public class DependencyExtractorByMethod {

	private final DependencyExplorer dependencyExplorer;
	public Map<MethodInformation,List<String>> findDependency(String className,JavaClassifiedDataContainer dataContainer) throws Exception {
		Map<MethodInformation,List<String>> methodAndDependencyMap = new HashMap<>();
		List<String> sortedPaths = null;

		String filePath = dataContainer.getClassToFilePath().get(className);

		if (filePath == null) {
			throw new RuntimeException("file path is null");
		}

		String srcContent = new String(Files.readAllBytes(Paths.get(filePath)));
		CompilationUnit srcContentUnit;
		try {
			srcContentUnit = StaticJavaParser.parse(srcContent);
		} catch (Exception e) {
			System.err.println("Failed to parse controller class: " + className);
			e.printStackTrace();
			throw e;
		}

		//import문 제거
		TypeDeclaration<?> classDeclaration = srcContentUnit.getType(0);


		// 클래스의 모든 메서드를 순회합니다.
		for (MethodDeclaration method : classDeclaration.getMethods()) {
			Set<String> requiredFiles = new HashSet<>();
			Set<String> visitedClasses = new HashSet<>(); // 재귀적 추적을 위한 방문한 클래스 집합
			requiredFiles.add(filePath); // 컨트롤러 파일 자체 추가


			// 컨트롤러 클래스의 의존성도 추적
			dependencyExplorer.findClassDependencies(className, requiredFiles, visitedClasses);

			// 메소드 파라미터 의존성 추적 [지원하는 모든 파라미터 어노테이션 및 MultipartFile 처리]
			for (Parameter param : method.getParameters()) {
				boolean isSupported = false;
				for (AnnotationExpr paramAnnotation : param.getAnnotations()) {
					if (supportedAnnotations.contains(paramAnnotation.getNameAsString())) {
						isSupported = true;
						break;
					}
				}
				// 파라미터 어노테이션이 지원되거나 타입이 MultipartFile인 경우 처리
				if (isSupported || param.getType().asString().equals("MultipartFile")) {
					String paramType = param.getType().asString();
					dependencyExplorer.findClassDependencies(paramType, requiredFiles, visitedClasses);
				}
			}

			// 메서드의 반환 타입에서 의존성 추적
			if (method.getType() != null) {
				String returnType = method.getType().asString();
				dependencyExplorer.findClassDependencies(returnType, requiredFiles, visitedClasses);
			}
			//메소드 네임 이랑 매핑
			// 중복을 방지하고 정렬된 리스트로 변환
			sortedPaths = new ArrayList<>(requiredFiles);
			Collections.sort(sortedPaths);

			MethodInformation methodSignature = new MethodInformation(method);

			List<String> orDefault = methodAndDependencyMap.getOrDefault(methodSignature,
				new ArrayList<>());
			orDefault.addAll(sortedPaths);
			methodAndDependencyMap.put(methodSignature,sortedPaths);
		}

		return methodAndDependencyMap;
	}
}