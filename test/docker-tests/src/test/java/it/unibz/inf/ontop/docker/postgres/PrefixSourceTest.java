package it.unibz.inf.ontop.docker.postgres;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

import static org.junit.Assert.assertTrue;

/**
 * Class to test that a URI with double prefix has not a prefix  wrongly removed
 */
public class PrefixSourceTest extends AbstractVirtualModeTest {

    private static final String owlfile = "/pgsql/imdb/movieontology.owl";
    private static final String obdafile = "/pgsql/imdb/newPrefixMovieOntology.obda";
    private static final String propertiesfile = "/pgsql/imdb/movieontology.properties";

    private static EngineConnection CONNECTION;

    @BeforeClass
    public static void before() {
        CONNECTION = createReasoner(owlfile, obdafile, propertiesfile);
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws Exception {
        CONNECTION.close();
    }

    @Test
    public void testPrefixInsideURI() throws Exception {
        String queryBind = "PREFIX :  <http://www.movieontology.org/2009/10/01/movieontology.owl>" +
                "PREFIX mo:  <http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "PREFIX mo2:		<http://www.movieontology.org/2009/11/09/movieontology.owl#>" +
                "\n" +
                "SELECT  ?x " +

                "WHERE {?y a mo:East_Asian_Company ; mo2:hasCompanyLocation ?x .  \n" +

                "}";

        assertTrue(
                checkContainsTuplesSetSemantics(
                        queryBind,
                        ImmutableSet.of(
                                ImmutableMap.of("x", "<http://www.movieontology.org/2009/10/01/movieontology.owl#Japan>")
                        )));
    }


}

