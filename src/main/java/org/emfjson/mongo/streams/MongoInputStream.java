package org.emfjson.mongo.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter.Loadable;
import org.emfjson.jackson.module.EMFModule;
import org.emfjson.mongo.MongoHelper;

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
		final ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new EMFModule(options));
		ObjectNode rootNode = null;
		try {
			rootNode = (ObjectNode) mapper.readTree(data);
		} catch (IOException e) {
			throw e;
		}

		final EObject result = mapper.readValue(rootNode.get("contents").traverse(), EObject.class);

		if (result != null) {
			resource.getContents().add(result);
		}
	}

	@Override
	public int read() throws IOException {
		return 0;
	}

}
