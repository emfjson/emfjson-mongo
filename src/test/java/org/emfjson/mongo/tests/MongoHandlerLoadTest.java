package org.emfjson.mongo.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
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

import static org.junit.Assert.assertEquals;

public class MongoHandlerLoadTest {

    private ResourceSet resourceSet;
	private MongoClient client;
	private URI testURI = URI.createURI("mongodb://localhost:27017/emfjson-test/models/model1");

	@Before
    public void setUp() throws JsonProcessingException {
        resourceSet = new ResourceSetImpl();

        resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new JsonResourceFactory());
        resourceSet.getURIConverter().getURIHandlers().add(0, new MongoHandler());

		client = MongoHelper.getClient(testURI);

		MongoCollection<Document> collection = MongoHelper.getCollection(MongoHelper.getDB(client, testURI), testURI);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode content = mapper.createObjectNode()
				.put("_id", "model1")
				.put("_type", "resource")
				.set("contents", mapper.createObjectNode()
								.put("eClass", "http://www.eclipse.org/emf/2002/Ecore#//EPackage")
								.put("name", "p")
								.set("eClassifiers", mapper.createArrayNode()
										.add(mapper.createObjectNode()
												.put("eClass", "http://www.eclipse.org/emf/2002/Ecore#//EClass")
												.put("name", "A"))));

		collection.deleteOne(new BasicDBObject("_id", "model1"));
		collection.insertOne(Document.parse(mapper.writeValueAsString(content)));
	}

	@After
	public void tearDown() {
		MongoHelper
				.getDB(client, testURI)
				.drop();
	}

    @Test
    public void testLoad() throws IOException {
        Resource resource = resourceSet.createResource(testURI);
        resource.load(null);

        assertEquals(1, resource.getContents().size());
        assertEquals(EcorePackage.Literals.EPACKAGE, resource.getContents().get(0).eClass());

        EPackage p = (EPackage) resource.getContents().get(0);

        assertEquals("p", p.getName());
        assertEquals(1, p.getEClassifiers().size());
        assertEquals(EcorePackage.Literals.ECLASS, p.getEClassifiers().get(0).eClass());

        EClass c = (EClass) p.getEClassifiers().get(0);
        assertEquals("A", c.getName());
    }

    @Test
    public void testLoadWithUriMapping() throws IOException {
        resourceSet.getURIConverter().getURIMap().put(
                URI.createURI("http://resources/"),
                URI.createURI("mongodb://localhost:27017/emfjson-test/models/"));

        Resource resource = resourceSet.createResource(URI.createURI("http://resources/model1"));
        resource.load(null);

        assertEquals(1, resource.getContents().size());
        assertEquals(EcorePackage.Literals.EPACKAGE, resource.getContents().get(0).eClass());

        EPackage p = (EPackage) resource.getContents().get(0);

        assertEquals("p", p.getName());
        assertEquals(1, p.getEClassifiers().size());
        assertEquals(EcorePackage.Literals.ECLASS, p.getEClassifiers().get(0).eClass());

        EClass c = (EClass) p.getEClassifiers().get(0);
        assertEquals("A", c.getName());
    }

}
