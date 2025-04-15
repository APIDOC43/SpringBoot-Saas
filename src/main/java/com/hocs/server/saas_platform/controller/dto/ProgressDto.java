package com.hocs.server.saas_platform.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 하나의 생성 단계(step)와 그 상태(status)를 나타냅니다.
 * status 예: PENDING, IN_PROGRESS, COMPLETED, FAILED
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgressDto {
	private String status;
}
