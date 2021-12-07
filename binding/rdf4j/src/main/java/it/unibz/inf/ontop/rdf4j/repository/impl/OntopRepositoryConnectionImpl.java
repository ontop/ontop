package it.unibz.inf.ontop.rdf4j.repository.impl;

import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JInputQueryFactory;
import it.unibz.inf.ontop.answering.reformulation.input.SPARQLQuery;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.injection.OntopSystemSettings;
import it.unibz.inf.ontop.rdf4j.query.impl.*;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepositoryConnection;
import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.NamespaceImpl;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.parser.*;
import org.eclipse.rdf4j.queryrender.RenderUtils;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.*;

public class OntopRepositoryConnectionImpl implements OntopRepositoryConnection {

    private static final String READ_ONLY_MESSAGE = "Ontop is a read-only system";
    private static Logger LOGGER = LoggerFactory.getLogger(OntopRepositoryConnectionImpl.class);
    private OntopRepository repository;
    private OntopConnection ontopConnection;
    private final RDF4JInputQueryFactory inputQueryFactory;
    private final OntopSystemSettings settings;
    private boolean isOpen;
    private boolean isActive;
    private RDFParser rdfParser;
    private Map<String, String> namespaces;


    OntopRepositoryConnectionImpl(OntopRepository rep, OntopConnection connection,
                                  RDF4JInputQueryFactory inputQueryFactory, OntopSystemSettings settings) {
        this.repository = rep;
        this.ontopConnection = connection;
        this.inputQueryFactory = inputQueryFactory;
        this.settings = settings;
        this.isOpen = true;
        this.isActive = false;
        this.rdfParser = Rio.createParser(RDFFormat.RDFXML, this.repository.getValueFactory());
        this.namespaces = new HashMap<>();
    }


    @Override
    public void add(Statement st, Resource... contexts) throws RepositoryException {
        throw new RepositoryException(READ_ONLY_MESSAGE);
    }

    @Override
    public void add(Iterable<? extends Statement> statements, Resource... contexts)
            throws RepositoryException {
        throw new RepositoryException(READ_ONLY_MESSAGE);
    }


    @Override
    public void add(File file, String baseIRI, RDFFormat dataFormat, Resource... contexts)
            throws IOException, RDFParseException, RepositoryException {
        throw new RepositoryException(READ_ONLY_MESSAGE);
    }

    @Override
    public void add(URL url, String baseIRI, RDFFormat dataFormat,
                    Resource... contexts) throws IOException,
            RDFParseException, RepositoryException {
        throw new RepositoryException(READ_ONLY_MESSAGE);
    }

    @Override
    public void add(InputStream in, String baseIRI,
                    RDFFormat dataFormat, Resource... contexts)
            throws IOException, RDFParseException, RepositoryException {
        throw new RepositoryException(READ_ONLY_MESSAGE);
    }

    @Override
    public void add(Reader reader, String baseIRI,
                    RDFFormat dataFormat, Resource... contexts)
            throws IOException, RDFParseException, RepositoryException {
        throw new RepositoryException(READ_ONLY_MESSAGE);
    }

    @Override
    public void add(Resource subject, org.eclipse.rdf4j.model.IRI predicate, Value object, Resource... contexts)
            throws RepositoryException {
        throw new RepositoryException(READ_ONLY_MESSAGE);
    }


    @Override
    public void clear(Resource... contexts) throws RepositoryException {
        throw new RepositoryException(READ_ONLY_MESSAGE);
    }

    @Override
    public void clearNamespaces() throws RepositoryException {
        //Removes all namespace declarations from the repository.
        remove(null, null, null, (Resource[]) null);

    }

    @Override
    public void close() throws RepositoryException {
        //Closes the connection, freeing resources.
        //If the connection is not in autoCommit mode,
        //all non-committed operations will be lost.
        isOpen = false;
        try {
            ontopConnection.close();
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void commit() throws RepositoryException {
        // Commits all updates that have been performed as part of this
        // connection sofar.
        if (isActive()) {
            try {
                // System.out.println("QuestConn commit..");
                ontopConnection.commit();
                this.isActive = false;
            } catch (OntopConnectionException e) {
                throw new RepositoryException(e);
            }
        } else {
            throw new RepositoryException(
                    "Connection does not have an active transaction.");
        }
    }

    @Override
    public void export(RDFHandler handler, Resource... contexts)
            throws RepositoryException, RDFHandlerException {
        //Exports all explicit statements in the specified contexts to the supplied RDFHandler.
        exportStatements(null, null, null, false, handler, contexts);
    }

    @Override
    public void exportStatements(Resource subj, IRI pred, Value obj,
                                 boolean includeInferred, RDFHandler handler, Resource... contexts)
            throws RepositoryException, RDFHandlerException {
        //Exports all statements with a specific subject, predicate
        //and/or object from the repository, optionally from the specified contexts.
        RepositoryResult<Statement> stms = getStatements(subj, pred, obj, includeInferred, contexts);

        handler.startRDF();
        // handle
        if (stms != null) {
            while (stms.hasNext()) {
                Statement st = stms.next();
                if (st != null)
                    handler.handleStatement(st);
            }
        }
        handler.endRDF();

    }

    @Override
    public RepositoryResult<Resource> getContextIDs()
            throws RepositoryException {
        //Gets all resources that are used as content identifiers.
        //Care should be taken that the returned RepositoryResult
        //is closed to free any resources that it keeps hold of.
        List<Resource> contexts = new LinkedList<Resource>();
        return new RepositoryResult<Resource>(new CloseableIteratorIteration<Resource, RepositoryException>(contexts.iterator()));
    }

    @Override
    public String getNamespace(String prefix) throws RepositoryException {
        //Gets the namespace that is associated with the specified prefix, if any.
        return namespaces.get(prefix);
    }

    @Override
    public RepositoryResult<Namespace> getNamespaces()
            throws RepositoryException {
        //Gets all declared namespaces as a RepositoryResult of Namespace objects.
        //Each Namespace object consists of a prefix and a namespace name.
        Set<Namespace> namespSet = new HashSet<Namespace>();
        Map<String, String> namesp = namespaces;
        Set<String> keys = namesp.keySet();
        for (String key : keys) {
            //convert into namespace objects
            namespSet.add(new NamespaceImpl(key, namesp.get(key)));
        }
        return new RepositoryResult<Namespace>(new CloseableIteratorIteration<Namespace, RepositoryException>(
                namespSet.iterator()));
    }

    @Override
    public ParserConfig getParserConfig() {
        //Returns the parser configuration this connection uses for Rio-based operations.
        return rdfParser.getParserConfig();
    }

    @Override
    public Repository getRepository() {
        //Returns the Repository object to which this connection belongs.
        return this.repository;
    }

    @Override
    public RepositoryResult<Statement> getStatements(Resource subj, org.eclipse.rdf4j.model.IRI pred,
                                                     Value obj, boolean includeInferred, Resource... contexts)
            throws RepositoryException {
        //Gets all statements with a specific subject,
        //predicate and/or object from the repository.
        //The result is optionally restricted to the specified set of named contexts.

        //construct query for it
        StringBuilder queryString = new StringBuilder("CONSTRUCT {");

        StringBuilder spo = subj == null ? new StringBuilder("?s ") : RenderUtils.toSPARQL(subj, new StringBuilder());

        spo = pred == null ? spo.append(" ?p ") : RenderUtils.toSPARQL(pred, spo);

        spo = obj == null ? spo.append(" ?o ") : RenderUtils.toSPARQL(obj, spo);

        queryString.append(spo).append("} WHERE {").append(spo).append("}");

        //execute construct query
        try {
            if (contexts.length == 0 || (contexts.length > 0 && contexts[0] == null)) {
                GraphQuery query = prepareGraphQuery(QueryLanguage.SPARQL,
                        queryString.toString());
                GraphQueryResult result = query.evaluate();
                return new RepositoryResult<>(new CloseableIteratorIteration<>(result.iterator()));
            }
            return new RepositoryResult<>(new CloseableIteratorIteration<>());
        } catch (MalformedQueryException | QueryEvaluationException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public ValueFactory getValueFactory() {
        //Gets a ValueFactory for this OntopRepositoryConnection.
        return new ValueFactoryImpl();
    }

    @Override
    public boolean hasStatement(Statement st, boolean includeInferred, Resource... contexts)
            throws RepositoryException {
        //Checks whether the repository contains the specified statement,
        //optionally in the specified contexts.
        return hasStatement(st.getSubject(), st.getPredicate(), st
                .getObject(), includeInferred, contexts);
    }

    @Override
    public boolean hasStatement(Resource subj, org.eclipse.rdf4j.model.IRI pred, Value obj,
                                boolean includeInferred, Resource... contexts) throws RepositoryException {
        //Checks whether the repository contains statements with a specific subject,
        //predicate and/or object, optionally in the specified contexts.
        RepositoryResult<Statement> stIter = getStatements(subj, pred,
                obj, includeInferred, contexts);
        try {
            return stIter.hasNext();
        } finally {
            stIter.close();
        }
    }


    @Override
    public boolean isAutoCommit() throws RepositoryException {
        //Checks whether the connection is in auto-commit mode.
        try {
            return ontopConnection.getAutoCommit();
        } catch (OntopConnectionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean isEmpty() throws RepositoryException {
        //Returns true if this repository does not contain any (explicit) statements.
        return size() == 0;
    }

    @Override
    public boolean isOpen() throws RepositoryException {
        //Checks whether this connection is open.
        //A connection is open from the moment it is created until it is closed.
        return this.isOpen;
    }

    @Override
    public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query)
            throws RepositoryException, MalformedQueryException {
        //Prepares true/false queries. In case the query contains
        //relative IRIs that need to be resolved against an external base IRI,
        //one should use prepareBooleanQuery(QueryLanguage, String, String) instead.
        return prepareBooleanQuery(ql, query, null);
    }

    @Override
    public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String queryString,
                                            String baseIRI) throws RepositoryException, MalformedQueryException {
        return prepareBooleanQuery(ql, queryString, baseIRI, ImmutableMultimap.of());
    }

    @Override
    public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String queryString,
                String baseIRI, ImmutableMultimap<String, String> httpHeaders) throws RepositoryException, MalformedQueryException {
        //Prepares true/false queries.
        if (ql != QueryLanguage.SPARQL)
            throw new MalformedQueryException("SPARQL query expected!");

        String safeBaseIRI = baseIRI == null
                ? null
                : baseIRI.isEmpty() ? null : baseIRI;

        ParsedQuery q = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, queryString, safeBaseIRI);
        return new OntopBooleanQuery(queryString, q, safeBaseIRI, ontopConnection, httpHeaders, inputQueryFactory, settings);
    }

    @Override
    public GraphQuery prepareGraphQuery(QueryLanguage ql, String queryString)
            throws RepositoryException, MalformedQueryException {
        //Prepares queries that produce RDF graphs. In case the query
        //contains relative IRIs that need to be resolved against an
        //external base IRI, one should use prepareGraphQuery(QueryLanguage, String, String) instead.
        return prepareGraphQuery(ql, queryString, null);
    }

    @Override
    public GraphQuery prepareGraphQuery(QueryLanguage ql, String queryString, String baseIRI)
            throws RepositoryException, MalformedQueryException {
        return prepareGraphQuery(ql, queryString, baseIRI, ImmutableMultimap.of());
    }

    @Override
    public GraphQuery prepareGraphQuery(QueryLanguage ql, String queryString,
                                        String baseIRI, ImmutableMultimap<String, String> httpHeaders)
            throws RepositoryException, MalformedQueryException {
        //Prepares queries that produce RDF graphs.
        if (ql != QueryLanguage.SPARQL)
            throw new MalformedQueryException("SPARQL query expected!");

        String safeBaseIRI = baseIRI == null
                ? null
                : baseIRI.isEmpty() ? null : baseIRI;

        ParsedQuery q = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, queryString, safeBaseIRI);
        return new OntopGraphQuery(queryString, q, safeBaseIRI, ontopConnection, httpHeaders, inputQueryFactory, settings);

    }

    @Override
    public Query prepareQuery(QueryLanguage ql, String query)
            throws RepositoryException, MalformedQueryException {
        //Prepares a query for evaluation on this repository (optional operation).
        //In case the query contains relative IRIs that need to be resolved against
        //an external base IRI, one should use prepareQuery(QueryLanguage, String, String) instead.
        return prepareTupleQuery(ql, query, null, ImmutableMultimap.of());
    }

    @Override
    public Query prepareQuery(QueryLanguage ql, String query, ImmutableMultimap<String, String> httpHeaders)
            throws RepositoryException, MalformedQueryException {
        //Prepares a query for evaluation on this repository (optional operation).
        //In case the query contains relative IRIs that need to be resolved against
        //an external base IRI, one should use prepareQuery(QueryLanguage, String, String) instead.
        return prepareQuery(ql, query, null, httpHeaders);
    }

    @Override
    public Query prepareQuery(QueryLanguage ql, String queryString, String baseIRI)
            throws RepositoryException, MalformedQueryException {
        return prepareQuery(ql, queryString, baseIRI, ImmutableMultimap.of());
    }

    @Override
    public Query prepareQuery(QueryLanguage ql, String queryString, String baseIRI,
                              ImmutableMultimap<String, String> httpHeaders)
            throws RepositoryException, MalformedQueryException {
        if (ql != QueryLanguage.SPARQL)
            throw new MalformedQueryException("SPARQL query expected! ");

        long beforeParsing = System.currentTimeMillis();
        ParsedQuery q = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, queryString, baseIRI);
        LOGGER.debug(String.format("Parsing time: %d ms", System.currentTimeMillis() - beforeParsing));

        if (q instanceof ParsedTupleQuery)
            return new OntopTupleQuery(queryString, q, baseIRI, ontopConnection, httpHeaders, inputQueryFactory, settings);
        else if (q instanceof ParsedBooleanQuery)
            return new OntopBooleanQuery(queryString, q, baseIRI, ontopConnection, httpHeaders, inputQueryFactory, settings);
        else if (q instanceof ParsedGraphQuery)
            return new OntopGraphQuery(queryString, q, baseIRI, ontopConnection, httpHeaders, inputQueryFactory, settings);
        else
            throw new MalformedQueryException("Unrecognized query type. " + queryString);
    }

    @Override
    public TupleQuery prepareTupleQuery(QueryLanguage ql, String query)
            throws RepositoryException, MalformedQueryException {
        //Prepares a query that produces sets of value tuples.
        //In case the query contains relative IRIs that need to be
        //resolved against an external base IRI, one should use
        //prepareTupleQuery(QueryLanguage, String, String) instead.
        return this.prepareTupleQuery(ql, query, "");
    }

    @Override
    public TupleQuery prepareTupleQuery(QueryLanguage ql, String queryString, String baseIRI)
            throws RepositoryException, MalformedQueryException {
        return prepareTupleQuery(ql, queryString, baseIRI, ImmutableMultimap.of());
    }

    @Override
    public TupleQuery prepareTupleQuery(QueryLanguage ql, String queryString, String baseIRI,
                                        ImmutableMultimap<String, String> httpHeaders)
            throws RepositoryException, MalformedQueryException {
        //Prepares a query that produces sets of value tuples.
        if (ql != QueryLanguage.SPARQL)
            throw new MalformedQueryException("SPARQL query expected!");

        String safeBaseIRI = baseIRI == null
                ? null
                : baseIRI.isEmpty() ? null : baseIRI;
        ParsedQuery q = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, queryString, safeBaseIRI);

        return new OntopTupleQuery(queryString, q, safeBaseIRI, ontopConnection, httpHeaders, inputQueryFactory, settings);
    }

    @Override
    public Update prepareUpdate(QueryLanguage arg0, String arg1)
            throws RepositoryException, MalformedQueryException {
        throw new RepositoryException(READ_ONLY_MESSAGE);
    }

    @Override
    public Update prepareUpdate(QueryLanguage arg0, String arg1, String arg2)
            throws RepositoryException, MalformedQueryException {
        throw new RepositoryException(READ_ONLY_MESSAGE);
    }

    @Override
    public void remove(Statement st, Resource... contexts)
            throws RepositoryException {
        throw new RepositoryException(READ_ONLY_MESSAGE);
    }

    @Override
    public void remove(Iterable<? extends Statement> statements, Resource... contexts)
            throws RepositoryException {
        throw new RepositoryException(READ_ONLY_MESSAGE);
    }


    @Override
    public void remove(Resource subject, org.eclipse.rdf4j.model.IRI predicate, Value object, Resource... contexts)
            throws RepositoryException {
        throw new RepositoryException(READ_ONLY_MESSAGE);
    }

    @Override
    public void removeNamespace(String key) throws RepositoryException {
        //Removes a namespace declaration by removing the association between a prefix and a namespace name.
        namespaces.remove(key);
    }

    @Override
    public void rollback() throws RepositoryException {
        // Rolls back all updates that have been performed as part of this
        // connection sofar.
        if (isActive()) {
            try {
                this.ontopConnection.rollBack();
                this.isActive = false;
            } catch (OntopConnectionException e) {
                throw new RepositoryException(e);
            }
        } else {
            throw new RepositoryException(
                    "Connection does not have an active transaction.");
        }
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws RepositoryException {
        //Enables or disables auto-commit mode for the connection.
        //If a connection is in auto-commit mode, then all updates
        //will be executed and committed as individual transactions.
        //Otherwise, the updates are grouped into transactions that are
        // terminated by a call to either commit() or rollback().
        // By default, new connections are in auto-commit mode.
        try {
            if (autoCommit == this.ontopConnection.getAutoCommit()) {
                return;
            }
        } catch (OntopConnectionException e) {
            throw new RepositoryException(e);
        }
        if (isActive()) {
            try {
                this.ontopConnection.setAutoCommit(autoCommit);
            } catch (OntopConnectionException e) {
                throw new RepositoryException(e);

            }

            // if we are switching from non-autoCommit to autoCommit mode,
            // commit any
            // pending updates
            if (autoCommit) {
                commit();
            }
        } else if (!autoCommit) {
            // begin a transaction
            begin();
        }

    }

    @Override
    public void setNamespace(String key, String value)
            throws RepositoryException {
        //Sets the prefix for a namespace.
        namespaces.put(key, value);

    }

    @Override
    public void setParserConfig(ParserConfig config) {
        //Set the parser configuration this connection should use for RDFParser-based operations.
        rdfParser.setParserConfig(config);
    }

    @Override
    public long size(Resource... contexts) throws RepositoryException {
        //Returns the number of (explicit) statements that are in the specified contexts in this repository.
        return 0;
    }


    /**
     * Call this method to start a transaction. Have to call commit() or
     * rollback() to mark end of transaction.
     */
    @Override
    public void begin() throws RepositoryException {
        // TODO Auto-generated method stub
        if (!isOpen()) {
            throw new RepositoryException("Connection was closed.");
        }
        isActive = true;
    }


    /**
     * A boolean flag signaling when a transaction is active.
     */
    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public void setIsolationLevel(IsolationLevel level) throws IllegalStateException {
        if (level != IsolationLevels.NONE)
            throw new UnsupportedOperationException();
    }

    @Override
    public IsolationLevel getIsolationLevel() {
        return IsolationLevels.NONE;
    }

    @Override
    public void begin(IsolationLevel level) throws RepositoryException {
        // do nothing
    }


    @Override
    public <E extends Exception> void add(
            org.eclipse.rdf4j.common.iteration.Iteration<? extends Statement, E> statements, Resource... contexts)
            throws RepositoryException, E {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public <E extends Exception> void remove(
            org.eclipse.rdf4j.common.iteration.Iteration<? extends Statement, E> statements, Resource... contexts)
            throws RepositoryException, E {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public String reformulate(String sparql)
            throws RepositoryException {
        try {
            SPARQLQuery sparqlQuery = ontopConnection.getInputQueryFactory().createSPARQLQuery(sparql);
            return ontopConnection.createStatement().getExecutableQuery(sparqlQuery).toString();
        } catch (OntopReformulationException | OntopConnectionException e) {
            throw new RepositoryException(e);
        }
    }

}
