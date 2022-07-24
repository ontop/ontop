package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableMultimap;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryException;

public interface OntopRepositoryConnection extends org.eclipse.rdf4j.repository.RepositoryConnection {

    Query prepareQuery(QueryLanguage ql, String query, ImmutableMultimap<String, String> httpHeaders)
            throws RepositoryException, MalformedQueryException;

    Query prepareQuery(QueryLanguage ql, String queryString, String baseIRI,
                       ImmutableMultimap<String, String> httpHeaders)
            throws RepositoryException, MalformedQueryException;

    TupleQuery prepareTupleQuery(QueryLanguage ql, String queryString, String baseIRI,
                                 ImmutableMultimap<String, String> httpHeaders)
            throws RepositoryException, MalformedQueryException;

    GraphQuery prepareGraphQuery(QueryLanguage ql, String queryString,
                                 String baseIRI, ImmutableMultimap<String, String> httpHeaders)
            throws RepositoryException, MalformedQueryException;

    BooleanQuery prepareBooleanQuery(QueryLanguage ql, String queryString,
                                     String baseIRI, ImmutableMultimap<String, String> httpHeaders)
            throws RepositoryException, MalformedQueryException;

    String reformulate(String sparql) throws RepositoryException;
}
