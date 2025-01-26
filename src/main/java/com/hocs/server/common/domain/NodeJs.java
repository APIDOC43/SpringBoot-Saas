package com.hocs.server.common.domain;

import java.nio.file.Path;
import javax.naming.OperationNotSupportedException;

public class NodeJs implements LanguageFramework {

	@Override
	public boolean isApiEntry(Path path) {
		try {
			throw new OperationNotSupportedException("Node Js Not Supported");
		} catch (OperationNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getExtension() {
		try {
			throw new OperationNotSupportedException("Node Js Not Supported");
		} catch (OperationNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
