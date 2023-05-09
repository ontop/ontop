package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.GenericStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class ConstructEmptyUnionTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/construct-empty-union/mapping.obda";
    private static final String SQL_SCRIPT = "/construct-empty-union/database.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    /**
     the issue was fixed in the meta-mapping expander overhaul in April 2020
     */

    @Test
    public void testConstructEmptyUnion()  {

        String query = "PREFIX : <http://foobar.abc/ontology/demo#>" +
                        "CONSTRUCT { ?s :id2 ?o } WHERE {" +
                        "{ }" +
                        "UNION" +
                        "{ ?s :id ?o . }" +
                        "}";
        assertEquals(4, runGraphQueryAndCount(query));
    }

}
