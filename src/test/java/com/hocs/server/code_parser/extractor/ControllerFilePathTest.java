package com.hocs.server.code_parser.extractor;

import static org.assertj.core.api.Assertions.assertThat;

import com.hocs.server.pipline_orchestrator.domain.ControllerFile;
import org.junit.jupiter.api.Test;

class ControllerFilePathTest {


	@Test
	public void classNameParseTest() {
		String classname = "thisisclassName";
		ControllerFile controllerFile = new ControllerFile(
			"apb/abfbds/badfb/" + classname + ".class");

		assertThat(controllerFile.getClassName()).isEqualTo(classname);
	}
}