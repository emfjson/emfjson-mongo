package org.emfjson.mongo;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.emfjson.mongo.EcoreHelpers.uriOf;
import static org.emfjson.mongo.MongoHandler.ID_FIELD;

public class MongoResource extends ResourceImpl {

	private final Map<ObjectId, EObject> idToObjects = new ConcurrentHashMap<>();
	private final Map<EObject, ObjectId> objectToIds = new ConcurrentHashMap<>();

	private final MongoHandler handler;
	private ObjectId id;

	public MongoResource(URI uri, MongoHandler handler) {
		super(uri);

		this.handler = handler;
	}

	@Override
	public EList<EObject> getContents() {
		return super.getContents();
	}


	public ObjectId getID(EObject object) {
		return objectToIds.get(object);
	}

	public ObjectId getID() {
		return id;
	}

	@Override
	protected EObject getEObjectByID(String id) {
		return super.getEObjectByID(id);
	}

	@Override
	public void delete(Map<?, ?> options) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void load(Map<?, ?> options) throws IOException {
		MongoCollection<Document> collection = handler.getCollection(uri);

		Document filter = new Document("eClass", "EResource")
				.append("uri", uriOf(this));

		Document document = collection
				.find(filter)
				.limit(1)
				.first();

		if (document == null) {
			return;
		}

		@SuppressWarnings("unchecked")
		List<ObjectId> contents = (List<ObjectId>) document.get("contents");
		FindIterable<Document> documents = collection.find(new Document("_id", new Document("$in", contents)));

		Map<EObject, Map<EReference, List<ObjectId>>> resolving = new HashMap<>();

		documents.forEach((Consumer<Document>) e -> getContents().add(fromDocument(collection, e, resolving)));

		resolving.forEach(((object, map) ->
				map.forEach((reference, objectIds) -> {
							List<EObject> objects = objectIds.stream()
									.map(idToObjects::get)
									.collect(Collectors.toList());

							if (reference.isMany()) {
								((List<EObject>) object.eGet(reference)).addAll(objects);
							} else if (!objects.isEmpty()) {
								object.eSet(reference, objects.get(0));
							}
						}
				)));
	}

	@Override
	public void save(Map<?, ?> options) throws IOException {
		if (uri == null) {
			throw new IOException();
		}

		MongoCollection<Document> collection = handler.getCollection(uri);

		Document resourceDoc = new Document("eClass", "EResource")
				.append("uri", uriOf(this));

		collection.insertOne(resourceDoc);

		ObjectId resourceID = id = resourceDoc.getObjectId(ID_FIELD);

		Map<EObject, Document> documents = new HashMap<>();

		getAllContents().forEachRemaining(object -> {
			Document document = asDocument(object);
			document.append("eResource", resourceID);
			documents.putIfAbsent(object, document);
		});

		collection.insertMany(new ArrayList<>(documents.values()));

		documents.forEach((object, document) -> {
			ObjectId id = document.getObjectId(ID_FIELD);
			objectToIds.put(object, id);
			idToObjects.put(id, object);

			for (EReference reference : object.eClass().getEAllReferences()) {
				if (object.eIsSet(reference)) {
					if (reference.isMany()) {
						@SuppressWarnings("unchecked")
						Collection<EObject> values = (Collection<EObject>) object.eGet(reference);

						List<ObjectId> identifiers = new ArrayList<>();
						for (EObject value : values) {
							if (value.eResource() == null) {
								// add error
							} else {
								Document target = documents.get(value);
								if (target != null) {
									identifiers.add(target.getObjectId(ID_FIELD));
								} else {
									// add error
								}
							}
						}

						// add all references
						document.append(reference.getName(), identifiers);
						collection.findOneAndUpdate(new Document(ID_FIELD, id), new Document("$set", document));

					} else {
						EObject value = (EObject) object.eGet(reference);

						if (value.eResource() == null) {
							// add error
						} else {
							Document target = documents.get(value);
							if (target != null) {
								document.append(reference.getName(), target.getObjectId(ID_FIELD));
								collection.findOneAndUpdate(new Document(ID_FIELD, id), new Document("$set", document));
							} else {
								// add error
							}
						}
					}
				}
			}
		});
	}

	@Override
	protected void doSave(OutputStream outputStream, Map<?, ?> options) throws IOException {
		throw new UnsupportedOperationException();
	}

	private Document asDocument(EObject object) {
		Document document = new Document("eClass", uriOf(object.eClass()))
				.append("eResource", uriOf(object.eResource()));

		object.eClass().getEAllAttributes()
				.forEach(attribute -> {
					if (object.eIsSet(attribute)) {
						if (!attribute.isMany()) {
							document.append(attribute.getName(), object.eGet(attribute));
						}
					} else {
						document.append(attribute.getName(), null);
					}
				});

		object.eClass().getEAllReferences()
				.forEach(reference -> {
					if (reference.isMany()) {
						document.append(reference.getName(), new ArrayList<ObjectId>());
					} else {
						document.append(reference.getName(), null);
					}
				});

		return document;
	}

	private EObject fromDocument(MongoCollection<Document> collection, Document e, Map<EObject, Map<EReference, List<ObjectId>>> resolving) {
		String type = e.get("eClass", String.class);
		EClass eClass = (EClass) resourceSet.getEObject(URI.createURI(type), true);
		EObject object = EcoreUtil.create(eClass);

		for (EAttribute attribute : eClass.getEAllAttributes()) {
			if (e.containsKey(attribute.getName())) {
				Object value = e.get(attribute.getName());

				if (value != null && !attribute.isMany()) {
					Object converted = EcoreUtil.createFromString(attribute.getEAttributeType(), value.toString());
					object.eSet(attribute, converted);
				}
			}
		}

		for (EReference reference : eClass.getEAllReferences()) {
			if (reference.isContainment()) {

				if (e.containsKey(reference.getName())) {
					if (reference.isMany()) {
						@SuppressWarnings("unchecked")
						List<ObjectId> values = (List<ObjectId>) e.get(reference.getName());
						if (values != null && !values.isEmpty()) {
							@SuppressWarnings("unchecked")
							List<EObject> list = (List<EObject>) object.eGet(reference);
							collection.find(new Document("_id", new Document("$in", values)))
									.forEach((Consumer<Document>) doc -> list.add(fromDocument(collection, doc, resolving)));
						}
					} else {
						ObjectId value = (ObjectId) e.get(reference.getName());
						if (value != null) {
							collection.find(new Document("_id", value))
									.forEach((Consumer<Document>) doc -> object.eSet(reference, fromDocument(collection, doc, resolving)));
						}
					}
				}
			} else {
				if (e.containsKey(reference.getName())) {
					Map<EReference, List<ObjectId>> map = resolving.computeIfAbsent(object, k -> new HashMap<>());

					if (reference.isMany()) {
						@SuppressWarnings("unchecked")
						List<ObjectId> values = (List<ObjectId>) e.get(reference.getName());
						if (!values.isEmpty()) {
							map.putIfAbsent(reference, values);
						}

					} else {
						ObjectId value = (ObjectId) e.get(reference.getName());
						if (value != null) {
							map.putIfAbsent(reference, Stream.of(value).collect(Collectors.toList()));
						}
					}
				}
			}
		}

		objectToIds.putIfAbsent(object, e.getObjectId(MongoHandler.ID_FIELD));
		idToObjects.putIfAbsent(e.getObjectId(MongoHandler.ID_FIELD), object);

		return object;
	}
}
