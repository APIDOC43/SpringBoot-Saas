package com.hocs.server.extractor.util;

import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class FileManager {

	/**
	 * 출력 데이터를 YAML 파일로 저장합니다.
	 */
	public static void saveOutputAsYaml(List<Map<String, Object>> data, String outputFile) throws Exception {
		DumperOptions options = new DumperOptions();
		options.setIndent(4); // 들여쓰기 설정
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

		Yaml yaml = new Yaml(options);
		try (FileWriter writer = new FileWriter(outputFile)) {
			yaml.dump(data, writer);
		}
		System.out.println("Output saved to " + outputFile);
	}
}
