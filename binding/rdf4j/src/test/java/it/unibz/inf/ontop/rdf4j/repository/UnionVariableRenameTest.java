package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class UnionVariableRenameTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/union-variable-rename/mapping.obda";
    private static final String SQL_SCRIPT = "/union-variable-rename/query.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }
    @Test
    public void testQuery() {
        int count = runQueryAndCount("select ?x WHERE \n" +
                "{\n" +
                "  {\n" +
                "    ?s1 ?x ?o  .\n" +
                "  }\n" +
                "  UNION \n" +
                "  {\n" +
                "    {\n" +
                "      ?s1 ?x ?o .\n" +
                "    }\n" +
                "    {\n" +
                "      ?s1 ?x ?o  .\n" +
                "    }\n" +
                "    UNION \n" +
                "    {\n" +
                "      ?s2 ?x ?o  .\n" +
                "    } \n" +
                "    UNION \n" +
                "    {\n" +
                "      {\n" +
                "        ?s3 ?x ?o .\n" +
                "      }\n" +
                "      {\n" +
                "        ?s3 ?x ?o  .\n" +
                "      }\n" +
                "      UNION \n" +
                "      {\n" +
                "        {\n" +
                "          ?s3 ?x ?o .\n" +
                "        }\n" +
                "        {\n" +
                "          ?s3 ?x ?o  .\n" +
                "        } \n" +
                "        UNION \n" +
                "        {\n" +
                "          ?s4 ?x ?o .\n" +
                "        } \n" +
                "      }  \n" +
                "    }  \n" +
                "  }\n" +
                "}");
        assertEquals(192, count);
    }
}
