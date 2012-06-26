package com.excilys.labs.myos.model;

import org.vertx.java.core.json.JsonObject;

public enum Ingredient {
	 OGNON, GARLIC, CARROT, WINE, BUTTER ,PORK;
	 
	 public static JsonObject getAsJson() {
		 JsonObject json = new JsonObject();
			for(Ingredient ingredient : values()){
				json.putNumber(ingredient.toString(), 0);
			}
		return json;
	}
}
