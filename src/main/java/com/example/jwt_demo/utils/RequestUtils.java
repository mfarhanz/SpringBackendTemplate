package com.example.jwt_demo.utils;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtils {
	
	public static String getClientIp(HttpServletRequest request) {
	    String ip = request.getHeader("X-Forwarded-For");
	    if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
	        // Sometimes X-Forwarded-For can contain a comma-separated list of IPs
	        return ip.split(",")[0].trim();
	    }
	    ip = request.getHeader("Proxy-Client-IP");
	    if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
	        return ip;
	    }
	    ip = request.getHeader("WL-Proxy-Client-IP");  // WebLogic
	    if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
	        return ip;
	    }
	    // Fallback to remote address if no headers found
	    return request.getRemoteAddr();
	}

}
