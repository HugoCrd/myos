package com.excilys.labs.myos;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.deploy.Verticle;

import com.excilys.labs.myos.model.Ingredient;
import com.excilys.labs.myos.model.Recipe;

public class Server extends Verticle {
	public void start() {

		final Recipe recipe = new Recipe();
		RouteMatcher rm = new RouteMatcher();

		rm.get("/set/:ingredient/:quantity", new Handler<HttpServerRequest>() {
			public void handle(final HttpServerRequest req) {
				if (recipe.setSomeOf(req.params().get("ingredient"), req.params().get("quantity")) == null) {
					req.response.statusCode = 400;
					req.response.statusMessage = "Can't find that ingredient";
					req.response.end();
				} else {
					req.response.end(recipe.getIngredientsAsJson().encode());							
				}
			}
		});
		
		rm.get("/recipe.json", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				req.response.end(recipe.getIngredientsAsJson().encode());
			}
		});

		rm.get("/availableIngredients.json", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				req.response.end((Ingredient.getAsJson().mergeIn(recipe.getIngredientsAsJson())).encode());
			}
		});

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

		vertx.createHttpServer().requestHandler(rm).listen(8080);
	}

}