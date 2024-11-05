package com.hocs.server.extractor;

import com.hocs.server.openai.llm.ApiEntryMapper.APIEntry;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CodeStructuresAnalyzer {

	private final CodeCategorizer codeCategorizer;


	public List<File> getNotUesedSrc(List<APIEntry> apiEntries,String PROJECT_ROOT){


		String absolutePath = new File(PROJECT_ROOT, "src").getAbsolutePath();
		List<File> files = new ArrayList<>();
		collectAllFiles(new File(absolutePath), files);
		Iterator<File> iterator = files.iterator();
		while (iterator.hasNext()) {
			File file = iterator.next();
			for (APIEntry apiEntry : apiEntries) {
				for (String path : apiEntry.getPaths()) {
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
	 * 소스 코드의 모든 Java 파일을 재귀적으로 수집합니다.
	 */
	public void collectJavaFiles(File dir, List<File> javaFiles) {
		for (File file : Objects.requireNonNull(dir.listFiles())) {
			if (file.isDirectory()) {
				collectJavaFiles(file, javaFiles);
			} else if (file.getName().endsWith(".java")) {
				javaFiles.add(file);
			}
		}
	}
	private void collectAllFiles(File dir, List<File> files) {
		for (File file : Objects.requireNonNull(dir.listFiles())) {
			if (file.isDirectory()) {
				collectJavaFiles(file, files);
			} else {
				files.add(file);
			}
		}
	}




}
