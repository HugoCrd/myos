package com.excilys.labs.myos;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.deploy.Verticle;

import com.excilys.labs.myos.dao.RecipeDao;
import com.excilys.labs.myos.helper.CookieHelper;

public class Server extends Verticle {
	
	public void start() {
		
		JsonObject mongoConf = new JsonObject();
		mongoConf.putString("address", "vertx.mongopersistor");
		mongoConf.putString("host", "127.0.0.1");
		mongoConf.putNumber("port", 27017);
		mongoConf.putString("db_name", "test");

		this.container.deployWorkerVerticle("mongo-persistor", mongoConf);

		final EventBus eventBus = this.getVertx().eventBus();
		final RouteMatcher rm = new RouteMatcher();

		rm.get("/set/:ingredient/:quantity", new Handler<HttpServerRequest>() {
			public void handle(final HttpServerRequest req) {
				String sessionid = CookieHelper.getSessionid(req.headers());
				RecipeDao.setSomeOf(req.params().get("ingredient"), req.params().get("quantity"), sessionid, req, eventBus);
			}
		});

		rm.get("/recipe.json", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				String sessionid = CookieHelper.getSessionid(req.headers());
				RecipeDao.getIngredientsAsJson(sessionid, req, eventBus);
			}
		});

		rm.get("/availableIngredients.json", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				String sessionid = CookieHelper.getSessionid(req.headers());
				RecipeDao.getAvailableIngredientsAsJson(sessionid, req, eventBus);
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
