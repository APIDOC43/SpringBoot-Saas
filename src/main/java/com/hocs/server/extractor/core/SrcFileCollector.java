package com.hocs.server.extractor.core;

import com.hocs.server.extractor.domain.ClientProjectType;
import com.hocs.server.extractor.domain.SrcSuffix;
import com.hocs.server.openai.domain.APIEndpoint;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 소스 코드 파일을 수집하는 클래스
 */
@Service
@RequiredArgsConstructor
public class SrcFileCollector {

	/**
	 * apiEntries에 포함되지 않은 소스 코드 파일을 수집합니다.
	 *
	 * @return 파일 목록
	 */
	public List<File> getNotUesedSrc(List<APIEndpoint> apiEntries,String PROJECT_ROOT,ClientProjectType clientProjectType){

		String absolutePath = new File(PROJECT_ROOT, clientProjectType.srcRootPath()).getAbsolutePath();

		List<File> files = collectFiles(new File(absolutePath), SrcSuffix.ALL);
		Iterator<File> iterator = files.iterator();
		while (iterator.hasNext()) {
			File file = iterator.next();
			for (APIEndpoint apiEndpoint : apiEntries) {
				for (String path : apiEndpoint.getPaths()) {
					if (file.getPath().equals(path)) {
						iterator.remove();
						break;
					}
				}
			}
		}

		return files;
	}


	/**
	 * dir을 기준으로 suffix에 해당하는 파일을 모두 수집합니다.
	 *
	 * @return 파일 목록
	 */
	public List<File> collectFiles(File dir, SrcSuffix srcSuffix) {
		List<File> javaFiles = new ArrayList<>();

		for (File file : Objects.requireNonNull(dir.listFiles())) {
			if (file.isDirectory()) {
				javaFiles.addAll(collectFiles(file,srcSuffix));
			} else {
				if (file.getName().endsWith(srcSuffix.value())) {
					javaFiles.add(file);
				}
			}
		}

		return javaFiles;
	}

}
