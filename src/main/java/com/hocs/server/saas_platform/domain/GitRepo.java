package com.hocs.server.saas_platform.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Slf4j
public class GitRepo {
    private final String url;
    private final String owner;
    private final String repoName;

    public static GitRepo of(String repoUrl) {
        if (repoUrl == null) {
            throw new IllegalArgumentException("Repository URL cannot be null");
        }
        
        if (repoUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository URL cannot be empty");
        }

        // SSH URL 형식 처리 (git@github.com:owner/repo.git)
        if (repoUrl.startsWith("git@")) {
            return parseSshUrl(repoUrl);
        }

        // HTTPS URL 형식 처리
        return parseHttpsUrl(repoUrl);
    }

    private static GitRepo parseSshUrl(String repoUrl) {
        try {
            // git@github.com:owner/repo.git 형식
            String[] parts = repoUrl.split(":");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid SSH URL format");
            }
            
            String pathPart = parts[1];
            if (pathPart.endsWith(".git")) {
                pathPart = pathPart.substring(0, pathPart.length() - 4);
            }
            
            String[] pathParts = pathPart.split("/");
            if (pathParts.length < 2) {
                throw new IllegalArgumentException("Invalid SSH URL format");
            }
            
            String owner = pathParts[pathParts.length - 2];
            String repoName = pathParts[pathParts.length - 1];
            
            // SSH URL을 HTTPS 형식으로 변환
            String httpsUrl = "https://" + parts[0].substring(4) + "/" + pathPart;
            
            return new GitRepo(httpsUrl, owner, repoName);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse SSH URL: " + repoUrl, e);
        }
    }

    private static GitRepo parseHttpsUrl(String repoUrl) {
        try {
            String cleanUrl = repoUrl;
            
            // .git 확장자 제거 (쿼리 파라미터가 있으면 고려)
            if (cleanUrl.contains(".git?")) {
                cleanUrl = cleanUrl.replace(".git?", "?");
            } else if (cleanUrl.endsWith(".git")) {
                cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 4);
            }

            // URL을 '/'로 분할 (쿼리 파라미터 제거 후)
            String urlForParsing = cleanUrl;
            if (urlForParsing.contains("?")) {
                urlForParsing = urlForParsing.substring(0, urlForParsing.indexOf("?"));
            }
            
            String[] urlParts = urlForParsing.split("/");
            
            if (urlParts.length < 5) {
                throw new IllegalArgumentException("Invalid URL format. Expected format: https://host/owner/repo");
            }

            // 마지막 두 부분이 owner/repo
            String owner = urlParts[urlParts.length - 2];
            String repoName = urlParts[urlParts.length - 1];
            
            // 원본 URL에서 쿼리 파라미터가 있으면 repoName에 추가
            if (cleanUrl.contains("?")) {
                String queryPart = cleanUrl.substring(cleanUrl.indexOf("?"));
                repoName += queryPart;
            }

            return new GitRepo(cleanUrl, owner, repoName);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse HTTPS URL: " + repoUrl, e);
        }
    }
}
