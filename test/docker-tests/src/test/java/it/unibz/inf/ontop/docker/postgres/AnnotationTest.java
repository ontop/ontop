package it.unibz.inf.ontop.docker.postgres;


import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;

/**
 * Class to test if annotation property can be treated as data property and object property
 *
 *
 */
@Ignore("Too slow (20 min)!")
public class AnnotationTest extends AbstractVirtualModeTest {

    Logger log = LoggerFactory.getLogger(this.getClass());

    final static String owlFile = "/pgsql/annotation/doid.owl";
    final static String obdaFile = "/pgsql/annotation/doid.obda";
    final static String propertyFile = "/pgsql/annotation/doid.properties";

    public AnnotationTest() {
        super(owlFile, obdaFile, propertyFile);
    }


    @Test
    public void testAnnotationInOntology() throws Exception {

        String query = Joiner.on("\n").join(
                CharStreams.readLines(new FileReader("src/test/resources/pgsql/annotation/q1.q")));

        log.debug("Executing query: ");
        log.debug("Query: \n{}", query);

        countResults(query ,76);
    }



}

