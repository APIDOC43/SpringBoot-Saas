package com.hocs.server.code_resolver.legacy.extractor.core.util;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

public class GroupingStrategy {

	public static boolean isAspect(TypeDeclaration typeDecl) {
		return typeDecl.isAnnotationPresent("Aspect");
	}

	public static boolean isController(TypeDeclaration typeDecl) {
		return typeDecl.isAnnotationPresent("RestController");
	}

	public static boolean isClassOrInterfacee(TypeDeclaration typeDecl) {
		return typeDecl instanceof ClassOrInterfaceDeclaration;
	}


}
