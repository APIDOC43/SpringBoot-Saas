package com.hocs.server.openai.util;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileManager {


	public static void saveToFile(String content,String name) {
		try {
			Files.writeString(Paths.get(name), content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}





}
