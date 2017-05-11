package org.emfjson.mongo;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
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
				.append("uri", getURI().toString());

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

		documents.forEach((Consumer<Document>) e -> {
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

			getContents().add(object);
		});
	}

	@Override
	public void save(Map<?, ?> options) throws IOException {
		if (uri == null) {
			throw new IOException();
		}

		MongoCollection<Document> collection = handler.getCollection(uri);

		Document resourceDoc = new Document("eClass", "EResource")
				.append("uri", getURI().toString());

		collection.insertOne(resourceDoc);

		ObjectId resourceID = id = resourceDoc.getObjectId(ID_FIELD);

		Map<EObject, Document> documents = new HashMap<>();

		for (TreeIterator<EObject> it = getAllContents(); it.hasNext(); ) {
			EObject object = it.next();
			Document document = asDocument(object);
			document.append("eResource", resourceID);
			documents.put(object, document);
		}

		collection.insertMany(new ArrayList<>(documents.values()));

		for (EObject object : documents.keySet()) {
			Document document = documents.get(object);
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
							Document target = documents.get(value);
							identifiers.add(target.getObjectId(ID_FIELD));
						}

						document.append(reference.getName(), identifiers);
						collection.findOneAndUpdate(new Document(ID_FIELD, id),
								new Document("$set", document));

					} else {
						EObject value = (EObject) object.eGet(reference);
						Document target = documents.get(value);
						document.append(reference.getName(), target.getObjectId(ID_FIELD));

						collection.findOneAndUpdate(
								new Document(ID_FIELD, id),
								new Document("$set", document));
					}
				}
			}
		}
	}

	@Override
	protected void doSave(OutputStream outputStream, Map<?, ?> options) throws IOException {
		throw new UnsupportedOperationException();
	}

	private Document asDocument(EObject object) {
		Document document = new Document("eClass", EcoreUtil.getURI(object.eClass()).toString())
				.append("eResource", object.eResource().getURI().toString());

		for (EAttribute attribute : object.eClass().getEAttributes()) {
			if (object.eIsSet(attribute)) {
				if (!attribute.isMany()) {
					document.append(attribute.getName(), object.eGet(attribute));
				}
			} else {
				document.append(attribute.getName(), null);
			}
		}

		for (EReference reference : object.eClass().getEAllReferences()) {
			if (reference.isMany()) {
				document.append(reference.getName(), new ArrayList<ObjectId>());
			} else {
				document.append(reference.getName(), null);
			}
		}

		return document;
	}

	public ObjectId getID() {
		return id;
	}
}
