package it.unibz.inf.ontop.rdf4j.repository;

import it.unibz.inf.ontop.answering.cache.HTTPCacheHeaders;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import org.eclipse.rdf4j.repository.RepositoryException;

/**
 * Ontop RDF4J repository
 */
public interface OntopRepository extends org.eclipse.rdf4j.repository.Repository, AutoCloseable {

    HTTPCacheHeaders getHttpCacheHeaders();

    @Override
    OntopRepositoryConnection getConnection() throws RepositoryException;

    static OntopVirtualRepository defaultRepository(OntopSystemConfiguration configuration) {
        return new OntopVirtualRepository(configuration);
    }
}
