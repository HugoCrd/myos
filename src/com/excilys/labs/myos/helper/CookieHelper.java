package com.excilys.labs.myos.helper;


import java.util.Map;

import org.vertx.java.core.http.HttpServerRequest;

public class CookieHelper {

	public static String getSessionid(Map<String, String> headers){
		if(headers.get("Cookie")!=null)
			return headers.get("Cookie").replaceFirst("sessionid=", "").replaceFirst(";.*", "");
		else
			return null;
	}
	
	public static void setSessionid(String sessionid, HttpServerRequest req){
		req.response.headers().put("Set-Cookie", "sessionid="+sessionid+"; Expires=Tue, 15 Jan 2013 20:00:00 GMT; Path=/; Domain=localhost");
	}
}
