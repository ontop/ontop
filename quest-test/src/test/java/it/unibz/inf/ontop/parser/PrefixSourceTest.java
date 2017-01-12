package it.unibz.inf.ontop.parser;


import it.unibz.inf.ontop.quest.AbstractVirtualModeTest;

/**
 * Class to test if annotation property can be treated as data property and object property
 *
 *
 */
public class PrefixSourceTest extends AbstractVirtualModeTest{

    static final String owlfile = "src/test/resources/movieontology.owl";
    static final String obdafile = "src/test/resources/newPrefixMovieOntology.obda";

    public PrefixSourceTest() {
        super(owlfile, obdafile);
    }


    public void testPrefixInsideURI() throws Exception {
        String queryBind = "PREFIX :  <http://www.movieontology.org/2009/10/01/movieontology.owl>" +
                "PREFIX mo:  <http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "PREFIX mo2:		<http://www.movieontology.org/2009/11/09/movieontology.owl#>" +
                "\n" +
                "SELECT  ?x " +

                "WHERE {?y a mo:East_Asian_Company ; mo2:hasCompanyLocation ?x .  \n" +

                "}";

        assertEquals(runQueryAndReturnStringOfIndividualX(queryBind),
                "<http://example.com/resource/?repository=repo&uri=http://www.movieontology.org/2009/10/01/movieontology.owl/China-24951>");
    }


}

