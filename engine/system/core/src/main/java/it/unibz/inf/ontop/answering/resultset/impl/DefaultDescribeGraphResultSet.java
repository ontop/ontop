package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import it.unibz.inf.ontop.answering.connection.impl.Evaluator;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructQuery;
import it.unibz.inf.ontop.answering.reformulation.input.DescribeQuery;
import it.unibz.inf.ontop.answering.reformulation.input.SelectQuery;
import it.unibz.inf.ontop.answering.resultset.*;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nullable;

public class DefaultDescribeGraphResultSet implements GraphResultSet {
    private final ResultSetIterator iterator;

    public DefaultDescribeGraphResultSet(DescribeQuery describeQuery, QueryLogger queryLogger,
                                         QueryLogger.Factory queryLoggerFactory,
                                         Evaluator<TupleResultSet, SelectQuery> selectQueryEvaluator,
                                         Evaluator<GraphResultSet, ConstructQuery> constructQueryEvaluator,
                                         OntopConnectionCloseable statementClosingCB)
            throws OntopQueryEvaluationException, OntopConnectionException, OntopReformulationException,
            OntopResultConversionException {

        ImmutableSet<IRI> resourcesToDescribe = extractDescribeResources(describeQuery, queryLogger, queryLoggerFactory,
                selectQueryEvaluator);

        this.iterator = new ResultSetIterator(describeQuery.computeConstructQueries(resourcesToDescribe),
                queryLogger, queryLoggerFactory, constructQueryEvaluator, statementClosingCB);

    }

    private static ImmutableSet<IRI> extractDescribeResources(DescribeQuery inputQuery,
                                                              QueryLogger queryLogger, QueryLogger.Factory queryLoggerFactory,
                                                              Evaluator<TupleResultSet, SelectQuery> selectQueryEvaluator)
            throws OntopQueryEvaluationException, OntopConnectionException,
            OntopReformulationException, OntopResultConversionException {
        QueryLogger selectQueryLogger = queryLoggerFactory.create(ImmutableMultimap.of());
        try (TupleResultSet resultSet = selectQueryEvaluator.evaluate(inputQuery.getSelectQuery(), selectQueryLogger)) {
            queryLogger.declareResultSetUnblockedAndSerialize();

            ImmutableSet.Builder<IRI> iriSetBuilder = ImmutableSet.builder();
            while (resultSet.hasNext()) {
                final OntopBindingSet bindingSet = resultSet.next();
                for (OntopBinding binding : bindingSet.getBindings()) {
                    RDFConstant value = binding.getValue();
                    // Ignores blank-nodes and literals
                    if (value instanceof IRIConstant) {
                        iriSetBuilder.add(((IRIConstant) value).getIRI());
                    }
                }
            }
            return iriSetBuilder.build();
            // Exception is re-cast because not due to the initial input query
        } catch (OntopInvalidInputQueryException e) {
            throw new OntopReformulationException(e);
        }
    }

    @Override
    public boolean hasNext() throws OntopConnectionException, OntopQueryAnsweringException {
        return iterator.hasNext();
    }

    @Override
    public RDFFact next() throws OntopQueryAnsweringException, OntopConnectionException {
        return iterator.next();
    }

    @Override
    public OntopCloseableIterator<RDFFact, OntopConnectionException> iterator() {
        return iterator;
    }

    @Override
    public void close() throws OntopConnectionException {
        iterator.close();
    }

    protected static class ResultSetIterator extends RDFFactCloseableIterator {

        private final UnmodifiableIterator<ConstructQuery> constructQueryIterator;
        private final OntopConnectionCloseable statementClosingCB;
        private final QueryLogger queryLogger;
        private final QueryLogger.Factory queryLoggerFactory;
        private final Evaluator<GraphResultSet, ConstructQuery> constructQueryEvaluator;
        private long rowCount;

        @Nullable
        private OntopCloseableIterator<RDFFact, OntopConnectionException> currentGraphResultSetIterator;

        public ResultSetIterator(ImmutableCollection<ConstructQuery> constructQueries,
                                 QueryLogger queryLogger, QueryLogger.Factory queryLoggerFactory,
                                 Evaluator<GraphResultSet, ConstructQuery> constructQueryEvaluator,
                                 OntopConnectionCloseable statementClosingCB) {

            this.constructQueryIterator = constructQueries.iterator();
            this.statementClosingCB = statementClosingCB;
            this.currentGraphResultSetIterator = null;
            this.queryLogger = queryLogger;
            this.queryLoggerFactory = queryLoggerFactory;
            this.constructQueryEvaluator = constructQueryEvaluator;
            this.rowCount = 0;
        }

        @Override
        public boolean hasNext() throws OntopConnectionException, OntopResultConversionException {
            do {
                if (currentGraphResultSetIterator == null) {
                    if (constructQueryIterator.hasNext()) {
                        QueryLogger constructQueryLogger = queryLoggerFactory.create(ImmutableMultimap.of());
                        try {
                            GraphResultSet graphResultSet = constructQueryEvaluator.evaluate(constructQueryIterator.next(), constructQueryLogger);
                            currentGraphResultSetIterator = graphResultSet.iterator();
                        } catch (OntopQueryEvaluationException e) {
                            throw new LateQueryEvaluationExceptionWhenDescribing(e);
                        } catch (OntopReformulationException e) {
                            throw new LateQueryReformulationExceptionWhenDescribing(e);
                        }
                    }
                    else {
                        queryLogger.declareLastResultRetrievedAndSerialize(rowCount);
                        handleClose();
                        return false;
                    }
                }
                if (currentGraphResultSetIterator.hasNext()) {
                    rowCount++;
                    return true;
                }
                else {
                    currentGraphResultSetIterator.close();
                    currentGraphResultSetIterator = null;
                }
            } while (true);
        }

        @Override
        public RDFFact next() throws OntopConnectionException {
            if (currentGraphResultSetIterator == null)
                throw new IllegalStateException("Make sure to call hasNext() before calling next()");
            return currentGraphResultSetIterator.next();
        }

        @Override
        protected void handleClose() throws OntopConnectionException {
            if (currentGraphResultSetIterator != null)
                currentGraphResultSetIterator.close();
            statementClosingCB.close();
        }

        /**
         * Seeing as an connection exception as the processed query is at a lower level
         * (like if it was connecting to another system)
         */
        private static class LateQueryEvaluationExceptionWhenDescribing extends OntopConnectionException {
            private LateQueryEvaluationExceptionWhenDescribing(OntopQueryEvaluationException e) {
                super(e);
            }
        }

        /**
         * Seeing as an connection exception as the processed query is at a lower level
         * (like if it was connecting to another system)
         */
        private static class LateQueryReformulationExceptionWhenDescribing extends OntopConnectionException {
            private LateQueryReformulationExceptionWhenDescribing(OntopReformulationException e) {
                super(e);
            }
        }
    }

}
