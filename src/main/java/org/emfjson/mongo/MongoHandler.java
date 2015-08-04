package org.emfjson.mongo;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.URIHandlerImpl;
import org.emfjson.mongo.streams.MongoInputStream;
import org.emfjson.mongo.streams.MongoOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class MongoHandler extends URIHandlerImpl {

    @Override
    public boolean canHandle(URI uri) {
        return uri.scheme().equals("mongodb");
    }

    @Override
    public InputStream createInputStream(URI uri, Map<?, ?> options) throws IOException {
        return new MongoInputStream(uri, options);
    }

    @Override
    public OutputStream createOutputStream(URI uri, Map<?, ?> options) throws IOException {
        return new MongoOutputStream(uri, options);
    }

}
