package org.emfjson.mongo.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

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
import org.junit.Before;
import org.junit.Test;

public class MongoHandlerTest {

	ResourceSet resourceSet;

	@Before
	public void setUp() {
		resourceSet = new ResourceSetImpl();

		resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new JsonResourceFactory());
		resourceSet.getURIConverter().getURIHandlers().add(0, new MongoHandler());
	}

	@Test
	public void testSave() throws IOException {
		Resource resource = resourceSet.createResource(
				URI.createURI("mongodb://localhost:27017/emfjson-test/models/model1"));

		EPackage p = EcoreFactory.eINSTANCE.createEPackage();
		p.setName("p");
		EClass c = EcoreFactory.eINSTANCE.createEClass();
		c.setName("A");
		p.getEClassifiers().add(c);

		resource.getContents().add(p);
		resource.save(null);
	}

//	@Test
	public void testLoad() throws IOException {
		Resource resource = resourceSet.createResource(
				URI.createURI("mongodb://localhost:27017/emfjson-test/models/model1"));

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
	}

//	@Test
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
