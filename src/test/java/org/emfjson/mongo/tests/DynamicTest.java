package org.emfjson.mongo.tests;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emfjson.jackson.resource.JsonResourceFactory;
import org.emfjson.mongo.MongoHandler;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.*;

public class DynamicTest {

    private ResourceSetImpl resourceSet;

    @Before
    public void setUp() throws IOException {
        resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new JsonResourceFactory());
        resourceSet.getURIConverter().getURIHandlers().add(0, new MongoHandler());

        resourceSet.getURIConverter().getURIMap().put(
                URI.createURI("http://test/foo"),
                URI.createURI("mongodb://localhost:27017/emfjson-test/models/foo"));

        resourceSet.getURIConverter().getURIMap().put(
                URI.createURI("http://test/foo-model"),
                URI.createURI("mongodb://localhost:27017/emfjson-test/models/foo-model"));

        createModel(createPackage(resourceSet), resourceSet);
        resourceSet.getResources().clear();
    }

    @Test
    public void testLoadModel() throws IOException {
        Resource resource = resourceSet.createResource(URI.createURI("http://test/foo-model"));

        assertNotNull(resource);
        assertTrue(resource.getContents().isEmpty());

        resource.load(null);

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
        resource.save(null);

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
        resource.save(null);
    }

}
