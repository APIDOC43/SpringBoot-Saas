package com.hocs.server.oas.util.cli;

import com.hocs.server.saas_v2.legacy.saas.util.cli.CLIManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CLIManagerTest {

	private final CLIManager cliManager = new CLIManager();

	@Test
	@DisplayName("CLIManagerTest - executeCommand")
	public void executeCommandTest(){
		String args[] = {"ls"};
		cliManager.executeCommand(args);
	}

}