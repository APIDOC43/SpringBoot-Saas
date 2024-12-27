package com.hocs.server.aop.apicounter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class EndpointCounterAspect {

	private final ApiCounterService counterService;

	@Pointcut("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Controller *)")
	public void restOrMvcController() {}

	@Around("restOrMvcController() && execution(* *(..))")
	public Object countEndpointCall(ProceedingJoinPoint joinPoint) throws Throwable {
		String endpoint = joinPoint.getSignature().toShortString();
		counterService.increaseCount(endpoint);
		return joinPoint.proceed();
	}
}
