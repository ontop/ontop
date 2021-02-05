package it.unibz.inf.ontop.owlapi;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class DefaultBindWithFunctionsTest extends AbstractBindWithFunctionsTest {

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA("/test/bind/sparqlBindWithFns-create-h2.sql",
                "/test/bind/sparqlBindWithFunctions.obda",
                "/test/bind/sparqlBind.owl");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        release();
    }
}
