package com.hocs.server.saas_v2.out.language_framwork.adapter;

import com.hocs.server.saas_v2.out.language_framwork.port.SpringBootJavaPort;
import java.nio.file.Path;

public class SpringBootJava implements SpringBootJavaPort{

	@Override
	public boolean isApiEntry(Path path) {
		return false;
	}
}
