package com.hocs.server.saas_v2.service.out.language_framwork.adapter;

import com.hocs.server.saas_v2.service.out.language_framwork.port.NodeJsPort;
import java.nio.file.Path;
import javax.naming.OperationNotSupportedException;

public class NodeJs implements NodeJsPort {

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
