package com.hocs.server.openai.util;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileManager {


	public static void saveToFile(String content,String name) {
		try {
			Files.writeString(Paths.get(name), content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}





}
