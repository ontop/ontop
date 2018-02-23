package it.unibz.inf.ontop.temporal.datalog.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.datalog.impl.ImmutableQueryModifiersImpl;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.TemporalIntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.proposal.QueryMergingProposal;
import it.unibz.inf.ontop.iq.proposal.impl.QueryMergingProposalImpl;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.temporal.datalog.TemporalDatalogProgram2QueryConverter;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


public class TemporalDatalogProgram2QueryConverterImpl implements TemporalDatalogProgram2QueryConverter {
    private final TemporalIntermediateQueryFactory iqFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final TemporalDatalogRule2QueryConverter datalogRuleConverter;

    @Inject
    private TemporalDatalogProgram2QueryConverterImpl(TemporalIntermediateQueryFactory iqFactory,
                                                      UnionBasedQueryMerger queryMerger, TemporalDatalogRule2QueryConverter datalogRuleConverter) {
        this.iqFactory = iqFactory;
        this.queryMerger = queryMerger;
        this.datalogRuleConverter = datalogRuleConverter;
    }



    private static final Optional<ImmutableQueryModifiers> NO_QUERY_MODIFIER = Optional.empty();

    /**
     * TODO: explain
     */
    public static class NotSupportedConversionException extends RuntimeException {
        public NotSupportedConversionException(String message) {
            super(message);
        }
    }

    /**
     * TODO: explain
     */
    public static class InvalidDatalogProgramException extends OntopInternalBugException {
        public InvalidDatalogProgramException(String message) {
            super(message);
        }
    }

    /**
     * TODO: explain
     *
     */
    @Override
    public IntermediateQuery convertDatalogProgram(DBMetadata dbMetadata,
                                                   DatalogProgram queryProgram,
                                                   Collection<Predicate> tablePredicates,
                                                   ExecutorRegistry executorRegistry)
            throws TemporalDatalogProgram2QueryConverterImpl.InvalidDatalogProgramException, EmptyQueryException {
        List<CQIE> rules = queryProgram.getRules();

        DatalogDependencyGraphGenerator dependencyGraph = new DatalogDependencyGraphGenerator(rules);
        List<Predicate> topDownPredicates = Lists.reverse(dependencyGraph.getPredicatesInBottomUp());

        if (topDownPredicates.size() == 0) {
            throw new EmptyQueryException();
        }

        Predicate rootPredicate = topDownPredicates.get(0);
        if (tablePredicates.contains(rootPredicate))
            throw new TemporalDatalogProgram2QueryConverterImpl.InvalidDatalogProgramException("The root predicate must not be a table predicate");

        Multimap<Predicate, CQIE> ruleIndex = dependencyGraph.getRuleIndex();

        Optional<ImmutableQueryModifiers> topQueryModifiers = convertModifiers(queryProgram.getQueryModifiers());

        /**
         * TODO: explain
         */
        IntermediateQuery intermediateQuery = convertDatalogDefinitions(dbMetadata, rootPredicate, ruleIndex, tablePredicates,
                topQueryModifiers, executorRegistry).get();

        /**
         * Rules (sub-queries)
         */
        for (int i=1; i < topDownPredicates.size() ; i++) {
            Predicate datalogAtomPredicate  = topDownPredicates.get(i);
            Optional<IntermediateQuery> optionalSubQuery = convertDatalogDefinitions(dbMetadata, datalogAtomPredicate,
                    ruleIndex, tablePredicates, NO_QUERY_MODIFIER, executorRegistry);
            if (optionalSubQuery.isPresent()) {

                ImmutableSet<IntensionalDataNode> intensionalMatches = findIntensionalDataNodes(intermediateQuery,
                        optionalSubQuery.get().getProjectionAtom());

                for(IntensionalDataNode intensionalNode : intensionalMatches) {

                    if (intermediateQuery.contains(intensionalNode)) {
                        QueryMergingProposal mergingProposal = new QueryMergingProposalImpl(intensionalNode,
                                optionalSubQuery);
                        intermediateQuery.applyProposal(mergingProposal);
                    }
                }
            }
        }

        return intermediateQuery;
    }


    private static ImmutableSet<IntensionalDataNode> findIntensionalDataNodes(IntermediateQuery query,
                                                                              DataAtom subQueryProjectionAtom) {
        return query.getIntensionalNodes()
                .filter(n -> subQueryProjectionAtom.hasSamePredicateAndArity(n.getProjectionAtom()))
                .collect(ImmutableCollectors.toSet());
    }

    /**
     * TODO: explain and comment
     */
    @Override
    public Optional<IntermediateQuery> convertDatalogDefinitions(DBMetadata dbMetadata,
                                                                 Predicate datalogAtomPredicate,
                                                                 Multimap<Predicate, CQIE> datalogRuleIndex,
                                                                 Collection<Predicate> tablePredicates,
                                                                 Optional<ImmutableQueryModifiers> optionalModifiers,
                                                                 ExecutorRegistry executorRegistry)
            throws TemporalDatalogProgram2QueryConverterImpl.InvalidDatalogProgramException {
        Collection<CQIE> atomDefinitions = datalogRuleIndex.get(datalogAtomPredicate);
        switch(atomDefinitions.size()) {
            case 0:
                return Optional.empty();
            case 1:
                CQIE definition = atomDefinitions.iterator().next();
                return Optional.of(datalogRuleConverter.convertDatalogRule(dbMetadata, definition, tablePredicates, optionalModifiers,
                        iqFactory, executorRegistry));
            default:
                List<IntermediateQuery> convertedDefinitions = new ArrayList<>();
                for (CQIE datalogAtomDefinition : atomDefinitions) {
                    convertedDefinitions.add(
                            datalogRuleConverter.convertDatalogRule(dbMetadata, datalogAtomDefinition, tablePredicates,
                                    Optional.<ImmutableQueryModifiers>empty(), iqFactory, executorRegistry));
                }
                return optionalModifiers.isPresent()
                        ? queryMerger.mergeDefinitions(convertedDefinitions, optionalModifiers.get())
                        : queryMerger.mergeDefinitions(convertedDefinitions);
        }
    }

    /**
     * TODO: explain
     */
    private static Optional<ImmutableQueryModifiers> convertModifiers(MutableQueryModifiers queryModifiers) {
        if (queryModifiers.hasModifiers()) {
            ImmutableQueryModifiers immutableQueryModifiers = new ImmutableQueryModifiersImpl(queryModifiers);
            return Optional.of(immutableQueryModifiers);
        } else {
            return Optional.empty();
        }
    }
}
