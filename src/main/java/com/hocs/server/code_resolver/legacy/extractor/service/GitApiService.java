package com.hocs.server.code_resolver.legacy.extractor.service;

import com.hocs.server.saas.user.gitapi.domin.GitRepo;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class GitApiService {

	public String getDefaultBranch(GitRepo gitRepo) {

		try {
			URL url = new URL(
				"https://api.github.com/repos/" + gitRepo.getOwner() + "/" + gitRepo.getRepoName());

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			// User-Agent 설정 (GitHub API 요구 사항)
			conn.setRequestProperty("User-Agent", "Mozilla/5.0");

			// 응답 코드 확인
			int responseCode = conn.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) { // 응답 코드 200
				BufferedReader in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
				String inputLine;
				StringBuilder response = new StringBuilder();

				// 응답 내용 읽기
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				// JSON 파싱
				JSONObject json = new JSONObject(response.toString());
				String defaultBranch = json.getString("default_branch");
				System.out.println("기본 브랜치는: " + defaultBranch);
				return defaultBranch;
			} else {
				System.out.println("GET 요청 실패. 응답 코드: " + responseCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "main"; //default
	}

	public String buildSourceCodeUrl(GitRepo gitRepo, String entrySrcPath) {
		if (entrySrcPath.charAt(0) != '/') {
			entrySrcPath = "/" + entrySrcPath;
		}
		String defaultBranch = getDefaultBranch(gitRepo);
		return gitRepo.getUrl() + "/blob/" + defaultBranch + "/" + entrySrcPath;
	}
}
