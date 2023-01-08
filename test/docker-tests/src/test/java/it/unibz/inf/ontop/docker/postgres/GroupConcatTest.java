package it.unibz.inf.ontop.docker.postgres;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 Issue #508
 */
public class GroupConcatTest extends AbstractVirtualModeTest {

    private static final String owlfile = "/pgsql/gconcat/vkg.ttl";
    private static final String obdafile = "/pgsql/gconcat/vkg.obda";
    private static final String propertiesfile = "/pgsql/gconcat/vkg.properties";

    private static OntopOWLEngine REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() {
        REASONER = createReasoner(owlfile, obdafile, propertiesfile);
        CONNECTION = REASONER.getConnection();
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws Exception {
        CONNECTION.close();
        REASONER.close();
    }

    /**
     * @throws Exception
     */
    @Test
    public void testPostgresGroupConcat1() throws Exception {
        String query = readFromFile("/pgsql/gconcat/query1.rq");
        checkReturnedValuesUnordered(ImmutableList.of("Tom"), query);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testPostgresGroupConcat2() throws Exception {
        String query = readFromFile("/pgsql/gconcat/query2.rq");
        checkReturnedValuesUnordered(ImmutableList.of("Tom; Tommaso"), query);
    }


    private String readFromFile(String path) throws IOException {
        return Files.lines(Paths.get(GroupConcatTest.class.getResource(path).getPath()))
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
