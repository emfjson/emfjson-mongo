package org.emfjson.mongo.bench;

import com.mongodb.MongoClient;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.emfjson.jackson.resource.JsonResourceFactory;
import org.emfjson.model.ModelFactory;
import org.emfjson.model.TestA;
import org.emfjson.model.TestB;
import org.emfjson.mongo.MongoHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Benchmark {

	static URI mongoURI = URI.createURI("mongodb://localhost:27017/emfjson-test/bench");
	static long times = 20;

	static List<EObject> createModel() {
		List<EObject> contents = new ArrayList<>();

		for (int i = 0; i < 500; i++) {

			TestA a = ModelFactory.eINSTANCE.createTestA();
			a.setStringValue("A" + i);
			contents.add(a);

			for (int j = 0; j < 200; j++) {

				TestB b = ModelFactory.eINSTANCE.createTestB();
				b.setStringValue("B" + i + "-" + j);
				a.getContainBs().add(b);
			}
		}

		return contents;
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

	public static void main(String[] args) {
		long sum = 0;
		Map<String, Object> options = new HashMap<>();

		final MongoClient client = new MongoClient();

		for (int i = 0; i < times; i++) {
			ResourceSet resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new JsonResourceFactory());
			resourceSet.getURIConverter().getURIHandlers().add(0, new MongoHandler(client));

			Resource resource = resourceSet.createResource(mongoURI.appendSegment("test"));
			resource.getContents().addAll(createModel());

			sum += performSave(resource, options);
		}

		long average = sum / times;

		System.out.println("Average time for storing " + (500 * 200) + " elements: " + average / 1000. + " seconds.");
	}

}
