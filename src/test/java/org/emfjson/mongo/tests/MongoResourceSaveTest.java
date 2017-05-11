package org.emfjson.mongo.tests;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.emfjson.model.ModelFactory;
import org.emfjson.model.TestA;
import org.emfjson.model.TestB;
import org.emfjson.mongo.MongoHandler;
import org.emfjson.mongo.MongoResourceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class MongoResourceSaveTest {

	private ResourceSet resourceSet;
	private MongoClient client;
	private MongoHandler handler;
	private URI testURI = URI.createURI("mongodb://localhost:27017/emfjson-test/models/model1");

	@Before
	public void setUp() {
		client = new MongoClient();
		handler = new MongoHandler(client);
		resourceSet = new ResourceSetImpl();

		resourceSet.getResourceFactoryRegistry()
				.getExtensionToFactoryMap().put("*", new MongoResourceFactory(new MongoHandler(client)));
	}

	@After
	public void tearDown() {
		client.getDatabase("emfjson-test").drop();
	}

	@Test
	public void testSave() throws IOException {
		Resource resource = resourceSet.createResource(testURI);

		TestA a1 = ModelFactory.eINSTANCE.createTestA();
		a1.setStringValue("a1");
		TestA a2 = ModelFactory.eINSTANCE.createTestA();
		a2.setStringValue("a2");

		TestB b1 = ModelFactory.eINSTANCE.createTestB();
		b1.setStringValue("b1");

		a1.getContainBs().add(b1);
		resource.getContents().add(a1);
		resource.getContents().add(a2);

		resource.save(null);

		MongoCollection<Document> collection = handler.getCollection(testURI);

		assertThat(collection).isNotNull();
		assertThat(collection.find()).hasSize(4);

		collection.find().forEach((Consumer<? super Document>) System.out::println);
	}

}
