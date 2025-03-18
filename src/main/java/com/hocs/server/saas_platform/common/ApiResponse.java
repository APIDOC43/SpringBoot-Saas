package com.hocs.server.saas_platform.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ApiResponse<T> {

	private int status; // HTTP 상태 코드 또는 응답 상태 코드
	private T data;     // 실제 응답 데이터

	public static <T> ApiResponse<T> create(int status,T data){
	        return new ApiResponse<>(status,data);
	    }
}