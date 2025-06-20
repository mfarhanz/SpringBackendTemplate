package com.example.jwt_demo.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.example.jwt_demo.annotations.LogEndpoint;
import com.example.jwt_demo.dto.ApiResponse;
import com.example.jwt_demo.utils.RequestUtils;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@annotation(com.example.jwt_demo.annotations.LogEndpoint)")
    public Object logAroundEndpoint(ProceedingJoinPoint joinPoint) throws Throwable {
    	Object[] args = joinPoint.getArgs();
    	HttpServletRequest request = (HttpServletRequest) (args.length > 1 ? args[1] : args[0]);
    	MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    	LogEndpoint annotation = signature.getMethod().getAnnotation(LogEndpoint.class);
        String methodName = signature.toShortString();
        String ipAddr = RequestUtils.getClientIp(request);
        int ipPort = request.getRemotePort();
        long methodBegin = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            ResponseEntity<ApiResponse> parsedResult = (ResponseEntity<ApiResponse>) result;
            log.info("{}:{} - Executed {} ({}) in {} ms with Status: {}", 
            		ipAddr, ipPort, methodName, annotation.methodType(), (System.nanoTime() - methodBegin) / 1_000_000, parsedResult.getStatusCode().value());
            return result;
        } catch (Throwable ex) {
            log.error("{}:{} - Executed {} ({}) in {} ms and threw Error: {}", 
            		ipAddr, ipPort, methodName, annotation.methodType(), (System.nanoTime() - methodBegin) / 1_000_000, ex.getClass().getSimpleName());
            throw ex;
        }
    }
}
