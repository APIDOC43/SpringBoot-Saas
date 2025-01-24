package com.hocs.server.code_resolver.domain;

import java.nio.file.Path;
import java.util.Objects;
import lombok.Getter;

@Getter
public class ControllerFile extends SourceFile {
	private final String className;

	public ControllerFile(String path) {
		super(Path.of(path));
		this.className = path.substring(path.lastIndexOf('/')+1,path.lastIndexOf('.'));
	}

	// hashCode() 재정의
	@Override
	public int hashCode() {
		return Objects.hash(path, className); // id와 name을 기반으로 해시코드 생성
	}

	// equals() 재정의
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true; // 동일 객체
		if (obj == null || getClass() != obj.getClass()) return false; // 타입 체크

		ControllerFile other = (ControllerFile) obj; // 필드 비교
		return Objects.equals(path, other.path) && Objects.equals(className, other.className);
	}

}
