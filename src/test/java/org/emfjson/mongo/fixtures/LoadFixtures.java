package org.emfjson.mongo.fixtures;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.emf.common.util.URI;
import org.emfjson.model.ModelPackage;
import org.emfjson.mongo.MongoHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.emfjson.mongo.EcoreHelpers.uriOf;
import static org.emfjson.mongo.MongoHandler.ID_FIELD;

public class LoadFixtures {

	public static void fixtureRootElements(MongoHandler handler, URI testURI) {
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
				.append("uri", testURI.toString())
				.append("contents", Stream.of(a1.getObjectId(ID_FIELD), a2.getObjectId(ID_FIELD))
						.collect(Collectors.toList()));

		collection.insertOne(resourceDoc);
	}

	public static void fixtureTest_SingleReference(MongoHandler handler, URI testURI) {
		MongoCollection<Document> collection = handler.getCollection(testURI);

		Document a1 = new Document()
				.append("eClass", uriOf(ModelPackage.Literals.TEST_A))
				.append(ModelPackage.Literals.TEST_A__STRING_VALUE.getName(), "a1");

		Document b1 = new Document()
				.append("eClass", uriOf(ModelPackage.Literals.TEST_B))
				.append(ModelPackage.Literals.TEST_A__STRING_VALUE.getName(), "b1");

		collection.insertOne(a1);
		collection.insertOne(b1);

		Document resourceDoc = new Document()
				.append("eClass", "EResource")
				.append("uri", testURI.toString())
				.append("contents", Stream.of(a1.getObjectId(ID_FIELD), b1.getObjectId(ID_FIELD))
						.collect(Collectors.toList()));

		collection.insertOne(resourceDoc);
		collection.findOneAndUpdate(new Document("_id", a1.getObjectId("_id")),
				new Document("$set", new Document("oneB", b1.getObjectId("_id"))));

		collection.find().forEach((Consumer<? super Document>) System.out::println);
	}

	public static void fixtureTest_ManyReferences(MongoHandler handler, URI uri) {
		MongoCollection<Document> collection = handler.getCollection(uri);

		Document a1 = new Document()
				.append("eClass", uriOf(ModelPackage.Literals.TEST_A))
				.append(ModelPackage.Literals.TEST_A__STRING_VALUE.getName(), "a1");

		Document b1 = new Document()
				.append("eClass", uriOf(ModelPackage.Literals.TEST_B))
				.append(ModelPackage.Literals.TEST_A__STRING_VALUE.getName(), "b1");

		Document b2 = new Document()
				.append("eClass", uriOf(ModelPackage.Literals.TEST_B))
				.append(ModelPackage.Literals.TEST_A__STRING_VALUE.getName(), "b2");

		Document b3 = new Document()
				.append("eClass", uriOf(ModelPackage.Literals.TEST_B))
				.append(ModelPackage.Literals.TEST_A__STRING_VALUE.getName(), "b3");

		collection.insertOne(a1);
		collection.insertOne(b1);
		collection.insertOne(b2);
		collection.insertOne(b3);

		Document resourceDoc = new Document()
				.append("eClass", "EResource")
				.append("uri", uri.toString())
				.append("contents", Stream.of(a1.getObjectId(ID_FIELD), b1.getObjectId(ID_FIELD), b2.getObjectId(ID_FIELD), b3.getObjectId(ID_FIELD))
						.collect(Collectors.toList()));

		collection.insertOne(resourceDoc);

		List<ObjectId> values = new ArrayList<>();
		values.add(b1.getObjectId("_id"));
		values.add(b2.getObjectId("_id"));
		values.add(b3.getObjectId("_id"));

		collection.findOneAndUpdate(new Document("_id", a1.getObjectId("_id")),
				new Document("$set", new Document("manyBs", values)));

		collection.find().forEach((Consumer<? super Document>) System.out::println);
	}

	public static void singleContainment(MongoHandler handler, URI uri) {
		MongoCollection<Document> collection = handler.getCollection(uri);

		Document a1 = new Document()
				.append("eClass", uriOf(ModelPackage.Literals.TEST_A))
				.append(ModelPackage.Literals.TEST_A__STRING_VALUE.getName(), "a1");

		Document b1 = new Document()
				.append("eClass", uriOf(ModelPackage.Literals.TEST_B))
				.append(ModelPackage.Literals.TEST_A__STRING_VALUE.getName(), "b1");

		collection.insertOne(a1);
		collection.insertOne(b1);

		Document resourceDoc = new Document()
				.append("eClass", "EResource")
				.append("uri", uri.toString())
				.append("contents", Stream.of(a1.getObjectId(ID_FIELD))
						.collect(Collectors.toList()));

		collection.insertOne(resourceDoc);

		collection.findOneAndUpdate(new Document("_id", a1.getObjectId("_id")),
				new Document("$set", new Document(ModelPackage.Literals.TEST_A__CONTAIN_B.getName(), b1.getObjectId("_id"))));

		collection.find().forEach((Consumer<? super Document>) System.out::println);
	}

	public static void singleAndManyContainments(MongoHandler handler, URI uri) {
		MongoCollection<Document> collection = handler.getCollection(uri);

		Document a1 = new Document()
				.append("eClass", uriOf(ModelPackage.Literals.TEST_A))
				.append(ModelPackage.Literals.TEST_A__STRING_VALUE.getName(), "a1");

		Document b1 = new Document()
				.append("eClass", uriOf(ModelPackage.Literals.TEST_B))
				.append(ModelPackage.Literals.TEST_A__STRING_VALUE.getName(), "b1");

		Document b2 = new Document()
				.append("eClass", uriOf(ModelPackage.Literals.TEST_B))
				.append(ModelPackage.Literals.TEST_A__STRING_VALUE.getName(), "b2");

		Document b3 = new Document()
				.append("eClass", uriOf(ModelPackage.Literals.TEST_B))
				.append(ModelPackage.Literals.TEST_A__STRING_VALUE.getName(), "b3");

		Document b4 = new Document()
				.append("eClass", uriOf(ModelPackage.Literals.TEST_B))
				.append(ModelPackage.Literals.TEST_A__STRING_VALUE.getName(), "b4");

		collection.insertOne(a1);
		collection.insertOne(b1);
		collection.insertOne(b2);
		collection.insertOne(b3);
		collection.insertOne(b4);

		Document resourceDoc = new Document()
				.append("eClass", "EResource")
				.append("uri", uri.toString())
				.append("contents", Stream.of(a1.getObjectId(ID_FIELD))
						.collect(Collectors.toList()));

		collection.insertOne(resourceDoc);

		collection.findOneAndUpdate(new Document("_id", a1.getObjectId("_id")),
				new Document("$set", new Document(ModelPackage.Literals.TEST_A__CONTAIN_B.getName(), b1.getObjectId("_id"))));

		List<ObjectId> values = new ArrayList<>();
		values.add(b2.getObjectId("_id"));
		values.add(b3.getObjectId("_id"));
		values.add(b4.getObjectId("_id"));

		collection.findOneAndUpdate(new Document("_id", a1.getObjectId("_id")),
				new Document("$set", new Document(ModelPackage.Literals.TEST_A__CONTAIN_BS.getName(), values)));

		collection.find().forEach((Consumer<? super Document>) System.out::println);
	}
}
