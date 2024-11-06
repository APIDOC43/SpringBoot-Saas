package com.hocs.server.aop.apicounter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiCounter {

	@Id
	private String endpoint;

	private int count;

	public static ApiCounter of(String endpoint){
	        return new ApiCounter(endpoint,0);
	    }

	public void plus(int c) {
		this.count+=c;
	}
}
