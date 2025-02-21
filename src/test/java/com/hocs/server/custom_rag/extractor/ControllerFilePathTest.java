package com.hocs.server.custom_rag.extractor;

import static org.assertj.core.api.Assertions.assertThat;

import com.hocs.server.api_doc_pipline.domain.ControllerFile;
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