package it.unibz.inf.ontop.docker.snowflake;

import it.unibz.inf.ontop.docker.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

@Ignore("Snowflake is a non-free cloud DB")
public class BindWithFunctionsSnowflakeTest extends AbstractBindTestWithFunctions {
    private static final String owlfile = "/snowflake/sparqlBind.owl";
    private static final String obdafile = "/snowflake/sparqlBind.obda";
    private static final String propertiesfile = "/snowflake/sparqlBind.properties";

    private static OntopOWLEngine REASONER;
    private static OWLConnection CONNECTION;

    public BindWithFunctionsSnowflakeTest() throws OWLOntologyCreationException {
        super(createReasoner(owlfile, obdafile, propertiesfile));
        REASONER = getReasoner();
        CONNECTION = getConnection();
    }

    @AfterClass
    public static void after() throws Exception {
        CONNECTION.close();
        REASONER.close();
    }
}
