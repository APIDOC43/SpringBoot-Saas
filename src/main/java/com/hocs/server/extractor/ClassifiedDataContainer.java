package com.hocs.server.extractor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Getter
@Component
@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ClassifiedDataContainer {

	private final Map<String, String> specialAnnotations = new HashMap<>();
	private final Map<String, String> classToFilePath = new HashMap<>();
	private final Map<String, Set<String>> interfaceImplementations = new HashMap<>();
	private final Set<String> controllerClasses = new HashSet<>();
	private final Map<String, Set<String>> globalDependencies = new LinkedHashMap<>();
	private final Map<String, Set<String>> simpleClassNameToQualifiedNames = new HashMap<>();

	public ClassifiedDataContainer() {
		// Global Dependencies 초기화
		globalDependencies.put("AOP", new HashSet<>());
		globalDependencies.put("Filter", new HashSet<>());
		globalDependencies.put("Interceptor", new HashSet<>());
		globalDependencies.put("ExceptionHandler", new HashSet<>());

		specialAnnotations.put("ControllerAdvice", "ExceptionHandler");
		specialAnnotations.put("RestControllerAdvice", "ExceptionHandler");
		specialAnnotations.put("Configuration", "Configuration");
		specialAnnotations.put("Component", "Component");
		// 필요에 따라 더 추가 가능

		// specialAnnotations에 있는 카테고리를 globalDependencies에 추가
		for (String category : specialAnnotations.values()) {
			globalDependencies.putIfAbsent(category, new HashSet<>());
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
	public void putSimpleToQualifiedName(String simpleName, String fullyQualifiedName) {
		simpleClassNameToQualifiedNames.computeIfAbsent(simpleName, k -> new HashSet<>()).add(fullyQualifiedName);
	}
}
