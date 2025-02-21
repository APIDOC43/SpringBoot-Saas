package com.hocs.server.custom_rag.legacy.extractor.core.util;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GenericTypeResolver {
	/**
	 * 주어진 타입 문자열에서 클래스 이름을 추출합니다.
	 * @ex) : List-ProductResponse -> ["List", "ProductResponse"]
	 */
	public List<String> extractClassNamesFromType(String typeStr) {
		List<String> classNames = new ArrayList<>();
		if (typeStr.contains("<") && typeStr.contains(">")) {
			// 제네릭 타입 처리
			int start = typeStr.indexOf('<');
			int end = typeStr.lastIndexOf('>');
			String mainType = typeStr.substring(0, start).trim();
			String genericTypes = typeStr.substring(start + 1, end).trim();
			classNames.add(mainType);
			// 제네릭 타입이 여러 개인 경우 쉼표로 분리
			String[] generics = genericTypes.split(",");
			for (String generic : generics) {
				classNames.add(generic.trim());
			}
		} else {
			classNames.add(typeStr);
		}
		return classNames;
	}

}
