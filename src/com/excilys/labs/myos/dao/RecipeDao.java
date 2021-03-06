package com.excilys.labs.myos.dao;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import com.excilys.labs.myos.helper.CookieHelper;
import com.excilys.labs.myos.helper.MongoDbHelper;
import com.excilys.labs.myos.model.Ingredient;

public class RecipeDao {

	public static void setSomeOf(String ingredientString, String quantityString, final String sessionid, final HttpServerRequest req, final EventBus eventBus) {

		Ingredient ingredient;
		Integer quantity;

		try {
			ingredient = Ingredient.valueOf(ingredientString.toUpperCase());
			quantity = Integer.decode(quantityString);
		} catch (IllegalArgumentException e) {
			req.response.statusCode = 400;
			req.response.statusMessage = "Can't find that ingredient";
			req.response.end();
			return;
		}

		final JsonObject newIngredient = new JsonObject();
		newIngredient.putNumber(ingredient.toString(), quantity);

		MongoDbHelper.findFromSession("myos", new Handler<Message<JsonObject>>() {

			public void handle(Message<JsonObject> msg) {
				JsonObject response = msg.body;
				if ("ok".equals(response.getString("status"))) {
					final JsonObject ingredients;
					if(response.getObject("result")!=null){
						ingredients = response.getObject("result").getObject("myos").mergeIn(newIngredient);
					}else{
						ingredients = newIngredient;
					}
						
					MongoDbHelper.saveOrUpdateIntoSession("myos", ingredients, new Handler<Message<JsonObject>>() {
						public void handle(Message<JsonObject> msg) {
							JsonObject response = msg.body;
							if(response.getString("_id")!=null)
								CookieHelper.setSessionid(response.getString("_id"), req);
								req.response.end(ingredients.encode());
						}
					}, sessionid, eventBus);
				}
			}
		}, sessionid, eventBus);
	}

	public static void getIngredientsAsJson(final String sessionid, final HttpServerRequest req, final EventBus eventBus) {
		if(sessionid==null){
			req.response.end("{}");
			return;
		}
		MongoDbHelper.findFromSession("myos", new Handler<Message<JsonObject>>() {

			public void handle(Message<JsonObject> msg) {
				JsonObject response = msg.body;

				if ("ok".equals(response.getString("status"))) {
					final JsonObject ingredients = response.getObject("result").getObject("myos");
					req.response.end(ingredients.encode());
				}
			}
			
		}, sessionid, eventBus);
	}
	
	public static void getAvailableIngredientsAsJson(final String sessionid, final HttpServerRequest req, final EventBus eventBus){
		if(sessionid==null){
			req.response.end(Ingredient.getAsJson().encode());
			return;
		}
		MongoDbHelper.findFromSession("myos", new Handler<Message<JsonObject>>() {

			public void handle(Message<JsonObject> msg) {
				JsonObject response = msg.body;

				if ("ok".equals(response.getString("status"))) {
					final JsonObject ingredients = response.getObject("result").getObject("myos");
					req.response.end(Ingredient.getAsJson().mergeIn(ingredients).encode());
				}
			}
			
		}, sessionid, eventBus);
	}

}
