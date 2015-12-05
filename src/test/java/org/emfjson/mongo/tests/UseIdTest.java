package org.emfjson.mongo.tests;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emfjson.EMFJs;
import org.emfjson.jackson.resource.JsonResource;
import org.emfjson.jackson.resource.JsonResourceFactory;
import org.emfjson.mongo.MongoHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class UseIdTest {

	private ResourceSet resourceSet;
	private MongoHandler handler;
	private MongoClient client;

	@Before
	public void setUp() {
		client = new MongoClient();
		handler = new MongoHandler(client);
		resourceSet = new ResourceSetImpl();

		resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new JsonResourceFactory() {
			@Override
			public Resource createResource(URI uri) {
				return new JsonResource(uri) {
					@Override
					protected boolean useUUIDs() {
						return true;
					}
				};
			}
		});
		resourceSet.getURIConverter().getURIHandlers().add(0, handler);
	}

	@After
	public void tearDown() {
		client.getDatabase("emfjson-test").drop();
	}

	private Map<?, ?> getOptions() {
		Map<String, Object> options = new HashMap<>();
		options.put(EMFJs.OPTION_USE_ID, true);
		return options;
	}

	@Test
	public void testSave() throws IOException {
		Resource resource = resourceSet.createResource(
				URI.createURI("mongodb://localhost:27017/emfjson-test/models/model-test-uuid-1"));

		EPackage p = EcoreFactory.eINSTANCE.createEPackage();
		p.setName("p");
		EClass c = EcoreFactory.eINSTANCE.createEClass();
		c.setName("A");
		p.getEClassifiers().add(c);

		resource.getContents().add(p);

		String pId = EcoreUtil.getURI(p).fragment();
		String cId = EcoreUtil.getURI(c).fragment();

		resource.save(getOptions());

		MongoCollection<Document> collection = handler.getCollection(resource.getURI());
		FindIterable<Document> objects = collection.find(new Document("_id", "model-test-uuid-1"));

		assertNotNull(objects);

		Document document = objects.iterator().next();

		Document contents = (Document) document.get("contents");
		assertNotNull(contents);

		assertTrue(contents.containsKey("_id"));
		assertEquals(pId, contents.get("_id"));

		List eClassifiers = (List) contents.get("eClassifiers");
		assertEquals(1, eClassifiers.size());

		Document cObject = (Document) eClassifiers.get(0);
		assertTrue(cObject.containsKey("_id"));
		assertEquals(cId, cObject.get("_id"));
	}

}

