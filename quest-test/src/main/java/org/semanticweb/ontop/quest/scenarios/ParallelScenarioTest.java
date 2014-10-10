package org.semanticweb.ontop.quest.scenarios;


import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Ignore;
import org.openrdf.model.URI;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
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
                                                              List<String> testURIs, List<String> names,  List<String> queryFileURLs,
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
        manifestRep.initialize();
        RepositoryConnection con = manifestRep.getConnection();

        ScenarioManifestTestUtils.addTurtle(con, new URL(manifestFileURL), manifestFileURL);

        String suiteName = getManifestName(manifestRep, con, manifestFileURL);
        suite.setName(suiteName);

        // Extract test case information from the manifest file. Note that we only
        // select those test cases that are mentioned in the list.
        StringBuilder query = new StringBuilder(512);
        query.append(" SELECT DISTINCT testURI, testName, resultFile, queryFile, owlFile, obdaFile, parameterFile \n");
        query.append(" FROM {} rdf:first {testURI} \n");
        if (approvedOnly) {
            query.append("    obdat:approval {obdat:Approved}; \n");
        }
        query.append("    mf:name {testName}; \n");
        query.append("    mf:result {resultFile}; \n");
        query.append("    mf:knowledgebase {owlFile}; \n");
        query.append("    mf:mappings {obdaFile}; \n");
        query.append("    [ mf:parameters {parameterFile} ]; \n");
        query.append("    mf:action {action} qt:query {queryFile} \n");
        query.append(" USING NAMESPACE \n");
        query.append("    mf = <http://obda.org/quest/tests/test-manifest#>, \n");
        query.append("    obdat = <http://obda.org/quest/tests/test-scenario#>, \n");
        query.append("    qt = <http://obda.org/quest/tests/test-query#> ");
        TupleQuery testCaseQuery = con.prepareTupleQuery(QueryLanguage.SERQL, query.toString());

        LOGGER.debug("Evaluating query..");
        TupleQueryResult testCases = testCaseQuery.evaluate();

        List<String> testURIs = new ArrayList<>();
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

            URI testURI = (URI) bindingSet.getValue("testURI");
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

            testURIs.add(testURI.toString());
            testNames.add(testName);
            queryFiles.add(queryFile);
            resultFiles.add(resultFile);
        }

        QuestParallelScenario scenario = factory.createQuestParallelScenarioTest(suiteName, testURIs, testNames,
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
        TupleQuery manifestNameQuery = con.prepareTupleQuery(QueryLanguage.SERQL,
                "SELECT ManifestName FROM {ManifestURL} rdfs:label {ManifestName}");
        manifestNameQuery.setBinding("ManifestURL", manifestRep.getValueFactory().createURI(manifestFileURL));
        TupleQueryResult manifestNames = manifestNameQuery.evaluate();
        try {
            if (manifestNames.hasNext()) {
                return manifestNames.next().getValue("ManifestName").stringValue();
            }
        }
        finally {
            manifestNames.close();
        }
        // Derive name from manifest URL
        int lastSlashIdx = manifestFileURL.lastIndexOf('/');
        int secLastSlashIdx = manifestFileURL.lastIndexOf('/', lastSlashIdx - 1);
        return manifestFileURL.substring(secLastSlashIdx + 1, lastSlashIdx);
    }
}
