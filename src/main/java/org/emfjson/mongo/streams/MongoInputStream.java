package org.emfjson.mongo.streams;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter.Loadable;
import org.emfjson.common.Options;
import org.emfjson.jackson.streaming.StreamReader;
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
		final DB db = MongoHelper.getDB(client, uri);
		final DBCollection collection = MongoHelper.getCollection(db, uri);

        if (!resource.getContents().isEmpty()) {
			resource.getContents().clear();
		}

        try (DBCursor cursor = collection.find(new BasicDBObject("_id", uri.segment(2)))) {
            while (cursor.hasNext()) {
                DBObject dbObject = cursor.next();
                String data = JSON.serialize(dbObject);

                readJson(resource, data);
            }
        }
	}

	private void readJson(Resource resource, String data) throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
        final JsonNode rootNode = mapper.readTree(data);
        final JsonNode contents = rootNode.has("contents") ? rootNode.get("contents") : null;

        if (contents != null) {
            StreamReader reader = new StreamReader(Options.from(options).build());
            reader.parse(resource, contents.traverse());
        }
	}

	@Override
	public int read() throws IOException {
		return 0;
	}

}
