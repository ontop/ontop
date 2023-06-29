package it.unibz.inf.ontop.docker.utils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.unibz.inf.ontop.docker.ScenarioManifestTestUtils;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collection;
import java.util.List;

import static it.unibz.inf.ontop.docker.ScenarioManifestTestUtils.addTurtle;

public class ManifestTestUtils {

    static final Logger LOGGER = LoggerFactory.getLogger(ManifestTestUtils.class);


    public static Collection<Object[]> parametersFromSuperManifest(String manifestFilePath,
                                                                   ImmutableSet<String> ignoredTests,
                                                                   RepositoryRegistry registry) throws Exception {
        URL url = ScenarioManifestTestUtils.class.getResource(manifestFilePath);

        if (url == null)
            throw new RuntimeException("Could not find the resource file " + manifestFilePath
                    + ".\nPlease make sure resources have been generated");

        Repository manifestRep = new SailRepository(new MemoryStore());
        manifestRep.init();
        RepositoryConnection con = manifestRep.getConnection();

        String manifestFile = url.toString();
        addTurtle(con, url, manifestFile);

        String query = "PREFIX mf: <http://obda.org/quest/tests/test-manifest#> \n"
                + "PREFIX qt: <http://obda.org/quest/tests/test-query#> \n"
                + "SELECT DISTINCT ?manifestFile WHERE { ?x rdf:first ?manifestFile .} ";

        TupleQueryResult manifestResults = con.prepareTupleQuery(QueryLanguage.SPARQL, query, manifestFile).evaluate();
        List<Object[]> testCaseParameters = Lists.newArrayList();
        while (manifestResults.hasNext()) {
            BindingSet bindingSet = manifestResults.next();
            String subManifestFile = bindingSet.getValue("manifestFile").toString();
            testCaseParameters.addAll(parametersFromSubManifest(new URL(subManifestFile), true, ignoredTests,
                    registry));
        }

        manifestResults.close();
        con.close();
        manifestRep.shutDown();

        LOGGER.info("Created aggregated test suite with " + testCaseParameters.size() + " test cases.");
        return testCaseParameters;
    }

    public static Collection<Object[]> parametersFromSubManifest(String subManifestFilePath, ImmutableSet<String> ignoredTests,
                                                                 RepositoryRegistry registry) throws Exception {
        return parametersFromSubManifest(ManifestTestUtils.class.getResource(subManifestFilePath),
                true, ignoredTests, registry);
    }

    public static Collection<Object[]> parametersFromSubManifest(URL subManifestFileURL, boolean approvedOnly,
                                                                 ImmutableSet<String> ignoredTests,
                                                                 RepositoryRegistry registry) throws Exception {
        LOGGER.info("Building test for {}", subManifestFileURL);

        List<Object[]> testCaseParameters = Lists.newArrayList();

        // Read manifest and create declared test cases
        Repository manifestRep = new SailRepository(new MemoryStore());
        manifestRep.init();
        RepositoryConnection con = manifestRep.getConnection();

        String manifestName = getManifestName(manifestRep, con, subManifestFileURL.toString());

        addTurtle(con, subManifestFileURL, subManifestFileURL.toString());

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
        query.append(" ?action qt:query ?queryFile . } \n");
        TupleQuery testCaseQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query.toString());

        LOGGER.debug("Evaluating query..");
        TupleQueryResult testCases = testCaseQuery.evaluate();
        while (testCases.hasNext()) {
            BindingSet bindingSet = testCases.next();

            //URI testURI = (URI) bindingSet.getValue("testURI");
            String testName = manifestName + "-" + bindingSet.getValue("testName").stringValue();
            String resultFile = bindingSet.getValue("resultFile").toString();
            String queryFile = bindingSet.getValue("queryFile").toString();
            String owlFile = bindingSet.getValue("owlFile").toString();
            String obdaFile = bindingSet.getValue("obdaFile").toString();
            String parameterFile = (bindingSet.getValue("parameterFile") == null
                    ? "" : bindingSet.getValue("parameterFile").toString());

            LOGGER.debug("Found test case: {}", testName);

            testCaseParameters.add(
                    new Object[] {testName, queryFile,
                            resultFile, owlFile, obdaFile, parameterFile, registry, ignoredTests});
        }

        testCases.close();
        con.close();

        manifestRep.shutDown();
        LOGGER.info("Created test with " + testCaseParameters.size()  + " test cases.");
        return testCaseParameters;
    }

    protected static String getManifestName(Repository manifestRep, RepositoryConnection con, String manifestFileURL)
            throws QueryEvaluationException, RepositoryException, MalformedQueryException
    {
        // Try to extract suite name from manifest file
        TupleQuery manifestNameQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
                "SELECT ?ManifestName WHERE { ?ManifestURL rdfs:label ?ManifestName .}");
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
