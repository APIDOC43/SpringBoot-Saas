package com.hocs.server.api_spec_generator.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileManager {

    public static void saveToFile(String filePath, String content) {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }
        
        try {
            Path path = Paths.get(filePath);
            // 디렉터리가 존재하지 않으면 생성
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
