package com.hocs.server.common.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Embeddable;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class ClientProjectPath {

	@Convert(converter = PathConverter.class)
	private Path path;
	
	public ClientProjectPath(Path path) {
		if (path == null) {
			throw new IllegalArgumentException("Path cannot be null");
		}
		this.path = path;
	}

	public File getToFile(){
		return path.toFile();
	}

	public static ClientProjectPath of(String pathString) {
		if (pathString == null || pathString.trim().isEmpty()) {
			throw new IllegalArgumentException("Path string cannot be null or empty");
		}
		return new ClientProjectPath(Paths.get(pathString));
	}

	public static ClientProjectPath of(Path path) {
		return new ClientProjectPath(path);
	}
}
@Converter(autoApply = true)
class PathConverter implements AttributeConverter<Path, String> {

	@Override
	public String convertToDatabaseColumn(Path attribute) {
		return (attribute != null) ? attribute.toString() : null;
	}

	@Override
	public Path convertToEntityAttribute(String dbData) {
		return (dbData != null && !dbData.isEmpty()) ? Paths.get(dbData) : null;
	}
}

