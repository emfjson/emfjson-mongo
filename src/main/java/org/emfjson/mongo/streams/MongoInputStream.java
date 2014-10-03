package org.emfjson.mongo.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter.Loadable;
import org.emfjson.common.Options;
import org.emfjson.jackson.map.ObjectReader;
import org.emfjson.mongo.MongoHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class MongoInputStream extends InputStream implements Loadable {

	private final Map<?, ?> options;
	private final URI uri;

	public MongoInputStream(URI uri, Map<?, ?> options) {
		this.uri = uri;
		this.options = options;
	}

	@Override
	public void loadResource(Resource resource) throws IOException {
		final MongoClient client = MongoHelper.getClient(uri);
		final DB db = MongoHelper.getDB(client, uri);
		final DBCollection collection = MongoHelper.getCollection(db, uri);

		DBCursor cursor = collection.find(new BasicDBObject("_id", uri.segment(2)));

		if (!resource.getContents().isEmpty()) {
			resource.getContents().clear();
		}

		try {
			while (cursor.hasNext()) {
				DBObject dbObject = cursor.next();
				String data = JSON.serialize(dbObject);

				readJson(resource, data);
			}
		} finally {
			cursor.close();
		}
	}

	private void readJson(Resource resource, String data) throws IOException {
		ObjectMapper jmapper = new ObjectMapper();
		ObjectNode rootNode = null;
		try {
			rootNode = (ObjectNode) jmapper.readTree(data);
		} catch (IOException e) {
			throw e;
		}

		final JsonNode contents = rootNode.get("contents");
		final ObjectReader reader = new ObjectReader(resource, Options.from(options).build());
		final EObject result = reader.from(contents);

		if (result != null) {
			resource.getContents().add(result);
			reader.resolveEntries();
		}
	}

	@Override
	public int read() throws IOException {
		return 0;
	}

}
