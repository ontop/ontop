package it.unibz.inf.ontop.docker.utils;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.TestExecutor;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import junit.framework.TestCase;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.junit.Assume.assumeTrue;

public abstract class OntopTestCase extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntopTestCase.class);
    private final String queryFileURL;
    private final String resultFileURL;
    private final String owlFileURL;
    private final String obdaFileURL;
    private final String parameterFileURL;
    private final RepositoryRegistry registry;
    private final ImmutableSet<String> ignoredTests;

    public OntopTestCase(String name, String queryFileURL, String resultFileURL, String owlFileURL,
                         String obdaFileURL, String parameterFileURL,
                         RepositoryRegistry registry, ImmutableSet<String> ignoredTests) {
        super(name);
        this.queryFileURL = queryFileURL;
        this.resultFileURL = resultFileURL;
        this.owlFileURL = owlFileURL;
        this.obdaFileURL = obdaFileURL;
        this.parameterFileURL = parameterFileURL;
        this.registry = registry;
        this.ignoredTests = ignoredTests;
    }

    private Repository createRepository() throws RepositoryException {

        // Already existing
        Optional<Repository> optionalRepository = registry.getRepository(owlFileURL, obdaFileURL, parameterFileURL);
        if (optionalRepository.isPresent())
            return optionalRepository.get();


        OntopSQLOWLAPIConfiguration.Builder configBuilder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(owlFileURL)
                .enableTestMode()
                .nativeOntopMappingFile(obdaFileURL);

        if (parameterFileURL != null && (!parameterFileURL.isEmpty())) {
            configBuilder.propertyFile(parameterFileURL);
        }

        OntopRepository repo = OntopRepository.defaultRepository(configBuilder.build());
        repo.initialize();

        registry.register(repo, owlFileURL, obdaFileURL, parameterFileURL);
        return repo;
    }

    @Test
    @Override
    public void runTest() throws Exception {
        assumeTrue(!ignoredTests.contains(getName()));

        Repository dataRep = createRepository();

        TestExecutor executor = new TestExecutor(getName(), queryFileURL, resultFileURL, dataRep, LOGGER);
        executor.runTest();
    }
}
