package it.unibz.inf.ontop.docker.denodo;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Ignore("Currently, has to be installed locally. TODO: create a docker image")
public class DenodoBookFederationTest {

    private static OntopRepository repo;

    @BeforeClass
    public static void init() {

        OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
                //.ontologyFile(owlfile)
                .nativeOntopMappingFile(DenodoBookFederationTest.class.getResource("/denodo/bookfed/bookfed.obda").getFile())
                .propertyFile(DenodoBookFederationTest.class.getResource("/denodo/bookfed/bookfed.properties").getFile())
                .enableTestMode()
                .build();

        repo = OntopRepository.defaultRepository(configuration);
        repo.initialize();
    }


    @AfterClass
    public static void tearDown() throws Exception {
        repo.close();
    }

    @Test
    public void test1() {
        int nbResults = executeQuery("PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
                "\n" +
                "SELECT ?x\n" +
                "{ " +
                "   ?a a :Author ; " +
                "       :name ?x .\n" +
                "}\n");
        assertEquals(22, nbResults);

    }

    @Test
    public void test2() {
        int nbResults = executeQuery("PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
                "\n" +
                "SELECT ?x\n" +
                "{ " +
                "   ?x a :Author .\n" +
                "}");
        assertEquals(22, nbResults);
    }

    @Test
    public void test3() {
        int nbResults = executeQuery("PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
                "\n" +
                "SELECT ?x ?y ?z \n" +
                "{" +
                "   ?x a :Store; \n" +
                "       :name ?y;\n" +
                "       :address ?z\n" +
                "}");
        assertEquals(5, nbResults);
    }

    @Test
    public void test4() {
        int nbResults = executeQuery("PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
                "\n" +
                "SELECT ?x ?y ?z \n" +
                "{?x a :Store; \n" +
                "      :name ?y;\n" +
                "      :address ?z\n" +
                "}" +
                "LIMIT 3");
        assertEquals(3, nbResults);
    }

    @Test
    public void test5() {
        int nbResults = executeQuery("PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
                "\n" +
                "SELECT ?x ?y ?z \n" +
                "{" +
                " ?x a :Store; \n" +
                "      :name ?y;\n" +
                "      :address ?z\n" +
                "}" +
                "OFFSET 2 " +
                "LIMIT 3");
        assertEquals(3, nbResults);
    }

    @Test
    public void test6() {
        int nbResults = executeQuery("PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
                "\n" +
                "SELECT ?x ?title \n" +
                "{" +
                " ?x a :Store;\n" +
                "      :sells ?y ;\n" +
                "      :name 'Athesia' .\n" +
                " ?y a :Book;\n" +
                "      :title ?title .\n" +
                "}");
        assertEquals(5, nbResults);
    }

    @Test
    public void test7() {
        int nbResults = executeQuery("PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
                "\n" +
                "SELECT ?x ?t \n" +
                "{\n" +
                "   ?y a :Book ; :title ?t\n" +
                "OPTIONAL {\n" +
                "       ?x :sells ?y .\n" +
                "  }\n" +
                "}");
        assertEquals(42, nbResults);
    }

    @Test
    public void test8() {
        int nbResults = executeQuery("PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
                "\n" +
                "SELECT ?b ?x {\n" +
                "  ?b :hasEdition ?e .\n" +
                "  ?e :editionNumber ?x .\n" +
                "}");
        assertEquals(28, nbResults);
    }

    @Test
    public void test9() {
        int nbResults = executeQuery("PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
                "\n" +
                "SELECT ?b ?x {\n" +
                "  ?b :hasEdition ?e .\n" +
                "  ?e :editionNumber ?x .\n" +
                "}" +
                "ORDER BY ?x");
        assertEquals(28, nbResults);
    }

    private int executeQuery(String queryString) {
        int i=0;
        try (RepositoryConnection conn = repo.getConnection()) {
            // execute query
            TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result = query.evaluate();
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                System.out.println(bindingSet);
                i++;
            }
            System.out.printf("Total: %d\n", i);
            result.close();
        }
        return i;
    }

}
