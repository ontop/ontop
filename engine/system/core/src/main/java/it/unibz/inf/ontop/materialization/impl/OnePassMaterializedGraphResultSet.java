package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.GeneralStructuralAndSemanticIQOptimizer;
import it.unibz.inf.ontop.iq.planner.QueryPlanner;
import it.unibz.inf.ontop.materialization.MappingAssertionInformation;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.query.resultset.OntopBindingSet;
import it.unibz.inf.ontop.query.resultset.OntopCloseableIterator;
import it.unibz.inf.ontop.query.resultset.TupleResultSet;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

public class OnePassMaterializedGraphResultSet implements MaterializedGraphResultSet {
    private final NativeQueryGenerator nativeQueryGenerator;
    private final AtomFactory atomFactory;
    private final IntermediateQueryFactory iqFactory;
    private final OntopQueryEngine queryEngine;
    private final GeneralStructuralAndSemanticIQOptimizer generalOptimizer;
    private final QueryPlanner queryPlanner;
    private final QueryLogger.Factory queryLoggerFactory;

    private final ImmutableMap<IRI, VocabularyEntry> vocabulary;
    private final Iterator<MappingAssertionInformation> mappingAssertionsIterator;
    private final boolean canBeIncomplete;
    private int counter;

    @Nullable
    private OntopConnection ontopConnection;
    @Nullable
    private OntopStatement tmpStatement;
    @Nullable
    private TupleResultSet tmpContextResultSet;
    @Nullable
    private RDFFactTemplates currentRDFFactTemplates;
    @Nullable
    private Iterator<RDFFact> tmpRDFFactsIterator;

    private final Logger LOGGER = LoggerFactory.getLogger(DefaultMaterializedGraphResultSet.class);
    private final List<IRI> possiblyIncompleteClassesAndProperties;

    OnePassMaterializedGraphResultSet(ImmutableMap<IRI, VocabularyEntry> vocabulary,
                                      ImmutableList<MappingAssertionInformation> assertionsInfo,
                                      MaterializationParams params,
                                      OntopQueryEngine queryEngine,
                                      NativeQueryGenerator nativeQueryGenerator,
                                      AtomFactory atomFactory,
                                      IntermediateQueryFactory iqFactory,
                                      GeneralStructuralAndSemanticIQOptimizer generalOptimizer,
                                      QueryPlanner queryPlanner,
                                      QueryLogger.Factory queryLogger) {
        this.vocabulary = vocabulary;
        this.mappingAssertionsIterator = assertionsInfo.stream().iterator();
        this.queryEngine = queryEngine;
        this.canBeIncomplete = params.canMaterializationBeIncomplete();
        this.nativeQueryGenerator = nativeQueryGenerator;
        this.atomFactory = atomFactory;
        this.iqFactory = iqFactory;
        this.generalOptimizer = generalOptimizer;
        this.queryPlanner = queryPlanner;
        this.queryLoggerFactory = queryLogger;

        this.possiblyIncompleteClassesAndProperties = new ArrayList<>();
        counter = 0;

        // Lately initialized
        ontopConnection = null;
        tmpStatement = null;
        tmpContextResultSet = null;
        currentRDFFactTemplates = null;
        tmpRDFFactsIterator = null;
    }

    @Override
    public ImmutableSet<IRI> getSelectedVocabulary() {
        return vocabulary.keySet();
    }

    @Override
    public boolean hasNext() throws OntopQueryAnsweringException, OntopConnectionException {
        if (ontopConnection == null)
            ontopConnection = queryEngine.getConnection();

        if (tmpRDFFactsIterator != null && tmpRDFFactsIterator.hasNext()) {
            return true;
        }

        if (tmpContextResultSet != null && tmpContextResultSet.hasNext()) {
            return true;
        }

        while (mappingAssertionsIterator.hasNext()) {
            if (tmpContextResultSet != null) {
                try {
                    tmpContextResultSet.close();
                } catch (OntopConnectionException e) {
                    LOGGER.warn("Non-critical exception while closing the graph result set: " + e);
                }
            }
            if (tmpStatement != null) {
                try {
                    tmpStatement.close();
                } catch (OntopConnectionException e) {
                    LOGGER.warn("Non-critical exception while closing the statement: " + e);
                }
            }

            MappingAssertionInformation assertionInfo = mappingAssertionsIterator.next();
            currentRDFFactTemplates = assertionInfo.getRDFFactTemplates();

            try {
                tmpStatement = ontopConnection.createStatement();
                QueryLogger queryLogger = queryLoggerFactory.create(ImmutableMap.of());
                IQ nativeQuery = translateIntoNativeQuery(assertionInfo, queryLogger);
                tmpContextResultSet = tmpStatement.executeSelectQuery(nativeQuery, queryLogger);

                if (tmpContextResultSet.hasNext()) {
                    return true;
                }
            } catch (OntopQueryAnsweringException | OntopConnectionException e) {
                if (canBeIncomplete) {
                    LOGGER.warn("Possibly incomplete class/property " + assertionInfo.getIQTree() + " (materialization problem).\n"
                            + "Details: " + e);
                } else {
                    LOGGER.error("Problem materializing " + assertionInfo.getIQTree());
                    throw e;
                }
            }
        }

        return false;
    }

    @Override
    public RDFFact next() throws OntopQueryAnsweringException {
        counter++;
        OntopBindingSet resultTuple;
        try {
            if (tmpRDFFactsIterator != null && tmpRDFFactsIterator.hasNext()) {
                return tmpRDFFactsIterator.next();
            }
            resultTuple = tmpContextResultSet.next();
            tmpRDFFactsIterator = toAssertions(resultTuple, currentRDFFactTemplates);
            while (!tmpRDFFactsIterator.hasNext()) {
                if (!tmpContextResultSet.hasNext()) {
                    MappingAssertionInformation assertionInfo = mappingAssertionsIterator.next();
                    currentRDFFactTemplates = assertionInfo.getRDFFactTemplates();

                    try {
                        tmpStatement = ontopConnection.createStatement();
                        QueryLogger queryLogger = queryLoggerFactory.create(ImmutableMap.of());
                        IQ nativeQuery = translateIntoNativeQuery(assertionInfo, queryLogger);
                        tmpContextResultSet = tmpStatement.executeSelectQuery(nativeQuery, queryLogger);
                    } catch (OntopConnectionException e) {
                        if (canBeIncomplete) {
                            LOGGER.warn("Possibly incomplete class/property " + assertionInfo.getIQTree() + " (materialization problem).\n"
                                    + "Details: " + e);
                        } else {
                            LOGGER.error("Problem materializing " + assertionInfo.getIQTree());
                            throw e;
                        }
                    }

                }
                resultTuple = tmpContextResultSet.next();
                tmpRDFFactsIterator = toAssertions(resultTuple, currentRDFFactTemplates);
            }
            return tmpRDFFactsIterator.next();
        } catch (OntopConnectionException e) {
            try {
                tmpContextResultSet.close();
            } catch (OntopConnectionException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public OntopCloseableIterator<RDFFact, OntopConnectionException> iterator() {
        throw new UnsupportedOperationException("iterator");
    }

    @Override
    public void close() throws OntopConnectionException {
        if (tmpStatement != null) {
            tmpStatement.close();
        }
        if (ontopConnection != null) {
            ontopConnection.close();
        }
    }

    @Override
    public long getTripleCountSoFar() {
        return counter;
    }

    @Override
    public ImmutableList<IRI> getPossiblyIncompleteRDFPropertiesAndClassesSoFar() {
        return ImmutableList.copyOf(possiblyIncompleteClassesAndProperties);
    }

    private IQ translateIntoNativeQuery(MappingAssertionInformation assertionInfo, QueryLogger queryLogger) {
        ImmutableList<Variable> variables = assertionInfo.getRDFFactTemplates().getVariables().asList();
        IQ tree = iqFactory.createIQ(
                atomFactory.getDistinctVariableOnlyDataAtom(atomFactory.getRDFAnswerPredicate(variables.size()),
                        variables),
                assertionInfo.getIQTree()
        );

        IQ optimizedQuery = generalOptimizer.optimize(tree, null);
        IQ plannedQuery = queryPlanner.optimize(optimizedQuery);
        IQ executableQuery = nativeQueryGenerator.generateSourceQuery(plannedQuery, true, true);
        queryLogger.declareReformulationFinishedAndSerialize(executableQuery, false);

        return executableQuery;
    }

    private Iterator<RDFFact> toAssertions(OntopBindingSet tuple, RDFFactTemplates templates) throws OntopResultConversionException {
        return templates.getTriplesOrQuadsVariables().stream()
                .filter(variables -> convertToRDFConstants(tuple, variables.subList(0, 3)).stream().allMatch(Optional::isPresent))
                .map(variables -> {
                    ImmutableList<Optional<RDFConstant>> tupleConstants = convertToRDFConstants(tuple, variables);
                    var subject = (ObjectConstant) tupleConstants.get(0).orElseThrow();
                    var predicate = (IRIConstant) tupleConstants.get(1).orElseThrow();
                    var object = tupleConstants.get(2).orElseThrow();
                    return tupleConstants.size() == 3
                            ? RDFFact.createTripleFact(subject, predicate, object)
                            : RDFFact.createQuadFact(
                                    subject, predicate, object, (ObjectConstant) tupleConstants.get(3).orElseThrow());
                }).iterator();
    }

    private ImmutableList<Optional<RDFConstant>> convertToRDFConstants(OntopBindingSet tuple, ImmutableList<Variable> variables) {
        return variables.stream()
                .map(variable -> {
                    try {
                        var constant = tuple.getConstant(variable.getName());
                        return constant == null ? Optional.<RDFConstant>empty() : Optional.of(constant);
                    } catch (OntopResultConversionException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(ImmutableList.toImmutableList());
    }
}
