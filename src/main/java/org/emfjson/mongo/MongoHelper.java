package org.emfjson.mongo;

import java.net.UnknownHostException;

import org.eclipse.emf.common.util.URI;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class MongoHelper {

	public static MongoClient getClient(URI uri) {
		String host = uri.host();
		String port = uri.port();

		try {
			return new MongoClient(host, Integer.decode(port));
		} catch (NumberFormatException | UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static DB getDB(MongoClient client, URI uri) {
		String databaseName = uri.segment(0);

		return client.getDB(databaseName);
	}

	public static DBCollection getCollection(DB db, URI uri) {
		String collectionName = uri.segment(1);

		return db.getCollection(collectionName);
	}

}
