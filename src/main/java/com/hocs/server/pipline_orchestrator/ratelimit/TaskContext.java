package com.hocs.server.pipline_orchestrator.ratelimit;

import com.hocs.server.common.domain.ProjectMetaData;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

@Getter
public class TaskContext {

	private final String userId;
	private final String defaultBranchName;
	private final String[] filenamesRelatedException;
	private final ProjectMetaData projectMetaData;
	private final TaskType taskType;
	private final AtomicInteger completeCount = new AtomicInteger(0);
	private final int taskSize;

	public TaskContext(String userId, String defaultBranchName, String[] filenamesRelatedException,
		ProjectMetaData projectMetaData, TaskType taskType, int taskSize) {
		this.userId = userId;
		this.defaultBranchName = defaultBranchName;
		this.filenamesRelatedException = filenamesRelatedException;
		this.projectMetaData = projectMetaData;
		this.taskType = taskType;
		this.taskSize = taskSize;
	}

	public int incrementAndGetCompleteCount() {
		return this.completeCount.incrementAndGet();
	}
}