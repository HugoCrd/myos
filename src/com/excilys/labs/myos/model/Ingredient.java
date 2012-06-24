package com.excilys.labs.myos.model;

import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.json.JsonObject;

public enum Ingredient {
	 OGNON, GARLIC, CARROT, WINE, BUTTER ,PORK;
	 
	 public static JsonObject getAsJson() {
		Map<String, Object> map = new HashMap<String, Object>();
			for(Ingredient ingredient : values()){
				map.put(ingredient.toString(), 0f);
			}
		return new JsonObject(map);
	}
}
