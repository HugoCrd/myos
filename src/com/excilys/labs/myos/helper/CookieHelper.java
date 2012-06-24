package com.excilys.labs.myos.helper;


import java.util.Map;

public class CookieHelper {

	public static String getSessionid(Map<String, String> headers){
		if(headers.get("Cookie")!=null)
			return headers.get("Cookie").replaceFirst("sessionid=", "").replaceFirst(";.*", "");
		else
			return null;
	}
}
