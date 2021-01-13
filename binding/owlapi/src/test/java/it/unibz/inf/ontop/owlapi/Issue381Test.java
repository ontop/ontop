package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;
import java.sql.SQLException;

public class Issue381Test extends AbstractOWLAPITest {

    @BeforeClass
    public static void before() throws OWLOntologyCreationException, SQLException, IOException {
        initOBDA("/issue381/data.sql", "/issue381/mapping.obda", "/issue381/ontology.owl");
    }

    @AfterClass
    public static void after() throws OWLException, SQLException {
        release();
    }

    @Test
    public void testLoad() throws Exception {
        String sparqlQuery =
                "SELECT  *\n" +
                        "WHERE\n" +
                        "  { ?s  <http://ex.org/p1>  ?o ;\n" +
                        "        <http://ex.org/p2>  ?o1 ;\n" +
                        "        ?p                  ?o\n" +
                        "  }";

        checkReturnedValues(sparqlQuery, "s", ImmutableList.of());
    }

}
