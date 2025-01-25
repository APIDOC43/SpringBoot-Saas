package com.hocs.server.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import java.io.File;
import java.nio.file.Path;
import jakarta.persistence.Embeddable;
import java.nio.file.Paths;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Embeddable
public class ClientProjectPath {

	@Convert(converter = PathConverter.class)
	private Path path;

	public File getToFile(){
		return path.toFile();
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

