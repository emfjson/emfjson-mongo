package org.emfjson.mongo.streams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter.Saveable;
import org.emfjson.jackson.JacksonOptions;
import org.emfjson.jackson.module.EMFModule;
import org.emfjson.mongo.MongoHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

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
        final MongoDatabase db = MongoHelper.getDB(client, uri);
        final MongoCollection<Document> collection = MongoHelper.getCollection(db, uri);
        final String data = toJson(resource);

        if (data == null) {
            throw new IOException("Error during saving");
        }

        final BasicDBObject filter = new BasicDBObject("_id", uri.segment(2));
        collection.findOneAndDelete(filter);
        collection.insertOne(Document.parse(data));
    }

    private String toJson(Resource resource) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
		final JacksonOptions jacksonOptions = JacksonOptions.from(options);
		mapper.registerModule(new EMFModule(resource.getResourceSet(), jacksonOptions));

        final JsonNode contents = mapper.valueToTree(resource);
        final ObjectNode resourceNode = mapper.createObjectNode();
        final String id = uri.segment(2);

        resourceNode.put("_id", id);
        resourceNode.put("_type", "resource");
        resourceNode.set("contents", contents);

        return mapper.writeValueAsString(resourceNode);
    }

}
