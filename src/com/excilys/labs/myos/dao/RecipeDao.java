package com.excilys.labs.myos.dao;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import com.excilys.labs.myos.helper.MongoDbHelper;
import com.excilys.labs.myos.model.Ingredient;

public class RecipeDao {

	private EventBus eventBus;

	public RecipeDao(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	public void setSomeOf(String ingredientString, String quantityString, final String sessionid, final HttpServerRequest req) {

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
		newIngredient.putString("ingredient", ingredient.toString());
		newIngredient.putNumber("quantity", quantity);

		MongoDbHelper.findFromSession("myos", new Handler<Message<JsonObject>>() {

			public void handle(Message<JsonObject> msg) {
				JsonObject response = msg.body;
				if ("ok".equals(response.getString("status"))) {
					final JsonObject ingredients = response.getObject("result").getObject("ingredients").mergeIn(newIngredient);

					MongoDbHelper.saveOrUpdateIntoSession("myos", ingredients, new Handler<Message<JsonObject>>() {
						public void handle(Message<JsonObject> arg0) {
							req.response.end(ingredients.encode());
						}
					}, sessionid, eventBus);
				}
			}
		}, sessionid, eventBus);
	}

	public void getIngredientsAsJson(final String sessionid, final HttpServerRequest req) {
		MongoDbHelper.findFromSession("myos", new Handler<Message<JsonObject>>() {

			public void handle(Message<JsonObject> msg) {
				JsonObject response = msg.body;

				if ("ok".equals(response.getString("status"))) {
					final JsonObject ingredients = response.getObject("result").getObject("ingredients");
					req.response.end(ingredients.encode());
				}
			}
			
		}, sessionid, eventBus);
	}
	
	public void getAvailableIngredientsAsJson(final String sessionid, final HttpServerRequest req){
		MongoDbHelper.findFromSession("myos", new Handler<Message<JsonObject>>() {

			public void handle(Message<JsonObject> msg) {
				JsonObject response = msg.body;

				if ("ok".equals(response.getString("status"))) {
					final JsonObject ingredients = response.getObject("result").getObject("ingredients");
					req.response.end(Ingredient.getAsJson().mergeIn(ingredients).encode());
				}
			}
			
		}, sessionid, eventBus);
	}

}
