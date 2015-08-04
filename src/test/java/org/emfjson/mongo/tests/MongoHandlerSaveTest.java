package org.emfjson.mongo.tests;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.emfjson.jackson.resource.JsonResourceFactory;
import org.emfjson.mongo.MongoHandler;
import org.emfjson.mongo.MongoHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MongoHandlerSaveTest {

	private ResourceSet resourceSet;
	private MongoClient client;
	private URI testURI = URI.createURI("mongodb://localhost:27017/emfjson-test/models/model1");

	@Before
	public void setUp() {
		resourceSet = new ResourceSetImpl();

		resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new JsonResourceFactory());
		resourceSet.getURIConverter().getURIHandlers().add(0, new MongoHandler());

		client = MongoHelper.getClient(testURI);
	}

	@After
	public void tearDown() {
		MongoHelper
				.getDB(client, testURI)
				.drop();
	}

	@Test
	public void testSave() throws IOException {
		Resource resource = resourceSet.createResource(testURI);

		EPackage p = EcoreFactory.eINSTANCE.createEPackage();
		p.setName("p");
		EClass c = EcoreFactory.eINSTANCE.createEClass();
		c.setName("A");
		p.getEClassifiers().add(c);

		resource.getContents().add(p);
		resource.save(null);

		checkDocument(testURI);
	}

	@Test
	public void testSaveWithUriMapping() throws IOException {
		resourceSet.getURIConverter().getURIMap().put(
				URI.createURI("http://resources/"),
				URI.createURI("mongodb://localhost:27017/emfjson-test/models/"));

		Resource resource = resourceSet.createResource(URI.createURI("http://resources/model1"));

		EPackage p = EcoreFactory.eINSTANCE.createEPackage();
		p.setName("p");
		EClass c = EcoreFactory.eINSTANCE.createEClass();
		c.setName("A");
		p.getEClassifiers().add(c);

		resource.getContents().add(p);
		resource.save(null);

		checkDocument(testURI);
	}

	private void checkDocument(URI uri) {
		MongoDatabase db = MongoHelper.getDB(client, uri);
		MongoCollection<Document> collection = MongoHelper.getCollection(db, uri);

		assertNotNull(collection);

		Document document = collection.find(new BasicDBObject("_id", "model1")).first();

		assertNotNull(document);
		assertEquals("model1", document.get("_id"));
		assertEquals("resource", document.get("_type"));

		Document contents = (Document) document.get("contents");

		assertEquals(3, contents.keySet().size());
		assertEquals("http://www.eclipse.org/emf/2002/Ecore#//EPackage", contents.getString("eClass"));
		assertEquals("p", contents.getString("name"));

		assertTrue(contents.get("eClassifiers") instanceof List);

		List<Document> eClassifiers = (List<Document>) contents.get("eClassifiers");
		Document first = eClassifiers.get(0);
		assertEquals("http://www.eclipse.org/emf/2002/Ecore#//EClass", first.getString("eClass"));
		assertEquals("A", first.getString("name"));
	}

}
