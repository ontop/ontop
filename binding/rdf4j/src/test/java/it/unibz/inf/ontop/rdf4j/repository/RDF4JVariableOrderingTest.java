package it.unibz.inf.ontop.rdf4j.repository;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Checks whether the order of variables in the returned binding set is identical to the order in the SPARQL query
 * (in particular, may differ from the natural ordering of variables)
 * Reproduces issue #291
 */
public class RDF4JVariableOrderingTest extends AbstractRDF4JTest {
    private static final String CREATE_DB_FILE = "/var-order/var-order-create.sql";
    private static final String MAPPING_FILE = "/var-order/var-order.obda";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(CREATE_DB_FILE, MAPPING_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testVariableOrdering() {
        String query = "" +
                "PREFIX : <http://example.org/>\n"+
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
                "SELECT  ?i ?v ?l\n" +
                "WHERE {\n" +
                "  ?i rdfs:label ?l .\n" +
                "  ?i :value ?v \n" +
                "}";

        TupleQueryResult result = evaluate(query);
        assertTrue(result.hasNext());
        BindingSet bindingSet = result.next();
        assertEquals(
                ImmutableList.of("i", "v", "l"),
                ImmutableList.copyOf(bindingSet.iterator()).stream()
                        .map(Binding::getName)
                        .collect(ImmutableCollectors.toList())
        );
        result.close();
    }
}

