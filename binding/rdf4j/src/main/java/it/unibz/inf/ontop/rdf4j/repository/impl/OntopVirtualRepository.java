package it.unibz.inf.ontop.rdf4j.repository.impl;

import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.cache.HTTPCacheHeaders;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JInputQueryFactory;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.injection.OntopSystemSettings;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepositoryConnection;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.base.AbstractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;

public class OntopVirtualRepository extends AbstractRepository implements OntopRepository {

    private static final Logger logger = LoggerFactory.getLogger(OntopVirtualRepository.class);
    private final OntopSystemSettings settings;

    // Temporary (dropped after initialization)
    @Nullable
    private OntopSystemConfiguration configuration;

    @Nullable
    private OntopQueryEngine queryEngine;
    private final RDF4JInputQueryFactory inputQueryFactory;
    private final HTTPCacheHeaders cacheHeaders;

    public OntopVirtualRepository(OntopSystemConfiguration configuration) {
        this.configuration = configuration;
        Injector injector = configuration.getInjector();
        inputQueryFactory = injector.getInstance(RDF4JInputQueryFactory.class);
        cacheHeaders = injector.getInstance(HTTPCacheHeaders.class);
        settings = configuration.getSettings();
    }

    /**
     * Returns a new RepositoryConnection.
     * <p>
     * (No repository connection sharing for the sake
     * of thread-safeness)
     */
    @Override
    public OntopRepositoryConnection getConnection() throws RepositoryException {
        if (!isInitialized()) {
            init();
        }

        try {
            return new OntopRepositoryConnectionImpl(this, getOntopConnection(), inputQueryFactory, settings);
        } catch (Exception e) {
            logger.error("Error creating repo connection: " + e.getMessage());
            throw new RepositoryException(e);
        }
    }


    /**
     * This method leads to the reasoner being initialized (connecting to the database,
     * analyzing mappings, etc.). .
     */
    @Override
    protected void initializeInternal() throws RepositoryException {
        try {
            queryEngine = configuration.loadQueryEngine();
            queryEngine.connect();
            logger.info("Ontop virtual repository initialized successfully!");
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    /**
     * Returns a connection which can be used to run queries over the repository
     * Before this method can be used, initialize() must be called once.
     */
    private OntopConnection getOntopConnection() throws RepositoryException {
        try {
            return queryEngine.getConnection();
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean isWritable() throws RepositoryException {
        return false;
    }

    @Override
    protected void shutDownInternal() throws RepositoryException {
        try {
            queryEngine.close();
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public File getDataDir() {
        throw new RepositoryException("Ontop does not have a data directory");
    }

    @Override
    public ValueFactory getValueFactory() {
        // Gets a ValueFactory for this Repository.
        return SimpleValueFactory.getInstance();
    }

    @Override
    public void setDataDir(File arg0) {
        // Ignores it
    }

    @Override
    public void close() throws RepositoryException {
        this.shutDown();
    }

    @Override
    public HTTPCacheHeaders getHttpCacheHeaders() {
        return cacheHeaders;
    }

    /**
     * Useful for the endpoints: allows to share the same query engine for the SPARQL and the predefined query endpoints
     */
    public OntopQueryEngine getOntopEngine() {
        if (!isInitialized()) {
            init();
        }
        return queryEngine;
    }
}
