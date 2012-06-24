package com.excilys.labs.myos.helper;


import java.util.Map;

public class CookieHelper {

	public static String getSessionid(Map<String, String> headers){
		return headers.get("Cookie").replaceFirst("sessionid=", "").replaceFirst(";.*", "");
	}
}
