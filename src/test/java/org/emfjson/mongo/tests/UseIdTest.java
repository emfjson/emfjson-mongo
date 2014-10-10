package org.emfjson.mongo.tests;

import static org.emfjson.mongo.MongoHelper.getClient;
import static org.emfjson.mongo.MongoHelper.getCollection;
import static org.emfjson.mongo.MongoHelper.getDB;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class UseIdTest {

	ResourceSet resourceSet;

	@Before
	public void setUp() {
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
		resourceSet.getURIConverter().getURIHandlers().add(0, new MongoHandler());
	}
	
	private Map<?, ?> getOptions() {
		Map<String, Object> options = new HashMap<>();
		options.put(EMFJs.OPTION_USE_UUID, true);		
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
		
		MongoClient client = getClient(resource.getURI());
		DBCollection collection = getCollection(getDB(client, resource.getURI()), resource.getURI());


		DBObject pObject = collection.findOne(new BasicDBObject("_id", "model-test-uuid-1"));

		assertNotNull(pObject);
		
		DBObject contents = (DBObject) pObject.get("contents");
		assertNotNull(contents);
		assertTrue(contents.containsField("_id"));
		assertEquals(pId, contents.get("_id"));

		BasicDBList eClassifiers = (BasicDBList) contents.get("eClassifiers");
		assertEquals(1, eClassifiers.size());
		
		DBObject cObject = (DBObject) eClassifiers.get(0);
		assertTrue(cObject.containsField("_id"));
		assertEquals(cId, cObject.get("_id"));
	}

}

