package com.hocs.server.code_parser.core.service;


import static com.hocs.server.code_parser.core.util.HttpMethodUtil.haveHttpMethodAnnotation;

import com.github.javaparser.ast.CompilationUnit;
import com.hocs.server.code_parser.core.config.GlobalJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.hocs.server.code_parser.core.dataobject.JavaClassifiedDataContainer;
import com.hocs.server.code_parser.core.domain.API;
import com.hocs.server.code_parser.core.domain.ApiEndpoint;
import com.hocs.server.code_parser.core.util.EndpointPathUtil;
import com.hocs.server.code_parser.core.util.ParameterSupportAnnotations;
import com.hocs.server.common.domain.MethodInformation;
import com.hocs.server.pipline_orchestrator.ratelimit.PipelineTask;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


/**
 * 컨트롤러 클래스의 API 메서드를 추적하여 의존성을 수집하는 클래스입니다.
 */
@Component
@RequiredArgsConstructor
public class DependencyAnalyzer {

	private final DependencyExplorer dependencyExplorer;
	private final GlobalJavaParser globalJavaParser;


	/**
	 * 하나의 컨트롤러 클래스의 모든 API 메서드를 추적하여 하나의 API가 실행될 때 필요한 파일 경로를 수집합니다.
	 */
	public List<API> findDependency(String className, JavaClassifiedDataContainer container) throws Exception {
		List<API> APIs = new ArrayList<>();

		String filePath = container.getClassToFilePath().get(className);//controller file path
		if (filePath == null) {
			return null;
		}

		String srcContent = new String(Files.readAllBytes(Paths.get(filePath)));
		CompilationUnit srcContentUnit;
		try {
			srcContentUnit = globalJavaParser.parse(srcContent);
		} catch (Exception e) {
			System.err.println("Failed to parse controller class: " + className);
			e.printStackTrace();
			return null ;
		}

		//import문 제거
		TypeDeclaration<?> classDeclaration = srcContentUnit.getType(0);

		// 클래스의 @RequestMapping 경로 추출
		String basePath = EndpointPathUtil.findRequestMappingValue(classDeclaration);

		// 클래스의 모든 메서드를 순회합니다.
		for (MethodDeclaration method : classDeclaration.getMethods()) {
			if (haveHttpMethodAnnotation(method)) {

				// endpoint 정보 추출 = http method, url
				ApiEndpoint apiEndpoint = EndpointPathUtil.generateApiEndpoint(basePath, method);

				// API별로 필요한 파일 경로를 추적합니다.
				Set<String> requiredFiles = new HashSet<>();
				Set<String> visitedClasses = new HashSet<>(); // 재귀적 추적을 위한 방문한 클래스 집합
				requiredFiles.add(filePath); // 컨트롤러 파일 자체 추가


				// 컨트롤러 클래스의 의존성도 추적
				dependencyExplorer.findClassDependencies(className, requiredFiles, visitedClasses, container);

				// 메소드 파라미터 의존성 추적 [지원하는 모든 파라미터 어노테이션 및 MultipartFile 처리]
				for (Parameter param : method.getParameters()) {
					boolean isSupported = false;
					for (AnnotationExpr paramAnnotation : param.getAnnotations()) {
						if (ParameterSupportAnnotations.supportedAnnotations.contains(paramAnnotation.getNameAsString())) {
							isSupported = true;
							break;
						}
					}
					// 파라미터 어노테이션이 지원되거나 타입이 MultipartFile인 경우 처리
					if (isSupported || param.getType().asString().equals("MultipartFile")) {
						String paramType = param.getType().asString();
						dependencyExplorer.findClassDependencies(paramType, requiredFiles, visitedClasses, container);
					}
				}

				// 메서드의 반환 타입에서 의존성 추적
				if (method.getType() != null) {
					String returnType = method.getType().asString();
					dependencyExplorer.findClassDependencies(returnType, requiredFiles, visitedClasses, container);
				}

				// 중복을 방지하고 정렬된 리스트로 변환
				List<String> sortedPaths = new ArrayList<>(requiredFiles);
				Collections.sort(sortedPaths);

				APIs.add(API.create(apiEndpoint,sortedPaths));
			}
		}

		return APIs;
	}

	/**
	 * 하나의 컨트롤러 클래스의 하나의 API 메서드를 추적하여 하나의 API가 실행될 때 필요한 파일 경로를 수집합니다.
	 */
	public API findDependencyByTask(String className, PipelineTask task,JavaClassifiedDataContainer container) throws Exception {

		String filePath = container.getClassToFilePath().get(className);//controller file path
		if (filePath == null) {
			return null;
		}

		String srcContent = new String(Files.readAllBytes(Paths.get(filePath)));
		CompilationUnit srcContentUnit;
		try {
			srcContentUnit = globalJavaParser.parse(srcContent);
		} catch (Exception e) {
			System.err.println("Failed to parse controller class: " + className);
			e.printStackTrace();
			return null ;
		}

		//import문 제거
		TypeDeclaration<?> classDeclaration = srcContentUnit.getType(0);

		// 클래스의 @RequestMapping 경로 추출
		String basePath = EndpointPathUtil.findRequestMappingValue(classDeclaration);

		// 클래스의 모든 메서드를 순회합니다.
		for (MethodDeclaration method : classDeclaration.getMethods()) {
			if(!task.getApiInfo().getMethodSignature().getSignature().equals((new MethodInformation(method)).getSignature())){
				continue;
			}

			if (haveHttpMethodAnnotation(method)) {

				// endpoint 정보 추출 = http method, url
				ApiEndpoint apiEndpoint = EndpointPathUtil.generateApiEndpoint(basePath, method);

				// API별로 필요한 파일 경로를 추적합니다.
				Set<String> requiredFiles = new HashSet<>();
				Set<String> visitedClasses = new HashSet<>(); // 재귀적 추적을 위한 방문한 클래스 집합
				requiredFiles.add(filePath); // 컨트롤러 파일 자체 추가


				// 컨트롤러 클래스의 의존성도 추적
				dependencyExplorer.findClassDependencies(className, requiredFiles, visitedClasses, container);

				// 메소드 파라미터 의존성 추적 [지원하는 모든 파라미터 어노테이션 및 MultipartFile 처리]
				for (Parameter param : method.getParameters()) {
					boolean isSupported = false;
					for (AnnotationExpr paramAnnotation : param.getAnnotations()) {
						if (ParameterSupportAnnotations.supportedAnnotations.contains(paramAnnotation.getNameAsString())) {
							isSupported = true;
							break;
						}
					}
					// 파라미터 어노테이션이 지원되거나 타입이 MultipartFile인 경우 처리
					if (isSupported || param.getType().asString().equals("MultipartFile")) {
						String paramType = param.getType().asString();
						dependencyExplorer.findClassDependencies(paramType, requiredFiles, visitedClasses, container);
					}
				}

				// 메서드의 반환 타입에서 의존성 추적
				if (method.getType() != null) {
					String returnType = method.getType().asString();
					dependencyExplorer.findClassDependencies(returnType, requiredFiles, visitedClasses, container);
				}

				// 중복을 방지하고 정렬된 리스트로 변환
				List<String> sortedPaths = new ArrayList<>(requiredFiles);
				Collections.sort(sortedPaths);

				return API.create(apiEndpoint,sortedPaths);
			}
		}

		throw new RuntimeException("Controller 파일에 없는 API 생성요청");
	}
}

