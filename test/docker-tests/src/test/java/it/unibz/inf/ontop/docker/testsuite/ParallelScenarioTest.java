package it.unibz.inf.ontop.docker.testsuite;


import it.unibz.inf.ontop.docker.QuestParallelScenario;
import it.unibz.inf.ontop.docker.ScenarioManifestTestUtils;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Ignore
public class ParallelScenarioTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuestParallelScenario.class);


    public interface ParallelFactory {

        QuestParallelScenario createQuestParallelScenarioTest(String suiteName,
                                                              List<String> testIRIs, List<String> names, List<String> queryFileURLs,
                                                              List<String> resultFileURLs, String owlFileURL, String obdaFileURL,
                                                              String parameterFileURL);

        String getMainManifestFile();
    }

    public static TestCase extractTest(String manifestFileURL, ParallelFactory factory) throws Exception {
        return extractTest(manifestFileURL, factory, true);
    }

    public static TestCase extractTest(String manifestFileURL, ParallelFactory factory, boolean approvedOnly) throws Exception {
        LOGGER.info("Building test extractTest for {}", manifestFileURL);

        TestSuite suite = new TestSuite(factory.getClass().getName());

        // Read manifest and create declared test cases
        Repository manifestRep = new SailRepository(new MemoryStore());
        manifestRep.init();
        RepositoryConnection con = manifestRep.getConnection();

        ScenarioManifestTestUtils.addTurtle(con, new URL(manifestFileURL), manifestFileURL);

        String suiteName = getManifestName(manifestRep, con, manifestFileURL);
        suite.setName(suiteName);

        // Extract test case information from the manifest file. Note that we only
        // select those test cases that are mentioned in the list.
        StringBuilder query = new StringBuilder(512);
        query.append(" PREFIX mf: <http://obda.org/quest/tests/test-manifest#> \n");
        query.append(" PREFIX obdat: <http://obda.org/quest/tests/test-scenario#> \n");
        query.append(" PREFIX qt: <http://obda.org/quest/tests/test-query#> \n");
        query.append(" SELECT DISTINCT ?testIRI ?testName ?resultFile ?queryFile ?owlFile ?obdaFile ?parameterFile \n");
        query.append(" WHERE { [] rdf:first ?testIRI . \n");
        if (approvedOnly) {
            query.append(" ?testIRI obdat:approval obdat:Approved . \n");
        }
        query.append(" ?testIRI mf:name ?testName; \n");
        query.append("    mf:result ?resultFile; \n");
        query.append("    mf:knowledgebase ?owlFile; \n");
        query.append("    mf:mappings ?obdaFile . \n");
        query.append(" OPTIONAL { ?testIRI mf:parameters ?parameterFile . } \n");
        query.append(" ?testIRI mf:action ?action . \n");
        query.append(" ?action qt:query ?queryFile . }\n");

        TupleQuery testCaseQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query.toString());

        LOGGER.debug("Evaluating query..");
        TupleQueryResult testCases = testCaseQuery.evaluate();

        List<String> testIRIs = new ArrayList<>();
        List<String> testNames = new ArrayList<>();
        List<String> queryFiles = new ArrayList<>();
        List<String> resultFiles = new ArrayList<>();

        String mainOwlFile = null;
        String mainObdaFile = null;
        String mainParameterFile = null;

        /**
         * Test cases that share the same configuration
         * will be run in parallel.
         */
        while (testCases.hasNext()) {
            BindingSet bindingSet = testCases.next();

            IRI testIRI = (IRI) bindingSet.getValue("testIRI");
            String testName = bindingSet.getValue("testName").toString();
            String resultFile = bindingSet.getValue("resultFile").toString();
            String queryFile = bindingSet.getValue("queryFile").toString();
            String owlFile = bindingSet.getValue("owlFile").toString();
            String obdaFile = bindingSet.getValue("obdaFile").toString();
            String parameterFile = (bindingSet.getValue("parameterFile") == null
                    ? "" : bindingSet.getValue("parameterFile").toString());

            /**
             * Checks the config files of the Quest instance.
             *
             * Uses the first given files and ignores tests that use different files.
             */
            if (mainOwlFile == null)
                mainOwlFile = owlFile;
            else if (!mainOwlFile.equals(owlFile)) {
                LOGGER.warn(String.format("%s use a different OWL file (%s vs %s). Ignored", testName, owlFile, mainOwlFile));
                continue;
            }
            if (mainObdaFile == null)
                mainObdaFile = obdaFile;
            else if (!mainObdaFile.equals(obdaFile)) {
                LOGGER.warn("{} use a different OBDA file. Ignored", testName);
                continue;
            }
            if (mainParameterFile == null)
                mainParameterFile = parameterFile;
            else if (!mainParameterFile.equals(parameterFile)) {
                LOGGER.warn("{} use a different parameter file. Ignored", testName);
                continue;
            }

            LOGGER.debug("Found test case: {}", testName);

            testIRIs.add(testIRI.toString());
            testNames.add(testName);
            queryFiles.add(queryFile);
            resultFiles.add(resultFile);
        }

        QuestParallelScenario scenario = factory.createQuestParallelScenarioTest(suiteName, testIRIs, testNames,
                queryFiles, resultFiles, mainOwlFile, mainObdaFile, mainParameterFile);

        testCases.close();
        con.close();

        manifestRep.shutDown();
        if (scenario == null) {
            return null;
        }
        return new ParallelTestCase(suiteName, scenario);
    }

    protected static String getManifestName(Repository manifestRep, RepositoryConnection con, String manifestFileURL)
            throws QueryEvaluationException, RepositoryException, MalformedQueryException
    {
        // Try to extract extractTest name from manifest file
        TupleQuery manifestNameQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
                "SELECT ?ManifestName WHERE { ?ManifestURL rdfs:label ?ManifestName }");
        manifestNameQuery.setBinding("ManifestURL", manifestRep.getValueFactory().createIRI(manifestFileURL));
        try (TupleQueryResult manifestNames = manifestNameQuery.evaluate()) {
            if (manifestNames.hasNext()) {
                return manifestNames.next().getValue("ManifestName").stringValue();
            }
        }
        // Derive name from manifest URL
        int lastSlashIdx = manifestFileURL.lastIndexOf('/');
        int secLastSlashIdx = manifestFileURL.lastIndexOf('/', lastSlashIdx - 1);
        return manifestFileURL.substring(secLastSlashIdx + 1, lastSlashIdx);
    }
}
