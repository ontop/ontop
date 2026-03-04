package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.query.KGQueryFactory;
import it.unibz.inf.ontop.query.SelectQuery;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.query.resultset.OntopBindingSet;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DefaultMaterializedGraphResultSet extends AbstractMaterializedGraphResultSet implements MaterializedGraphResultSet {

    private final TermFactory termFactory;
    private final KGQueryFactory kgQueryFactory;

    private final Logger LOGGER = LoggerFactory.getLogger(DefaultMaterializedGraphResultSet.class);
    private VocabularyEntry lastSeenPredicate;
    private IRIConstant lastSeenPredicateIRI;

    private final IRIConstant rdfTypeIRI;


    DefaultMaterializedGraphResultSet(ImmutableMap<IRI, VocabularyEntry> vocabulary, MaterializationParams params,
                                      OntopQueryEngine queryEngine,
                                      KGQueryFactory kgQueryFactory,
                                      TermFactory termFactory,
                                      QueryContext.Factory queryContextFactory) {
        super(vocabulary, params, queryEngine, queryContextFactory);
        this.termFactory = termFactory;
        this.kgQueryFactory = kgQueryFactory;

        tripleCounter = 0;
        queryCounter = 0;

        rdfTypeIRI = termFactory.getConstantIRI(RDF.TYPE.getIRIString());

        // Lately initiated
        ontopConnection = null;
        tmpStatement = null;
        tmpContextResultSet = null;
    }

    @Override
    public boolean hasNext() throws OntopQueryAnsweringException, OntopConnectionException {
        // Initialization
        if (ontopConnection == null)
            ontopConnection = queryEngine.getConnection();

        if ((tmpContextResultSet != null) && tmpContextResultSet.hasNext()) {
            return true;
        }

        // Davide> If there is no next, we need to go to the next vocabulary predicate

        while (vocabularyIterator.hasNext()) {
            /*
             * Closes the previous result set and statement (if open)
             */
            closeResource(tmpContextResultSet);
            closeResource(tmpStatement);

            /*
             * New query for the next RDF property/class
             */
            VocabularyEntry predicate = vocabularyIterator.next();

            try {
                SelectQuery query = kgQueryFactory.createSelectQuery(predicate.getSelectQuery());

                tmpStatement = ontopConnection.createStatement();
                tmpContextResultSet = tmpStatement.execute(query, queryContext);
                queryCounter ++;

                if (tmpContextResultSet.hasNext()) {
                    lastSeenPredicate = predicate;
                    lastSeenPredicateIRI = termFactory.getConstantIRI(lastSeenPredicate.getIRIString());

                    return true;
                }
            } catch (OntopQueryAnsweringException | OntopConnectionException e) {
                if (canBeIncomplete) {
                    LOGGER.warn("Possibly incomplete class/property {} (materialization problem).\nDetails: {}", predicate, e);
                    possiblyIncompleteClassesAndProperties.add(predicate.name);
                } else {
                    LOGGER.error("Problem materializing the class/property {}", predicate);
                    throw e;
                }
            } catch (OntopInvalidKGQueryException e) {
                throw new OntopInvalidInputQueryException(e.getMessage());
            }
        }

        return false;
    }

    /**
     * Builds (named) assertions out of (quad) results
     */
    private RDFFact toAssertion(OntopBindingSet tuple) throws OntopResultConversionException {
        ObjectConstant s = (ObjectConstant) tuple.getConstant("s");
        IRIConstant p = lastSeenPredicate.isClass() ? rdfTypeIRI : lastSeenPredicateIRI;
        RDFConstant o = lastSeenPredicate.isClass() ? lastSeenPredicateIRI : tuple.getConstant("o");
        ObjectConstant g = (ObjectConstant)tuple.getConstant("g");

        return (g == null)
                ? RDFFact.createTripleFact(s, p, o)
                : RDFFact.createQuadFact(s, p, o, g);
    }

    @Override
    public RDFFact next() throws OntopQueryAnsweringException {
        tripleCounter++;

        OntopBindingSet resultTuple;
        try {
            resultTuple = tmpContextResultSet.next();
            return toAssertion(resultTuple);
        } catch (OntopConnectionException e) {
            getLogger().error("Connection error while retrieving next RDF fact", e);
            closeResource(tmpContextResultSet);
            closeResource(tmpStatement);
            throw new OntopQueryEvaluationException("Failed to retrieve next RDF fact", e);
        }
    }

    @Override
    Logger getLogger() {
        return LOGGER;
    }
}
