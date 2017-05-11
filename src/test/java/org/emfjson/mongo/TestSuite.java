package org.emfjson.mongo;

import org.emfjson.mongo.tests.MongoResourceLoadTest;
import org.emfjson.mongo.tests.MongoResourceSaveTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		MongoResourceSaveTest.class,
		MongoResourceLoadTest.class
})
public class TestSuite {
}
