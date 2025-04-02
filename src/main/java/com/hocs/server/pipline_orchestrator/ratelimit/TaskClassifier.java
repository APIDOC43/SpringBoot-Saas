package com.hocs.server.pipline_orchestrator.ratelimit;

import org.springframework.stereotype.Component;

@Component
public class TaskClassifier {

	public TaskType classify(int size) {
		if(size > 200){
			return TaskType.HEAVY;
		}else{
			return TaskType.FAST;
		}
	}
}