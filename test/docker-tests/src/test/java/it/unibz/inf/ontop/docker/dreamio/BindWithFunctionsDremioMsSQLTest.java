package it.unibz.inf.ontop.docker.dreamio;

import it.unibz.inf.ontop.docker.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.ArrayList;
import java.util.List;


public class BindWithFunctionsDremioMsSQLTest extends AbstractBindTestWithFunctions {
    private static final String owlfile = "/dremio/bind/sparqlBind.owl";
    private static final String obdafile = "/dremio/bind/mapping/sparqlBindDremioMsSql.obda";
    private static final String propertyfile = "/dremio/bind/sparqlBindDremio.properties";

    private static OntopOWLReasoner REASONER;
    private static OWLConnection CONNECTION;

    public BindWithFunctionsDremioMsSQLTest() throws OWLOntologyCreationException {
        super(createReasoner(owlfile, obdafile, propertyfile));
        REASONER = getReasoner();
        CONNECTION = getConnection();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }


}
