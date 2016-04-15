# MongoDB Adapter for Eclipse Modeling Framework (EMF)

An easy to use adapter that works on top of the EMF Resource API to store and retrieve EMF Models on MongoDB.

### Download

Add the following dependency to your pom file.

```xml
<dependency>
    <groupId>org.emfjson</groupId>
    <artifactId>emfjson-mongo</artifactId>
    <version>0.3.0</version>
</dependency>
```

You can also find the jars in [maven central](http://search.maven.org/#search|ga|1|emfjson-mongo)

### Usage

Setup the ResourceSet:

```java
ResourceSet resourceSet = new ResourceSetImpl();

resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new JsonResourceFactory());
resourceSet.getURIConverter().getURIHandlers().add(0, new MongoHandler());
```

Create a Resource with a URI pointing to MongoDB. The URI must contains 3 segments identifying the database, the collection and the document id.

The URI should have the form ```mongodb://{host}[:{port}]/{db}/{collection}/{id}```

```java
URI uri = URI.createURI("mongodb://localhost:27017/emfjson-test/models/model1");
Resource resource = resourceSet.createResource(uri);
```

Alternatively, you can use a URI mapping:

```java
resourceSet.getURIConverter().getURIMap().put(
	URI.createURI("http://resources/"), 
	URI.createURI("mongodb://localhost:27017/emfjson-test/models/"));
	
Resource resource = resourceSet.createResource(URI.createURI("http://resources/model1"));
```

Override the name of the field to avoid problem with the reserved name of Bson. And set the uri handler, to avoid problem with external resources.

```java
	HashMap<String, Object> DEFAULT_OPTIONS = new HashMap<String, Object>();
	DEFAULT_OPTIONS.put(EMFJs.OPTION_URI_HANDLER, new IdentityURIHandler());
	DEFAULT_OPTIONS.put(EMFJs.OPTION_REF_FIELD, "_ref");
	resourceSet.getLoadOptions().putAll(DEFAULT_OPTIONS);
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
