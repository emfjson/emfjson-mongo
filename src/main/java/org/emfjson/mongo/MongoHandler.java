package org.emfjson.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.URIHandlerImpl;
import org.emfjson.mongo.streams.MongoInputStream;
import org.emfjson.mongo.streams.MongoOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class MongoHandler extends URIHandlerImpl {

	private final MongoClient client;

	public MongoHandler(MongoClient client) {
		this.client = client;
	}

	@Override
	public boolean canHandle(URI uri) {
		return uri.scheme().equals("mongodb");
	}

	@Override
	public InputStream createInputStream(URI uri, Map<?, ?> options) throws IOException {
		return new MongoInputStream(this, uri, options);
	}

	@Override
	public OutputStream createOutputStream(URI uri, Map<?, ?> options) throws IOException {
		return new MongoOutputStream(this, uri, options);
	}

	@Override
	public void delete(URI uri, Map<?, ?> options) throws IOException {
		final MongoCollection<Document> collection = getCollection(uri);
		final Map<String, Object> filter = new HashMap<>();
		filter.put("_id", uri.segment(2));
		filter.put("type", "resource");

		collection.findOneAndDelete(new Document(filter));
	}

	@Override
	public boolean exists(URI uri, Map<?, ?> options) {
		final MongoCollection<Document> collection = getCollection(uri);
		final Map<String, Object> filter = new HashMap<>();
		filter.put("_id", uri.segment(2));
		filter.put("type", "resource");

		return collection.find(new Document(filter)).limit(1).first() != null;
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
