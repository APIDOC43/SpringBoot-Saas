package com.hocs.server.saas_v2.out.language_framwork.adapter;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.hocs.server.extractor.core.util.GroupingStrategy;
import com.hocs.server.saas_v2.out.language_framwork.port.SpringBootJavaPort;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SpringBootJava implements SpringBootJavaPort {

	@Override
	public boolean isApiEntry(Path path) {

		try {
			String content = new String(Files.readAllBytes(path));
			CompilationUnit cu = StaticJavaParser.parse(content);

			for (TypeDeclaration<?> typeDeclaration : cu.findAll(TypeDeclaration.class)) {
				if (typeDeclaration.isAnnotationPresent("RestController")) {
					return true; // 컨트롤러 클래스 식별
				}
			}

			return false;

		} catch (IOException e) {
			System.err.println("SpringBootJava.isApiEntry content is fileRead Error");
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getExtension() {
		return "java";
	}
}
