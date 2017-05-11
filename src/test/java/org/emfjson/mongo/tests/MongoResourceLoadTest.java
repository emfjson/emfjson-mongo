package org.emfjson.mongo.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emfjson.model.ModelPackage;
import org.emfjson.model.TestA;
import org.emfjson.mongo.MongoHandler;
import org.emfjson.mongo.MongoResourceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.emfjson.mongo.MongoHandler.ID_FIELD;

public class MongoResourceLoadTest {

	private ResourceSet resourceSet;
	private MongoClient client;
	private URI testURI = URI.createURI("mongodb://localhost:27017/emfjson-test/models/model1");
	private MongoHandler handler;

	@Before
	public void setUp() throws JsonProcessingException {
		client = new MongoClient();

		handler = new MongoHandler(client);
		resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
		resourceSet.getPackageRegistry().put(ModelPackage.eNS_URI, ModelPackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry()
				.getExtensionToFactoryMap()
				.put("*", new MongoResourceFactory(handler));
	}

	@After
	public void tearDown() {
		client.getDatabase("emfjson-test").drop();
	}

	@Test
	public void testLoadOne() throws IOException {
		MongoCollection<Document> collection = handler.getCollection(testURI);

		Document a1 = new Document()
				.append("eClass", uriOf(ModelPackage.Literals.TEST_A))
				.append(ModelPackage.Literals.TEST_A__STRING_VALUE.getName(), "a1");

		Document a2 = new Document()
				.append("eClass", uriOf(ModelPackage.Literals.TEST_A))
				.append(ModelPackage.Literals.TEST_A__STRING_VALUE.getName(), "a2");

		collection.insertOne(a1);
		collection.insertOne(a2);

		Document resourceDoc = new Document()
				.append("eClass", "EResource")
				.append("uri", "mongodb://localhost:27017/emfjson-test/models/model1")
				.append("contents", Stream.of(a1.getObjectId(ID_FIELD), a2.getObjectId(ID_FIELD))
						.collect(Collectors.toList()));

		collection.insertOne(resourceDoc);

		Resource resource = resourceSet.createResource
				(URI.createURI("mongodb://localhost:27017/emfjson-test/models/model1"));
		resource.load(null);

		assertThat(resource.getContents()).hasSize(2);

		EObject first = resource.getContents().get(0);
		EObject second = resource.getContents().get(1);

		assertThat(first).isInstanceOf(TestA.class);
		assertThat(second).isInstanceOf(TestA.class);

		assertThat(((TestA) first).getStringValue())
				.isEqualTo("a1");

		assertThat(((TestA) second).getStringValue())
				.isEqualTo("a2");
	}

	private String uriOf(EClass type) {
		return EcoreUtil.getURI(type).toString();
	}

}
