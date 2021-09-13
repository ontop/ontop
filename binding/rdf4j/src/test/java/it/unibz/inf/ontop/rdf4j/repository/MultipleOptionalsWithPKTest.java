package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.sql.SQLException;

/*
Test whether multiple OPTIONAL clauses with language tags can work
 */

public class MultipleOptionalsWithPKTest extends AbstractMultipleOptionalsTest {

    private static final String CREATE_DB_FILE = "/multiple-optionals/multiple-optionals-create.sql";
    private static final String OBDA_FILE = "/multiple-optionals/multiple-optionals.obda";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(CREATE_DB_FILE, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }
}
