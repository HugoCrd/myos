package com.excilys.labs.recipe;

import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.json.JsonObject;


public class Recipe {

	private Map<Ingredient, Integer> ingredients = new HashMap<Ingredient, Integer>();
	
	public Ingredient setSomeOf(String ingredientString, String quantityString){
		Ingredient ingredient;
		Integer quantity;
		try{
			ingredient = Ingredient.valueOf(ingredientString.toUpperCase());
			quantity = Integer.decode(quantityString);
		}catch(IllegalArgumentException e){
			return null;
		}
		ingredients.put(ingredient, quantity);
		return ingredient;
	}

	public JsonObject getIngredientsAsJson() {
		Map<String, Object> map = new HashMap<String, Object>();
		for(Ingredient ingredient : ingredients.keySet()){
			map.put(ingredient.toString(), ingredients.get(ingredient));
		}
		return new JsonObject(map);
	}
	
}
