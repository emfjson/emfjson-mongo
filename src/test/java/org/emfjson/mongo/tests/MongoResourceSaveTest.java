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
	public void testSaveOneContainment() throws IOException {
		Resource resource = resourceSet.createResource(testURI);

		TestA a1 = ModelFactory.eINSTANCE.createTestA();
		a1.setStringValue("a1");
		TestA a2 = ModelFactory.eINSTANCE.createTestA();
		a2.setStringValue("a2");

		TestB b1 = ModelFactory.eINSTANCE.createTestB();
		b1.setStringValue("b1");

		a1.setContainB(b1);
		resource.getContents().add(a1);
		resource.getContents().add(a2);

		resource.save(null);

		MongoCollection<Document> collection = handler.getCollection(testURI);

		assertThat(collection).isNotNull();
		assertThat(collection.find()).hasSize(4);

		collection.find().forEach((Consumer<? super Document>) System.out::println);
	}

	@Test
	public void testSaveManyContainments() throws IOException {
		Resource resource = resourceSet.createResource(testURI);

		TestA a1 = ModelFactory.eINSTANCE.createTestA();
		a1.setStringValue("a1");
		TestA a2 = ModelFactory.eINSTANCE.createTestA();
		a2.setStringValue("a2");

		TestB b1 = ModelFactory.eINSTANCE.createTestB();
		b1.setStringValue("b1");
		TestB b2 = ModelFactory.eINSTANCE.createTestB();
		b2.setStringValue("b2");
		TestB b3 = ModelFactory.eINSTANCE.createTestB();
		b3.setStringValue("b3");
		TestB b4 = ModelFactory.eINSTANCE.createTestB();
		b4.setStringValue("b4");

		a1.getContainBs().add(b1);
		a1.getContainBs().add(b2);
		a1.getContainBs().add(b3);
		a1.getContainBs().add(b4);

		resource.getContents().add(a1);
		resource.getContents().add(a2);

		resource.save(null);

		MongoCollection<Document> collection = handler.getCollection(testURI);

		assertThat(collection).isNotNull();
		assertThat(collection.find()).hasSize(7);

		collection.find().forEach((Consumer<? super Document>) System.out::println);
	}

	@Test
	public void testSaveOneReference() throws IOException {
		Resource resource = resourceSet.createResource(testURI);

		TestA a1 = ModelFactory.eINSTANCE.createTestA();
		a1.setStringValue("a1");
		TestB b1 = ModelFactory.eINSTANCE.createTestB();
		b1.setStringValue("b1");

		a1.setOneB(b1);

		resource.getContents().add(a1);
		resource.getContents().add(b1);

		resource.save(null);

		MongoCollection<Document> collection = handler.getCollection(testURI);

		assertThat(collection).isNotNull();
		assertThat(collection.find()).hasSize(3);

		collection.find().forEach((Consumer<? super Document>) System.out::println);
	}

	@Test
	public void testSaveManyReferences() throws IOException {
		Resource resource = resourceSet.createResource(testURI);

		TestA a1 = ModelFactory.eINSTANCE.createTestA();
		a1.setStringValue("a1");

		TestB b1 = ModelFactory.eINSTANCE.createTestB();
		b1.setStringValue("b1");
		TestB b2 = ModelFactory.eINSTANCE.createTestB();
		b2.setStringValue("b2");
		TestB b3 = ModelFactory.eINSTANCE.createTestB();
		b3.setStringValue("b3");
		TestB b4 = ModelFactory.eINSTANCE.createTestB();
		b4.setStringValue("b4");

		a1.getManyBs().add(b1);
		a1.getManyBs().add(b2);
		a1.getManyBs().add(b3);
		a1.getManyBs().add(b4);

		resource.getContents().add(a1);
		resource.getContents().add(b1);
		resource.getContents().add(b2);
		resource.getContents().add(b3);
		resource.getContents().add(b4);

		resource.save(null);

		MongoCollection<Document> collection = handler.getCollection(testURI);

		assertThat(collection).isNotNull();
		assertThat(collection.find()).hasSize(6);

		collection.find().forEach((Consumer<? super Document>) System.out::println);
	}

}
