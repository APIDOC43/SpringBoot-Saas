package com.hocs.server.custom_rag.legacy.extractor.domain;

import jakarta.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalSourceDependency {

	/* [example]
	    -   Global:
				AOP:
				- /var/folders/c4/djj8knkd4g38rchhdk2ph7d00000gn/T/cloneRepo15326191733021736194/src/main/java/com/practice/ecommerce/user/aop/LoginCheckAspect.java
				ExceptionHandler:
				- /var/folders/c4/djj8knkd4g38rchhdk2ph7d00000gn/T/cloneRepo15326191733021736194/src/main/java/com/practice/ecommerce/user/aop/GlobalExceptionHandler.java
				Configuration:
				- /var/folders/c4/djj8knkd4g38rchhdk2ph7d00000gn/T/cloneRepo15326191733021736194/src/main/java/com/practice/ecommerce/config/JpaAuditingConfig.java
				- /var/folders/c4/djj8knkd4g38rchhdk2ph7d00000gn/T/cloneRepo15326191733021736194/src/main/java/com/practice/ecommerce/config/RedisConfig.java
				Component:
				- /var/folders/c4/djj8knkd4g38rchhdk2ph7d00000gn/T/cloneRepo15326191733021736194/src/main/java/com/practice/ecommerce/user/aop/LoginCheckAspect.java
	 */
	@Id
	private String id;

	@Field("aop")
	private AOP aop;

	@Field("exceptionHandler")
	private ExceptionHandler exceptionHandler;

	@Field("configuration")
	private List<String> configuration;

	@Field("component")
	private List<String> component;

	public static GlobalSourceDependency create(String id, AOP aop,
		ExceptionHandler exceptionHandler,
		List<String> configuration, List<String> component) {
		return new GlobalSourceDependency(id, aop, exceptionHandler, configuration, component);
	}

	public List<String> getAllSourcePathList(){
		List<String> srcList = new ArrayList<>();

		for (String src : aop.getPaths()) {
			srcList.add(src);
		}
		for (String src : exceptionHandler.getPaths()) {
			srcList.add(src);
		}

		for (String src : configuration) {
			srcList.add(src);
		}

		for (String src : component) {
			srcList.add(src);
		}

		return srcList;
	}
}
