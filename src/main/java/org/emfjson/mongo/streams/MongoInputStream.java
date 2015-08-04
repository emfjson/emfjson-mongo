package org.emfjson.mongo.streams;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter.Loadable;
import org.emfjson.jackson.JacksonOptions;
import org.emfjson.jackson.module.EMFModule;
import org.emfjson.mongo.MongoHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

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
        final MongoDatabase db = MongoHelper.getDB(client, uri);
        final MongoCollection<Document> collection = MongoHelper.getCollection(db, uri);

        if (!resource.getContents().isEmpty()) {
            resource.getContents().clear();
        }

        final String id = uri.segment(2);
        final BasicDBObject filter = new BasicDBObject("_id", id);
        final Document document = collection.find(filter).first();

        if (document == null) {
            throw new IOException("Cannot find document with _id " + id);
        }

        readJson(resource, document.toJson());
    }

    private void readJson(Resource resource, String data) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

		final JacksonOptions jacksonOptions = JacksonOptions.from(options);
		final EMFModule module = new EMFModule(resource.getResourceSet(), jacksonOptions);
        mapper.registerModule(module);

        final JsonNode rootNode = mapper.readTree(data);
        final JsonNode contents = rootNode.has("contents") ? rootNode.get("contents") : null;

        if (contents != null) {
            mapper.reader()
					.withAttribute("resource", resource)
					.treeToValue(contents, Resource.class);
        }
    }

    @Override
    public int read() throws IOException {
        return 0;
    }

}
