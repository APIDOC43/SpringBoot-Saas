package com.hocs.server.oas.util.cli;

import com.hocs.server.front_server.legacy.saas.util.cli.CLIManager;
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