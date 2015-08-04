package org.emfjson.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.eclipse.emf.common.util.URI;

public class MongoHelper {

    public static MongoClient getClient(URI uri) {
        String host = uri.host();
        String port = uri.port();

        return new MongoClient(host, Integer.decode(port));
    }

    public static MongoDatabase getDB(MongoClient client, URI uri) {
        String databaseName = uri.segment(0);
        if (databaseName == null)
            throw new IllegalArgumentException("Cannot find database name from uri " + uri);

        return client.getDatabase(databaseName);
    }

    public static MongoCollection<Document> getCollection(MongoDatabase db, URI uri) {
        final String collectionName = uri.segment(1);
        if (collectionName == null)
            throw new IllegalArgumentException("Cannot find collection name from uri " + uri);

        return db.getCollection(collectionName);
    }

}
