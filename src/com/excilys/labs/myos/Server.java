package com.excilys.labs.myos;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;

import com.excilys.labs.myos.helper.MongoDbHelper;
import com.excilys.labs.myos.model.Ingredient;

public class Server extends BusModBase implements Handler<Message<JsonObject>> {
	private String address;

	public void start() {

		JsonObject mongoConf = new JsonObject();
		mongoConf.putString("address", "vertx.mongopersistor");
		mongoConf.putString("host", "127.0.0.1");
		mongoConf.putNumber("port", 27017);
		mongoConf.putString("db_name", "test");

		this.container.deployWorkerVerticle("mongo-persistor", mongoConf);

		final RouteMatcher rm = new RouteMatcher();

		address = getOptionalStringConfig("address", "vertx.excilys.myos");

		eb.registerHandler(address, this);

		rm.get(".+.(css|js|png)", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				req.response.sendFile("resources" + req.path);
			}
		});

		rm.getWithRegEx(".*", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				req.response.sendFile("resources/pages/index.html");
			}
		});

		HttpServer server = vertx.createHttpServer().requestHandler(rm);
		JsonArray permitted = new JsonArray();
		permitted.add(new JsonObject()); // Let everything through
		SockJSServer sockJSServer = vertx.createSockJSServer(server);
		sockJSServer.bridge(new JsonObject().putString("prefix", "/eventbus"), permitted);

		server.listen(8080);
	}

	public void handle(Message<JsonObject> msg) {
		String action = msg.body.getString("action");
		if (action == null) {
			sendError(msg, "action is null");
			return;
		}
		
		String sessionid = msg.body.getString("sessionid");
		
		switch (action) {
		case "availableIngredients":
			getAvailableIngredientsAsJson(sessionid, msg, eb);
			break;
		case "recipe":
			getIngredientsAsJson(sessionid, msg, eb);
			break;
		case "save":
			setSomeOf(msg.body.getString("ingredient"), msg.body.getString("quantity"), sessionid, msg, eb);
			break;
		default: 
			sendError(msg, "Wrong action");
			break;
		}
	}
	
	private void setSomeOf(String ingredientString, String quantityString, final String sessionid, final Message<JsonObject> message, final EventBus eventBus) {

		Ingredient ingredient;
		Integer quantity;

		try {
			ingredient = Ingredient.valueOf(ingredientString.toUpperCase());
			quantity = Integer.decode(quantityString);
		} catch (IllegalArgumentException e) {
			sendError(message,  "Can't find that ingredient");
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
							JsonObject value = new JsonObject().putObject("value", ingredients);
							if(response.getString("_id")!=null)
								value.putString("sessionid", response.getString("_id"));
							sendOK(message, value);
						}
					}, sessionid, eventBus);
				}
			}
		}, sessionid, eventBus);
	}

	private void getIngredientsAsJson(final String sessionid, final Message<JsonObject> message,  final EventBus eventBus) {
		if(sessionid==null){
			JsonObject value = new JsonObject().putString("value", "{}");
			sendOK(message, value);
			return;
		}
		MongoDbHelper.findFromSession("myos", new Handler<Message<JsonObject>>() {

			public void handle(Message<JsonObject> msg) {
				JsonObject response = msg.body;

				if ("ok".equals(response.getString("status"))) {
					final JsonObject ingredients = response.getObject("result").getObject("myos");
					JsonObject value = new JsonObject().putObject("value", ingredients);
					sendOK(message, value);
				}
			}
			
		}, sessionid, eventBus);
	}
	
	private void getAvailableIngredientsAsJson(final String sessionid, final Message<JsonObject> message, final EventBus eventBus){
		if(sessionid==null){
			JsonObject value = new JsonObject().putObject("value", Ingredient.getAsJson());
			sendOK(message, value);
			return;
		}
		MongoDbHelper.findFromSession("myos", new Handler<Message<JsonObject>>() {

			public void handle(Message<JsonObject> msg) {
				JsonObject response = msg.body;

				if ("ok".equals(response.getString("status"))) {
					final JsonObject ingredients = response.getObject("result").getObject("myos");
					JsonObject value = new JsonObject().putObject("value", Ingredient.getAsJson().mergeIn(ingredients));
					sendOK(message, value);
				}
			}
			
		}, sessionid, eventBus);
		
	}

}
