package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.query.resultset.OntopCloseableIterator;
import it.unibz.inf.ontop.query.resultset.TupleResultSet;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMaterializedGraphResultSet implements MaterializedGraphResultSet {

    protected final OntopQueryEngine queryEngine;
    protected final QueryContext queryContext;
    protected final ImmutableMap<IRI, VocabularyEntry> vocabulary;

    protected final UnmodifiableIterator<VocabularyEntry> vocabularyIterator;
    protected final boolean canBeIncomplete;
    protected int tripleCounter;
    protected int queryCounter;

    @Nullable
    protected OntopConnection ontopConnection;
    @Nullable
    protected OntopStatement tmpStatement;
    @Nullable
    protected TupleResultSet tmpContextResultSet;

    protected final List<IRI> possiblyIncompleteClassesAndProperties;

    AbstractMaterializedGraphResultSet(ImmutableMap<IRI, VocabularyEntry> vocabulary, MaterializationParams params,
                                OntopQueryEngine queryEngine, QueryContext.Factory queryContextFactory) {
        this.queryEngine = queryEngine;
        this.vocabulary = vocabulary;
        this.vocabularyIterator = vocabulary.values().iterator();
        this.queryContext = queryContextFactory.create(ImmutableMap.of());
        this.canBeIncomplete = params.canMaterializationBeIncomplete();

        this.possiblyIncompleteClassesAndProperties = new ArrayList<>();
        this.tripleCounter = 0;
        this.queryCounter = 0;

        // Lately initialized
        this.ontopConnection = null;
        this.tmpStatement = null;
        this.tmpContextResultSet = null;
    }

    abstract Logger getLogger();

    @Override
    public ImmutableSet<IRI> getSelectedVocabulary() {
        return vocabulary.keySet();
    }

    @Override
    public OntopCloseableIterator<RDFFact, OntopConnectionException> iterator() {
        throw new UnsupportedOperationException("iterator");
    }

    @Override
    public long getTripleCountSoFar() {
        return tripleCounter;
    }

    @Override
    public long getSQLQueryCountSoFar() {
        return queryCounter;
    }

    @Override
    public ImmutableList<IRI> getPossiblyIncompleteRDFPropertiesAndClassesSoFar() {
        return ImmutableList.copyOf(possiblyIncompleteClassesAndProperties);
    }

    /**
     * Releases all the connection resources
     */
    @Override
    public void close() throws OntopConnectionException {
        if (tmpStatement != null) {
            tmpStatement.close();
        }
        if (ontopConnection != null) {
            ontopConnection.close();
        }
    }

    /**
     * Closes a resource quietly, logging any errors but not throwing exceptions.
     * Useful for cleanup in exception handlers.
     */
    protected void closeResource(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                getLogger().warn("Error closing resource ", e);
            }
        }
    }
}
