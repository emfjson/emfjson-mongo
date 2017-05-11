package org.emfjson.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.eclipse.emf.common.util.URI;

public class MongoHandler {

	public static final String ID_FIELD = "_id";
	public static final String TYPE_FIELD = "_type";
	public static final String CONTENTS_FIELD = "contents";

	private final MongoClient client;

	public MongoHandler(MongoClient client) {
		this.client = client;
	}

	protected String db(URI uri) {
		String databaseName = uri.segment(0);
		if (databaseName == null)
			throw new IllegalArgumentException("Cannot find database name from uri " + uri);

		return databaseName;
	}

	protected String collection(URI uri) {
		final String collectionName = uri.segment(1);
		if (collectionName == null)
			throw new IllegalArgumentException("Cannot find collection name from uri " + uri);

		return collectionName;
	}

	public MongoCollection<Document> getCollection(URI uri) {
		if (uri.segmentCount() < 3)
			throw new IllegalArgumentException();

		return client.getDatabase(uri.segment(0)).getCollection(uri.segment(1));
	}
}
