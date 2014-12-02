package org.emfjson.mongo.bench;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.emfjson.EMFJs;
import org.emfjson.couchemf.tests.model.ANode;
import org.emfjson.couchemf.tests.model.BNode;
import org.emfjson.couchemf.tests.model.ModelFactory;
import org.emfjson.couchemf.tests.model.Node;
import org.emfjson.jackson.resource.JsonResourceFactory;
import org.emfjson.mongo.MongoHandler;

public class Benchmark {

	static URI mongoURI = URI.createURI("mongodb://localhost:27017/emfjson-test/bench");

	static EObject createModel() {
		Node node = ModelFactory.eINSTANCE.createNode();
		node.setLabel("root");
		node.setValue("root");

		for (int i=0; i < 500; i++) {
			ANode a = ModelFactory.eINSTANCE.createANode();
			node.getNodes().add(a);
			a.setLabel("A" + i);

			for (int j=0; j < 200; j++) {
				BNode b = ModelFactory.eINSTANCE.createBNode();
				a.getNodes().add(b);
				b.setLabel("B" + i);
			}
		}

		return node;
	}

	static long performSave(Resource resource, Map<String, Object> options) {
		long start = System.currentTimeMillis();
		try {
			resource.save(options);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return System.currentTimeMillis() - start;
	}

	static long times = 20;

	public static void main(String[] args) {
		long sum = 0;
		Map<String, Object> options = new HashMap<>();
		options.put(EMFJs.OPTION_INDENT_OUTPUT, false);
		options.put(EMFJs.OPTION_SERIALIZE_REF_TYPE, false);
		options.put(EMFJs.OPTION_SERIALIZE_TYPE, false);
		for (int i = 0; i < times; i++) {
			ResourceSet resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new JsonResourceFactory());
			resourceSet.getURIConverter().getURIHandlers().add(0, new MongoHandler());

			Resource resource = resourceSet.createResource(mongoURI.appendSegment("test"));
			resource.getContents().add(createModel());

			long cur;
			sum += cur = performSave(resource, options);
			System.out.println("JSON: " + cur / 1000.);
		}
		long average = sum / times;
		System.out.println("JSON: " + average / 1000.);
	}

}
