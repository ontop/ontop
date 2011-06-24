package it.unibz.krdb.obda.LUBM;


import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.abox.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DLLiterOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class LUBMTester {
    private static final Logger log = LoggerFactory.getLogger(LUBMTester.class);

    static long starttime;
    static long endtime;
    static String dataDirectory = "./";
    static TBoxLoader tboxHelper;
    static OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
    static OBDAModel apic = obdafac.getOBDAModel();

    static String[] queries = {
            "SELECT ?a WHERE {?a :worksFor ?b . ?b :affiliatedOrganizationOf ?c }",
            "SELECT ?a ?b WHERE {?a rdf:type :Person . ?a :teacherOf ?b . ?b rdf:type :Course }",
            "SELECT ?a ?b ?c WHERE {" +
                    " ?a rdf:type :Student . ?a :advisor ?b . " +
                    " ?b rdf:type :FacultyStaff . ?a :takesCourse ?c . " +
                    " ?b :teacherOf ?c . ?c rdf:type :Course }",
            "SELECT ?a ?b WHERE {?a rdf:type :Person . ?a :worksFor ?b . ?b rdf:type :Organization}",
            "SELECT ?a WHERE {" +
                    " ?a rdf:type :Person . ?a :worksFor ?b . " +
                    " ?b rdf:type :University . ?b :hasAlumnus ?a }"
    };


    public static void main(String[] args) throws Exception {

        // Prepare reasoner
        tboxHelper = new TBoxLoader(dataDirectory);
        DataSource ds = obdafac.getJDBCDataSource(CSVLoader.url, CSVLoader.username, CSVLoader.password, CSVLoader.driver);
        apic.getDatasourcesController().addDataSource(ds);

        DataQueryReasoner reasoner = tboxHelper.loadReasoner(apic, TBoxLoader.manager);

        for (String query : queries) {
            starttime = System.nanoTime();
            Set<String> res = execute(query, reasoner);
            endtime = System.nanoTime();
            log.info("Result size {}, res: {}", res.size(), res);
            log.info("Executing query {} took: {}", query, (endtime - starttime) * 1.0e-9);
        }

    }

    private static void loadData() throws Exception {

        int universityCount = 1;

        starttime = System.nanoTime();
        DLLiterOntology ontology = tboxHelper.loadOnto();
        DAG dag = new DAG(ontology);
        endtime = System.nanoTime();
        log.info("Building DAG took: {}", (endtime - starttime) * 1.0e-9);

        starttime = System.nanoTime();
        dag.index();
        endtime = System.nanoTime();
        log.info("Indexing DAG took: {}", (endtime - starttime) * 1.0e-9);

        CSVDumper dumper = new CSVDumper(dag, dataDirectory);
        CSVLoader loader = new CSVLoader(dataDirectory);

        dumper.dump(universityCount);

        loader.recreateDB();
        loader.loadData();
        loader.makeIndexes();
    }

    private static Set<String> execute(String query, DataQueryReasoner reasoner) throws Exception {

        String prefix = getPrefix();
        String fullquery = prefix + "\n" + query;
        Statement statement = reasoner.getStatement();
        QueryResultSet result = statement.executeQuery(fullquery);
        int col = result.getColumCount();
        HashSet<String> tuples = new HashSet<String>();
        while (result.nextRow()) {
            String tuple = "";
            for (int i = 1; i <= col; i++) {
                if (tuple.length() > 0) {
                    tuple = tuple + ",";
                }

                URI uri = result.getAsURI(i);
                tuple = tuple + uri.getFragment();

            }
            tuples.add(tuple);
        }
        return tuples;
    }

    private static String getPrefix() {
        String queryString = "";
        String defaultNamespace = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl";
        queryString += "BASE <" + defaultNamespace + ">\n";

        queryString += "PREFIX :   <" + defaultNamespace + "#>\n";

        queryString += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
        queryString += "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n";
        queryString += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
        queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
        queryString += "PREFIX dllite: <http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#>\n";
        queryString += "PREFIX ucq: <http://www.obda.org/ucq/predicate/queryonly#>\n";

        return queryString;
    }

}
