package com.hocs.server.saas_platform.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CLIManager {
	public String executeCommand(String[] command){

		try {
			// 명령어 실행
			Process process = Runtime.getRuntime().exec(command);

			// 명령어 실행 결과를 읽기 위한 BufferedReader 생성 (표준 출력)
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			// 표준 오류 출력을 읽기 위한 BufferedReader 생성 (표준 오류 출력)
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			String line;

			StringBuffer sb = new StringBuffer();

			// 표준 출력 출력
			while ((line = reader.readLine()) != null) {
				System.out.println("Output: " + line);
				sb.append(line).append("\n");
			}

			// 표준 오류 출력
			while ((line = errorReader.readLine()) != null) {
				System.err.println("Error: " + line);
			}

			// 종료 상태 확인
			int exitCode = process.waitFor();
			log.info("Exit code: {}", exitCode);

			return sb.toString();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
