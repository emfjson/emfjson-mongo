# MongoDB Adapter for Eclipse Modeling Framework (EMF)

An easy to use adapter that works on top of the EMF Resource API to store and retrieve EMF Models on MongoDB.

### Usage

Setup the ResourceSet:

```java
ResourceSet resourceSet = new ResourceSetImpl();

resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new JsonResourceFactory());
resourceSet.getURIConverter().getURIHandlers().add(0, new MongoHandler());
```

Create a Resource with a URI pointing to MongoDB. The URI must contains 3 segments identifying the database, the collection and the document id. The URI is in the form 
mongodb://{host}[:{port}]/{db}/{collection}/{id}

```java
Resource resource = resourceSet.createResource(URI.createURI("mongodb://localhost:27017/emfjson-test/models/model1"));
```

Alternatively, you can use a URI mapping:

```java
resourceSet.getURIConverter().getURIMap().put(
	URI.createURI("http://resources/"), 
	URI.createURI("mongodb://localhost:27017/emfjson-test/models/"));
	
Resource resource = resourceSet.createResource(URI.createURI("http://resources/model1"));
```

Saving documents

```java
EPackage p = EcoreFactory.eINSTANCE.createEPackage();
p.setName("p");

EClass c = EcoreFactory.eINSTANCE.createEClass();
c.setName("A");

p.getEClassifiers().add(c);

resource.getContents().add(p);
resource.save(null);
```

Loading document

```java
resource.load(null);
```
