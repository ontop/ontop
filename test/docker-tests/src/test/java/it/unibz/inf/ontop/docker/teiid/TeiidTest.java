package it.unibz.inf.ontop.docker.teiid;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.BooleanOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Ignore("A proper Docker-compose env needs to added")
public class TeiidTest {
    public static OntopOWLReasoner res;
    public static OntopOWLConnection ct;
    public static OntopOWLStatement st;
    public static TupleOWLResultSet rs;
    public static BooleanOWLResultSet bs;
    public static Logger logger;

    public static String owlfile = "/teiid/university/university.ttl";
    public static String obdafile = "/teiid/university/university.obda";
    public static String propertyfile = "/teiid/teiid.properties";


    @BeforeClass
    public static void init() throws OWLException {
        owlfile = TeiidTest.class.getResource(owlfile).toString();
        obdafile =  TeiidTest.class.getResource(obdafile).toString();
        propertyfile  =  TeiidTest.class.getResource(propertyfile).toString();

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdafile)
                .ontologyFile(owlfile)
                .propertyFile(propertyfile)
                .enableTestMode()
                .build();
        res = factory.createReasoner(config);
        ct = res.getConnection();
        st = ct.createStatement();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        res.close();
        ct.close();
        st.close();
    }

    @Test
    public void testQueryAnswering( ) throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "select ?x  {?x rdf:type  :Student} ";
        runQuery(query);
        /**
         * Results coming from two data sources.
         */
    }

    @Test
    public void testDistinct( ) throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "select distinct ?x  {?x rdf:type  :Student} ";
        runQuery(query);

    }

    @Test
    public void testASK( ) throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "ASK { :1 rdf:type  :Student } ";
        runASKQuery(query);

    }

    @Test
    public void testLimit( ) throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "select ?x  {?x rdf:type  :Student} offset 2 limit 3 ";
        runQuery(query);

    }

    @Test
    public void testOrderBy( ) throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "select ?x  {?x rdf:type  :Student} order by ?x ";
        runQuery(query);

    }

    //REGEXP_LIKE not supported in Teiid
    @Test
    public void testFilter( ) throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                        "select ?x  {?x rdf:type  :Student. ?x foaf:firstName ?y. filter regex(?y, \"a\", \"i\")}";

        runQuery(query);

    }

    @Test
    public void testValues( ) throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                        "select ?x ?y {values ?x { :1 :2 }. ?x rdf:type  :Student. ?x foaf:first_name ?y }";
        runQuery(query);

    }

    @Test
    public void testOptional() throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                        "select ?x ?y {?x rdf:type  :Student. optional { ?x foaf:firstName ?y }}";
        runQuery(query);

    }

    public void runQuery(String query) throws OWLException {
        rs = st.executeSelectQuery(query);
        List<String> schema=rs.getSignature();
        System.out.println(schema);
        while(rs.hasNext()) {
            OWLBindingSet bindingSet = rs.next();
            List<String> ans = new ArrayList<String>();
            for(String var: schema){
                ans.add(bindingSet.getOWLObject(var).toString());
            }
            System.out.println(ans);
        }
    }

    public void runASKQuery(String query) throws OWLException {
        BooleanOWLResultSet brs = st.executeAskQuery(query);
        System.out.println(brs.getValue());


    }

    @Test
    public void returnSQL() throws OWLException {
        String query =
                "PREFIX : <http://example.org/voc#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                        "select ?x  {?x rdf:type  :Student. ?x foaf:firstName ?y. filter regex(?y, \"a\", \"i\")}";
        String sql="";
        IQ executableQuery = st.getExecutableQuery(query);

        System.out.println(executableQuery.toString());

        sql = Optional.of(executableQuery.getTree())
                .filter(t -> t instanceof UnaryIQTree)
                .map(t -> ((UnaryIQTree) t).getChild().getRootNode())
                .filter(n -> n instanceof NativeNode)
                .map(n -> ((NativeNode) n).getNativeQueryString())
                .orElseThrow(() -> new RuntimeException("Cannot extract the SQL query from\n" + executableQuery));

        System.out.println("sql: "+sql);
    }
}
