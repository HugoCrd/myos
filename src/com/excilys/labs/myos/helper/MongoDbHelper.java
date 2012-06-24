package com.excilys.labs.myos.helper;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class MongoDbHelper {

	/**
	 * Save json object into MongoDb, using sessionid as _id if existing
	 * 
	 * @param sessionid
	 * @param key
	 *            in the mongo document
	 * @param json
	 */
	public static void saveOrUpdateIntoSession(final String key, final JsonObject json, final Handler<Message<JsonObject>> callback, final String sessionid, final EventBus eventBus) {
		findFromSession(key, new Handler<Message<JsonObject>>() {
			
			public void handle(Message<JsonObject> msg) {

				JsonObject jsonKey = json;
				JsonObject jsonQuery = new JsonObject();
				
				if ("ok".equals(msg.body.getString("status")) && msg.body.getObject("result") != null){
					jsonKey = json.mergeIn(msg.body.getObject("result").getObject(key));
				}
				if(sessionid!=null && !sessionid.isEmpty())
					jsonQuery.putString("_id", sessionid);
				jsonQuery.putString("action", "save");
				jsonQuery.putString("collection", "session");
				jsonQuery.putObject("document", new JsonObject().putObject(key, jsonKey));

				eventBus.send("vertx.mongopersistor", jsonQuery, callback);
			}
		}, sessionid, eventBus); 
		
		
	}

	public static void findFromSession(final String key, final Handler<Message<JsonObject>> callback, final String sessionid, final EventBus eventBus) {
		JsonObject jsonQuery = new JsonObject();
		jsonQuery.putString("action", "findone");
		jsonQuery.putString("collection", "session");
		jsonQuery.putObject("matcher", new JsonObject().putString("_id", sessionid));
		eventBus.send("vertx.mongopersistor", jsonQuery, callback);
	}

}
