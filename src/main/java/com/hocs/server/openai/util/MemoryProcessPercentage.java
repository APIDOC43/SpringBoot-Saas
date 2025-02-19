package com.hocs.server.openai.util;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemoryProcessPercentage {

	private static Map<String,Integer> map = new HashMap<>();
	public static void save(String userId, double value, double size) {
		int percentage = (int) ((value/size) * 100);
		log.info("percentage = " + percentage);
		map.put(userId,percentage);
	}

	public static int get(String userId) {
		return map.getOrDefault(userId,0);
	}

	public static void clear(String userId) {
		map.remove(userId);
	}
}
