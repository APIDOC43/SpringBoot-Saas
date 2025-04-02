package com.hocs.server.pipline_orchestrator.ratelimit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TaskContextStore {

	private static final ConcurrentMap<String, TaskContext> CONTEXT_MAP = new ConcurrentHashMap<>();

	// 등록
	private static void put(String requestId, TaskContext context) {
		CONTEXT_MAP.put(requestId, context);
	}

	// 조회
	public static TaskContext get(String requestId) {
		return CONTEXT_MAP.get(requestId);
	}

	// 삭제
	public static void remove(String requestId) {
		CONTEXT_MAP.remove(requestId);
	}

	// 존재 확인 (선택적)
	public static boolean contains(String requestId) {
		return CONTEXT_MAP.containsKey(requestId);
	}


	public static void save(ThrottleRequest request) {
		RateLimitRequestData data = request.getData();
		TaskContext taskContext = new TaskContext(
			data.getRequest().getUserId(),
			data.getDefaultBranchName(),
			data.getFilenamesRelatedException(),
			data.getMetaData(),
			request.getTaskType(),
			data.taskSize()
		);

		put(data.getRequest().getRequestId(),taskContext);
	}
}

