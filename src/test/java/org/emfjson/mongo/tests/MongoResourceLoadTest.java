package org.emfjson.mongo.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.MongoClient;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.emfjson.model.ModelPackage;
import org.emfjson.model.TestA;
import org.emfjson.model.TestB;
import org.emfjson.mongo.MongoHandler;
import org.emfjson.mongo.MongoResourceFactory;
import org.emfjson.mongo.fixtures.LoadFixtures;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

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
	public void testLoadRootElements() throws IOException {
		LoadFixtures.fixtureRootElements(handler, testURI);

		Resource resource = resourceSet.getResource(testURI, true);

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

	@Test
	public void testLoadRootElementsWithSingleReference() throws IOException {
		LoadFixtures.fixtureTest_SingleReference(handler, testURI);

		Resource resource = resourceSet.getResource(testURI, true);

		assertThat(resource.getContents()).hasSize(2);

		TestA first = (TestA) resource.getContents().get(0);
		TestB second = (TestB) resource.getContents().get(1);

		assertThat(first.getStringValue())
				.isEqualTo("a1");

		assertThat(second.getStringValue())
				.isEqualTo("b1");

		assertThat(first.getOneB()).isSameAs(second);
	}

	@Test
	public void testLoadRootElementsWithManyReferences() throws IOException {
		LoadFixtures.fixtureTest_ManyReferences(handler, testURI);

		Resource resource = resourceSet.getResource(testURI, true);

		assertThat(resource.getContents()).hasSize(4);

		TestA first = (TestA) resource.getContents().get(0);
		TestB b1 = (TestB) resource.getContents().get(1);
		TestB b2 = (TestB) resource.getContents().get(2);
		TestB b3 = (TestB) resource.getContents().get(3);

		assertThat(first.getStringValue())
				.isEqualTo("a1");

		assertThat(b1.getStringValue())
				.isEqualTo("b1");
		assertThat(b2.getStringValue())
				.isEqualTo("b2");
		assertThat(b3.getStringValue())
				.isEqualTo("b3");

		assertThat(first.getManyBs()).containsExactly(b1, b2, b3);
	}

	@Test
	public void testLoadRootElementWithSingleContainment() throws IOException {
		LoadFixtures.singleContainment(handler, testURI);

		Resource resource = resourceSet.getResource(testURI, true);

		assertThat(resource.getContents()).hasSize(1);

		TestA first = (TestA) resource.getContents().get(0);

		assertThat(first.getStringValue())
				.isEqualTo("a1");

		assertThat(first.getContainB())
				.isNotNull();
	}

	@Test
	public void testLoadRootElementWithSingleAndManyContainments() throws IOException {
		LoadFixtures.singleAndManyContainments(handler, testURI);

		Resource resource = resourceSet.getResource(testURI, true);

		assertThat(resource.getContents()).hasSize(1);

		TestA first = (TestA) resource.getContents().get(0);

		assertThat(first.getStringValue())
				.isEqualTo("a1");

		assertThat(first.getContainB())
				.isNotNull();

		assertThat(first.getContainBs())
				.hasSize(3);
	}

}
