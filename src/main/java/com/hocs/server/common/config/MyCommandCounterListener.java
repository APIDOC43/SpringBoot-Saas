package com.hocs.server.common.config;

import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;
import java.util.concurrent.atomic.AtomicInteger;

public class MyCommandCounterListener implements CommandListener {

	private AtomicInteger updateCount = new AtomicInteger();

	@Override
	public void commandStarted(CommandStartedEvent event) {
		if ("update".equals(event.getCommandName())) {
			updateCount.incrementAndGet();
		}
	}

	public int getUpdateCount() {
		return updateCount.get();
	}

	public void reset() {
		updateCount.set(0);
	}
}