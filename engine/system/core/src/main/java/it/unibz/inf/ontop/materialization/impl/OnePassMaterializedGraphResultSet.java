package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.optimizer.GeneralStructuralAndSemanticIQOptimizer;
import it.unibz.inf.ontop.iq.planner.QueryPlanner;
import it.unibz.inf.ontop.materialization.MappingEntryCluster;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.query.resultset.OntopBindingSet;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.impl.SubstitutionImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

public class OnePassMaterializedGraphResultSet extends AbstractMaterializedGraphResultSet implements MaterializedGraphResultSet {
    private final NativeQueryGenerator nativeQueryGenerator;
    private final AtomFactory atomFactory;
    private final IntermediateQueryFactory iqFactory;
    private final GeneralStructuralAndSemanticIQOptimizer generalOptimizer;
    private final QueryPlanner queryPlanner;
    private final QueryLogger.Factory queryLoggerFactory;
    private final Iterator<MappingEntryCluster> mappingClustersIterator;

    @Nullable
    private RDFFactTemplates currentRDFFactTemplates;
    @Nullable
    private Iterator<RDFFact> tmpRDFFactsIterator;

    private static final Logger LOGGER = LoggerFactory.getLogger(OnePassMaterializedGraphResultSet.class);

    OnePassMaterializedGraphResultSet(ImmutableMap<IRI, VocabularyEntry> vocabulary,
                                      ImmutableList<MappingEntryCluster> mappingEntryClusters,
                                      MaterializationParams params,
                                      OntopQueryEngine queryEngine,
                                      NativeQueryGenerator nativeQueryGenerator,
                                      AtomFactory atomFactory,
                                      IntermediateQueryFactory iqFactory,
                                      GeneralStructuralAndSemanticIQOptimizer generalOptimizer,
                                      QueryPlanner queryPlanner,
                                      QueryLogger.Factory queryLogger,
                                      QueryContext.Factory queryContextFactory) {
        super(vocabulary, params, queryEngine, queryContextFactory);
        this.nativeQueryGenerator = nativeQueryGenerator;
        this.atomFactory = atomFactory;
        this.iqFactory = iqFactory;
        this.generalOptimizer = generalOptimizer;
        this.queryPlanner = queryPlanner;
        this.queryLoggerFactory = queryLogger;
        this.mappingClustersIterator = mappingEntryClusters.stream().iterator();

        // Lately initialized
        tmpRDFFactsIterator = null;
    }

    @Override
    public boolean hasNext() throws OntopQueryAnsweringException, OntopConnectionException {
        if (ontopConnection == null) {
            ontopConnection = queryEngine.getConnection();
        }

        if (tmpRDFFactsIterator != null && tmpRDFFactsIterator.hasNext()) {
            return true;
        }

        if (tmpContextResultSet != null && tmpContextResultSet.hasNext()) {
            return true;
        }

        while (mappingClustersIterator.hasNext()) {
            closeResource(tmpContextResultSet);
            closeResource(tmpStatement);

            MappingEntryCluster mappingClusterEntry = mappingClustersIterator.next();
            currentRDFFactTemplates = mappingClusterEntry.getRDFFactTemplates();

            try {
                tmpStatement = ontopConnection.createStatement();
                QueryLogger queryLogger = queryLoggerFactory.create(queryContext);
                IQ nativeQuery = translateIntoNativeQuery(mappingClusterEntry, queryLogger, queryContext);
                tmpContextResultSet = tmpStatement.executeSelectQuery(nativeQuery, queryLogger);

                if (tmpContextResultSet.hasNext()) {
                    return true;
                }
            } catch (OntopQueryAnsweringException | OntopConnectionException e) {
                if (canBeIncomplete) {
                    ImmutableSet<IRI> incompleteClassesAndProperties = getIncompleteClassesAndProperties(mappingClusterEntry);
                    this.possiblyIncompleteClassesAndProperties.addAll(incompleteClassesAndProperties);
                    LOGGER.warn("Possibly incomplete class/property {} (materialization problem).\nDetails: {}",
                            incompleteClassesAndProperties, e);
                } else {
                    LOGGER.error("Problem materializing {}", mappingClusterEntry.getIQTree());
                    throw e;
                }
            }
        }

        return false;
    }

    @Override
    public RDFFact next() throws OntopQueryAnsweringException {
        tripleCounter++;
        OntopBindingSet resultTuple;
        try {
            if (tmpRDFFactsIterator != null && tmpRDFFactsIterator.hasNext()) {
                return tmpRDFFactsIterator.next();
            }
            resultTuple = tmpContextResultSet.next();
            tmpRDFFactsIterator = toRdfFacts(resultTuple, currentRDFFactTemplates);
            while (!tmpRDFFactsIterator.hasNext()) {
                if (!tmpContextResultSet.hasNext()) {
                    MappingEntryCluster mappingClusterEntry = mappingClustersIterator.next();
                    currentRDFFactTemplates = mappingClusterEntry.getRDFFactTemplates();

                    closeResource(tmpStatement);
                    try {
                        tmpStatement = ontopConnection.createStatement();
                        QueryLogger queryLogger = queryLoggerFactory.create(queryContext);
                        IQ nativeQuery = translateIntoNativeQuery(mappingClusterEntry, queryLogger, queryContext);
                        tmpContextResultSet = tmpStatement.executeSelectQuery(nativeQuery, queryLogger);
                    } catch (OntopConnectionException e) {
                        if (canBeIncomplete) {
                            ImmutableSet<IRI> incompleteClassesAndProperties = getIncompleteClassesAndProperties(mappingClusterEntry);
                            this.possiblyIncompleteClassesAndProperties.addAll(incompleteClassesAndProperties);
                            LOGGER.warn("Possibly incomplete class/property {} (materialization problem).\nDetails: {}",
                                    incompleteClassesAndProperties, e);
                        } else {
                            LOGGER.error("Problem materializing {}", mappingClusterEntry.getIQTree());
                            throw e;
                        }
                    }

                }
                resultTuple = tmpContextResultSet.next();
                tmpRDFFactsIterator = toRdfFacts(resultTuple, currentRDFFactTemplates);
            }
            return tmpRDFFactsIterator.next();
        } catch (OntopConnectionException e) {
            LOGGER.error("Connection error while retrieving next RDF fact", e);
            closeResource(tmpContextResultSet);
            closeResource(tmpStatement);
            throw new OntopQueryEvaluationException("Failed to retrieve next RDF fact", e);
        }
    }

    @Override
    Logger getLogger() {
        return LOGGER;
    }

    private IQ translateIntoNativeQuery(MappingEntryCluster mappingClusterEntry, QueryLogger queryLogger, QueryContext queryContext) {
        ImmutableList<Variable> variables = mappingClusterEntry.getRDFFactTemplates().getVariables().asList();
        IQ tree = iqFactory.createIQ(
                atomFactory.getDistinctVariableOnlyDataAtom(atomFactory.getRDFAnswerPredicate(variables.size()),
                        variables),
                mappingClusterEntry.getIQTree()
        );

        IQ optimizedQuery = generalOptimizer.optimize(tree, queryContext);
        IQ plannedQuery = queryPlanner.optimize(optimizedQuery);
        IQ executableQuery = nativeQueryGenerator.generateSourceQuery(plannedQuery, true, true);

        queryLogger.declareReformulationFinishedAndSerialize(executableQuery, false);
        queryCounter ++;

        return executableQuery;
    }

    private Iterator<RDFFact> toRdfFacts(OntopBindingSet tuple, RDFFactTemplates templates) {
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
                .collect(ImmutableCollectors.toList());
    }

    private ImmutableSet<IRI> getIncompleteClassesAndProperties(MappingEntryCluster mappingCluster) {
        ImmutableSet<Variable> predicatesVars = mappingCluster.getRDFFactTemplates().getTriplesOrQuadsVariables().stream()
                .map(variables -> variables.get(1))
                .collect(ImmutableCollectors.toSet());

        if (mappingCluster.getIQTree().getRootNode() instanceof ConstructionNode) {
            Substitution<ImmutableTerm> substitution = ((ConstructionNode) mappingCluster.getIQTree().getRootNode()).getSubstitution();
            return predicatesVars.stream()
                    .map(substitution::applyToTerm)
                    .filter(t -> t instanceof IRIConstant)
                    .map(t -> ((IRIConstant) t).getIRI())
                    .collect(ImmutableCollectors.toSet());
        } else {
            return ImmutableSet.of();
        }
    }
}
