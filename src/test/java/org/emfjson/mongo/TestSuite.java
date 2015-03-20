package org.emfjson.mongo;

import org.emfjson.mongo.tests.DynamicTest;
import org.emfjson.mongo.tests.MongoHandlerTest;
import org.emfjson.mongo.tests.UseIdTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MongoHandlerTest.class,
        UseIdTest.class,
        DynamicTest.class
})
public class TestSuite {
}
