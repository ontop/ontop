package it.unibz.inf.ontop.docker.postgres;


import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to test if annotation property can be treated as data property and object property
 *
 *
 */
public class AnnotationMovieTest extends AbstractVirtualModeTest{
    Logger log = LoggerFactory.getLogger(this.getClass());

    final static String owlFile = "/pgsql/annotation/movieontology.owl";
    final static String obdaFile = "/pgsql/annotation/newSyntaxMovieontology.obda";
    final static String propertyFile = "/pgsql/annotation/newSyntaxMovieontology.properties";

    public AnnotationMovieTest() {
        super(owlFile, obdaFile, propertyFile);
    }


    @Test
    public void testAnnotationInOntology() throws Exception {
        String queryBind = "PREFIX dbpedia: <http://dbpedia.org/ontology/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +
                "WHERE {<http://dbpedia.org/ontology/birthDate> rdfs:label ?r . \n" +

                "}";



        countResults(queryBind, 4);

    }


    @Test
    public void testAnnotationIRI() throws Exception {



        String queryBind = "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +
                " ?movie \n" +
                "WHERE {?movie dc:description ?r . \n" +

                "}";



        countResults(queryBind, 444090);

    }

    @Test
    public void testAnnotationLiteral() throws Exception {



        String queryBind = "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +
                " ?movie \n" +
                "WHERE {?movie dc:date ?r . \n" +

                "}";



        countResults(queryBind, 443300);

    }

    @Test
    public void testAnnotationString() throws Exception {



        String queryBind = "PREFIX dbpedia: <http://dbpedia.org/ontology/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +
                " ?movie \n" +
                "WHERE {?movie dbpedia:gross ?r . \n" +

                "}";



        countResults(queryBind, 112576);

    }


    @Test
    public void testAnnotationDatabaseValue() throws Exception {



        String queryBind = "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +
                " ?movie \n" +
                "WHERE {?movie mo:belongsToGenre ?r . \n" +

                "}";



        countResults(queryBind, 876722);

    }

    @Test //no check is executed to verify that the value is a valid uri
    public void testNewSyntaxUri() throws Exception {



        String queryBind = "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +
                " ?movie \n" +
                "WHERE {?movie mo:hasMaleActor ?r . \n" +

                "} LIMIT 100000";



        // value without LIMIT
        // countResults(queryBind, 7530011);
        countResults(queryBind, 100000);

    }

    @Test //no class in the ontology
    public void testClassUndefined() throws Exception {



        String queryBind = "PREFIX dbpedia: <http://dbpedia.org/ontology/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +
                "WHERE {?r a mo:Vip . \n" +

                "} LIMIT 100000";

        // value without LIMIT
        // countResults(queryBind, 7530011);
        countResults(queryBind, 100000);
    }

    @Test //no dataproperty in the ontology
    public void testDataPropertyUndefined() throws Exception {



        String queryBind = "PREFIX dbpedia: <http://dbpedia.org/ontology/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +
                "WHERE {?company mo:companyId ?r . \n" +

                "}";

        countResults(queryBind, 705859);
//        countResults(queryBind, 10000);

    }

    @Test //no objectproperty in the ontology
    public void testObjectPropertyUndefined() throws Exception {



        String queryBind = "PREFIX dbpedia: <http://dbpedia.org/ontology/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +
                "WHERE {?movie mo:idTitle ?r . \n" +

                "}";



        countResults(queryBind, 444090);
//        countResults(queryBind, 10000);


    }



}

