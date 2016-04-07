package org.emfjson.mongo.streams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter.Saveable;
import org.emfjson.jackson.JacksonOptions;
import org.emfjson.jackson.module.EMFModule;
import org.emfjson.mongo.MongoHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class MongoOutputStream extends ByteArrayOutputStream implements Saveable {

	private final Map<?, ?> options;
	private final URI uri;
	private final MongoHandler handler;

	public MongoOutputStream(MongoHandler handler, URI uri, Map<?, ?> options) {
		this.handler = handler;
		this.uri = uri;
		this.options = options;
	}

	@Override
	public void saveResource(Resource resource) throws IOException {
		if (uri == null) {
			throw new IOException();
		}

		final MongoCollection<Document> collection = handler.getCollection(uri);
		final String data = toJson(resource);

		if (data == null) {
			throw new IOException("Error during saving");
		}

		final Document filter = new Document(MongoHandler.ID_FIELD, uri.segment(2));
		if (collection.find(filter).limit(1).first() == null) {
			collection.insertOne(Document.parse(data));
		} else {
			collection.findOneAndReplace(filter, Document.parse(data));
		}
	}

	private String toJson(Resource resource) throws JsonProcessingException {
		final ObjectMapper mapper = new ObjectMapper();
		final JacksonOptions jacksonOptions = JacksonOptions.from(options);
		mapper.registerModule(new EMFModule(resource.getResourceSet(), jacksonOptions));

		final JsonNode contents = mapper.valueToTree(resource);
		final ObjectNode resourceNode = mapper.createObjectNode();
		final String id = uri.segment(2);

		resourceNode.put(MongoHandler.ID_FIELD, id);
		resourceNode.put(MongoHandler.TYPE_FIELD, "resource");
		resourceNode.set(MongoHandler.CONTENTS_FIELD, contents);

		return mapper.writeValueAsString(resourceNode);
	}

}
