package org.emfjson.mongo.tests;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emfjson.EMFJs;
import org.emfjson.jackson.resource.JsonResourceFactory;
import org.emfjson.mongo.MongoHandler;
import org.emfjson.mongo.MongoHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DynamicTest {

    private ResourceSetImpl resourceSet;
	private MongoClient client;
	private MongoDatabase db;

	private Map<String, Object> options = new HashMap<>();
	{
        options.put(EMFJs.OPTION_REF_FIELD, "_ref");
    }

	private URI testURI1 = URI.createURI("mongodb://localhost:27017/emfjson-test/models/foo");
	private URI testURI2 = URI.createURI("mongodb://localhost:27017/emfjson-test/models/foo-model");

    @Before
    public void setUp() throws IOException {
        resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new JsonResourceFactory());
        resourceSet.getURIConverter().getURIHandlers().add(0, new MongoHandler());
		resourceSet.getLoadOptions().putAll(options);

        resourceSet.getURIConverter().getURIMap().put(
				URI.createURI("http://test/foo"), testURI1);

        resourceSet.getURIConverter().getURIMap().put(
				URI.createURI("http://test/foo-model"), testURI2);

		client = MongoHelper.getClient(testURI1);
		db = MongoHelper.getDB(client, testURI1);
		createModel(createPackage(resourceSet), resourceSet);
        resourceSet.getResources().clear();
    }

	@After
	public void tearDown() {
		db.drop();
	}

    @Test
    public void testLoadModel() throws IOException {
        Resource resource = resourceSet.createResource(URI.createURI("http://test/foo-model"));
        assertNotNull(resource);
        assertTrue(resource.getContents().isEmpty());

        resource.load(options);

        assertEquals(1, resource.getContents().size());

        EObject root = resource.getContents().get(0);

        assertEquals("A", root.eClass().getName());
    }

    private EPackage createPackage(ResourceSet resourceSet) throws IOException {
        EPackage foo = EcoreFactory.eINSTANCE.createEPackage();
        foo.setName("foo");
        foo.setNsPrefix("foo");
        foo.setNsURI("http://test/foo");

        EClass a = EcoreFactory.eINSTANCE.createEClass();
        a.setName("A");

        EClass b = EcoreFactory.eINSTANCE.createEClass();
        b.setName("B");
        b.getESuperTypes().add(a);

        EAttribute label = EcoreFactory.eINSTANCE.createEAttribute();
        label.setName("label");
        label.setEType(EcorePackage.Literals.ESTRING);

        EReference bs = EcoreFactory.eINSTANCE.createEReference();
        bs.setName("bs");
        bs.setEType(b);
        bs.setUpperBound(-1);
        bs.setContainment(true);

        a.getEStructuralFeatures().add(label);
        a.getEStructuralFeatures().add(bs);

        foo.getEClassifiers().add(a);
        foo.getEClassifiers().add(b);

        Resource resource = resourceSet.createResource(URI.createURI("http://test/foo"));

        resource.getContents().add(foo);
        resource.save(options);

        return foo;
    }

    @SuppressWarnings("unchecked")
    private void createModel(EPackage p, ResourceSet resourceSet) throws IOException {
        EClass a = (EClass) p.getEClassifier("A");
        EClass b = (EClass) p.getEClassifier("B");

        EObject a1 = EcoreUtil.create(a);
        EObject b1 = EcoreUtil.create(a);
        EObject b2 = EcoreUtil.create(a);

        a1.eSet(a.getEStructuralFeature("label"), "a1");
        b1.eSet(b.getEStructuralFeature("label"), "b1");
        b2.eSet(b.getEStructuralFeature("label"), "b2");

        ((Collection) a1.eGet(a.getEStructuralFeature("bs"))).add(b1);
        ((Collection) a1.eGet(a.getEStructuralFeature("bs"))).add(b2);

        Resource resource = resourceSet.createResource(URI.createURI("http://test/foo-model"));

        resource.getContents().add(a1);
        resource.save(options);
    }

}
