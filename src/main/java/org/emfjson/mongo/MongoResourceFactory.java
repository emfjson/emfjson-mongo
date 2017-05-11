package org.emfjson.mongo;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

public class MongoResourceFactory implements Resource.Factory {

	private final MongoHandler handler;

	public MongoResourceFactory(MongoHandler handler) {
		this.handler = handler;
	}

	@Override
	public Resource createResource(URI uri) {
		return new MongoResource(uri, handler);
	}
}
