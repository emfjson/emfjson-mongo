package org.emfjson.mongo.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.bson.Document;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emfjson.jackson.resource.JsonResourceFactory;
import org.emfjson.model.ModelPackage;
import org.emfjson.model.TestA;
import org.emfjson.mongo.MongoHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

public class MongoHandlerSaveAndLoadTest {

	private ResourceSet resourceSet;
	private MongoClient client;
	private MongoHandler handler;
	private URI testURI = URI.createURI("mongodb://localhost:27017/emfjson-test/models/model1");

	@Before
	public void setUp() {
		client = new MongoClient();
		handler = new MongoHandler(client);
		resourceSet = new ResourceSetImpl();

		resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new JsonResourceFactory());
		resourceSet.getURIConverter().getURIHandlers().add(0, handler);
	}

	@After
	public void tearDown() {
		client.getDatabase("emfjson-test").drop();
	}

	@Test
	public void testSaveThenLoadNode() throws IOException {
		MongoCollection<Document> models = handler.getCollection(testURI);
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode content_model1 = mapper.createObjectNode()
				.put("_id", "model1")
				.put("_type", "resource")
				.set("contents", mapper.createObjectNode()
						.put("eClass", EcoreUtil.getURI(ModelPackage.Literals.TEST_A).toString())
						.put("stringValue", "testStringValue")
						.put("intValue", 1234)
						.put("booleanValue", true)
						.put("longValue", 12345678900L));
		
		
		models.insertOne(Document.parse(mapper.writeValueAsString(content_model1)));
		
		Resource resource = resourceSet.createResource(testURI);
		resource.load(null);

		assertEquals(1, resource.getContents().size());
		assertEquals(ModelPackage.Literals.TEST_A, resource.getContents().get(0).eClass());

		TestA ta = (TestA) resource.getContents().get(0);

		assertEquals("testStringValue", ta.getStringValue());
		assertEquals(1234, ta.getIntValue().intValue());
		assertEquals(true, ta.getBooleanValue().booleanValue());
		assertEquals(12345678900L, ta.getLongValue().longValue());
	}

}
