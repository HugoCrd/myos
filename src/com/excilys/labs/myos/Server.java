package com.excilys.labs.myos;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.deploy.Verticle;

import com.excilys.labs.myos.dao.RecipeDao;
import com.excilys.labs.myos.helper.CookieHelper;

public class Server extends Verticle {
	
	public void start() {

		final RecipeDao recipeDao = new RecipeDao(this.getVertx().eventBus());
		final RouteMatcher rm = new RouteMatcher();

		rm.get("/set/:ingredient/:quantity", new Handler<HttpServerRequest>() {
			public void handle(final HttpServerRequest req) {
				String sessionid = CookieHelper.getSessionid(req.headers());
				recipeDao.setSomeOf(req.params().get("ingredient"), req.params().get("quantity"), sessionid, req);
			}
		});

		rm.get("/recipe.json", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				String sessionid = CookieHelper.getSessionid(req.headers());
				recipeDao.getIngredientsAsJson(sessionid, req);
			}
		});

		rm.get("/availableIngredients.json", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				String sessionid = CookieHelper.getSessionid(req.headers());
				recipeDao.getIngredientsAsJson(sessionid, req);
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
