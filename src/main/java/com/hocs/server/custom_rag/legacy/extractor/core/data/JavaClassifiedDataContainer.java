package com.hocs.server.custom_rag.legacy.extractor.core.data;

import com.hocs.server.custom_rag.legacy.extractor.domain.AOP;
import com.hocs.server.custom_rag.legacy.extractor.domain.ExceptionHandler;
import com.hocs.server.custom_rag.legacy.extractor.domain.GlobalSourceDependency;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import lombok.Getter;

/**
 * 의존성을 수집하는데 핵심이 되는 데이터 클래스입니다.
 */
@Getter
public class JavaClassifiedDataContainer {

	private final Map<String, String> specialAnnotations = new ConcurrentHashMap<>();
	private final Map<String, String> classToFilePath = new ConcurrentHashMap<>();
	private final Map<String, Set<String>> interfaceImplementations = new ConcurrentHashMap<>();
	private final Set<String> controllerClasses = new ConcurrentSkipListSet<>();
	private final Map<String, Set<String>> globalDependencies = new LinkedHashMap<>();
	private final Map<String, Set<String>> simpleClassNameToQualifiedNames = new ConcurrentHashMap<>();
	private JavaClassifiedDataContainerStatus status = JavaClassifiedDataContainerStatus.NOT_INIT;

	public JavaClassifiedDataContainer() {
		// Global Dependencies 초기화
		globalDependencies.put("AOP", new ConcurrentSkipListSet<>());
		globalDependencies.put("Filter", new ConcurrentSkipListSet<>());
		globalDependencies.put("Interceptor", new ConcurrentSkipListSet<>());
		globalDependencies.put("ExceptionHandler", new ConcurrentSkipListSet<>());

		specialAnnotations.put("ControllerAdvice", "ExceptionHandler");
		specialAnnotations.put("RestControllerAdvice", "ExceptionHandler");
		specialAnnotations.put("Configuration", "Configuration");
		specialAnnotations.put("Component", "Component");
		// 필요에 따라 더 추가 가능

		// specialAnnotations에 있는 카테고리를 globalDependencies에 추가
		for (String category : specialAnnotations.values()) {
			globalDependencies.putIfAbsent(category, new ConcurrentSkipListSet<>());
		}
	}

	public void putClassToFilePath(String typeName, String absolutePath) {
		getClassToFilePath().put(typeName, absolutePath);
	}

	public void addControllerClasses(String typeName) {
		getControllerClasses().add(typeName);
	}

	public void addGlobalDependencies(String absolutePath) {
		getGlobalDependencies().get("AOP").add(absolutePath);
	}

	public GlobalSourceDependency getGlobalDependencies(String id) {
		AOP aop = AOP.create(new ArrayList<>());
		ExceptionHandler exceptionHandler = null;
		List<String> configuration = new ArrayList<>();
		List<String> component = new ArrayList<>();

		for (Map.Entry<String, Set<String>> entry : this.getGlobalDependencies().entrySet()) {
			if (!entry.getValue().isEmpty()) {
				List<String> sortedPaths = new ArrayList<>(entry.getValue());
				Collections.sort(sortedPaths);
				switch (entry.getKey()) {
					case "AOP" -> aop = AOP.create(sortedPaths);
					case "ExceptionHandler" ->
						exceptionHandler = ExceptionHandler.create(sortedPaths);
					case "Configuration" -> configuration.addAll(sortedPaths);
					case "Component" -> component.addAll(sortedPaths);
					default -> {
					}// 필요한 경우 다른 처리
				}
			}
		}

		return GlobalSourceDependency.create(id, aop, exceptionHandler, configuration, component);
	}

	public void setStatusToInit() {
		this.status = JavaClassifiedDataContainerStatus.INIT;
	}

}
