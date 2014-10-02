package org.emfjson.mongo.streams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter.Saveable;
import org.emfjson.common.Options;
import org.emfjson.jackson.map.ObjectWriter;
import org.emfjson.mongo.MongoHelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class MongoOutputStream extends ByteArrayOutputStream implements Saveable {

	private final Map<?, ?> options;
	private final URI uri;

	public MongoOutputStream(URI uri, Map<?, ?> options) {
		this.uri = uri;
		this.options = options;
	}

	@Override
	public void saveResource(Resource resource) throws IOException {
		if (uri == null) {
			throw new IOException();
		}

		final MongoClient client = MongoHelper.getClient(uri);
		final DB db = MongoHelper.getDB(client, uri);
		final DBCollection collection = MongoHelper.getCollection(db, uri);

		String data = null;
		try {
			data = toJson(resource);
		} catch (JsonProcessingException e) {
			throw e;
		}

		if (data != null) {
			DBObject dbObject = (DBObject) JSON.parse(data);
			collection.save(dbObject);
		}
	}

	private String toJson(Resource resource) throws JsonProcessingException {
		ObjectMapper jmapper = new ObjectMapper();
		ObjectWriter writer = new ObjectWriter(jmapper, resource, Options.from(options).build());
		JsonNode contents = writer.toNode();

		ObjectNode resourceNode = jmapper.createObjectNode();

		final String id = uri.segment(2);
		resourceNode.put("_id", id);
		resourceNode.set("contents", contents);
		
		return jmapper.writeValueAsString(resourceNode);
	}

}
