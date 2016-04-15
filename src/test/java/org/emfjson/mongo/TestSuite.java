package org.emfjson.mongo;

import org.emfjson.mongo.tests.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		MongoHandlerSaveTest.class,
		MongoHandlerLoadTest.class,
		LoadReferenceTest.class,
		MongoHandlerSaveAndLoadTest.class,
		UseIdTest.class,
		DynamicTest.class
})
public class TestSuite {
}
