package com.excilys.labs.helper;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class MongoDbHelper {

	private EventBus eventBus;

	public MongoDbHelper(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	/**
	 * Save json object into MongoDb, using sessionid as _id if existing
	 * 
	 * @param sessionid
	 * @param key
	 *            in the mongo document
	 * @param json
	 */
	public void saveOrUpdateIntoSession(final String sessionid, final String key, final JsonObject json, final Handler<Message<JsonObject>> callback) {
		findFromSession(sessionid, key, new Handler<Message<JsonObject>>() {
			
			public void handle(Message<JsonObject> msg) {

				JsonObject jsonKey = json;
				JsonObject jsonQuery = new JsonObject();
				
				if ("ok".equals(msg.body.getString("status")) && msg.body.getObject("result") != null){
					jsonKey = json.mergeIn(msg.body.getObject("result").getObject(key));
				}
				if(sessionid!=null)
					jsonQuery.putString("_id", sessionid);
				jsonQuery.putString("action", "save");
				jsonQuery.putString("collection", "session");
				jsonQuery.putObject("document", new JsonObject().putObject(key, jsonKey));

				eventBus.send("vertx.mongopersistor", jsonQuery, callback);
			}
		}); 
		
		
	}

	public void findFromSession(final String sessionid, final String key, final Handler<Message<JsonObject>> callback) {
		JsonObject jsonQuery = new JsonObject();
		jsonQuery.putString("action", "findone");
		jsonQuery.putString("collection", "session");
		jsonQuery.putObject("matcher", new JsonObject().putString("_id", sessionid));
		eventBus.send("vertx.mongopersistor", jsonQuery, callback);
	}

}
