package it.unibz.inf.ontop.datalog.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractIntensionalQueryMerger;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Converts a datalog program into an intermediate query
 */
public class DatalogProgram2QueryConverterImpl implements DatalogProgram2QueryConverter {

    private final IntermediateQueryFactory iqFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final DatalogRule2QueryConverter datalogRuleConverter;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final QueryTransformerFactory transformerFactory;

    @Inject
    private DatalogProgram2QueryConverterImpl(IntermediateQueryFactory iqFactory,
                                              UnionBasedQueryMerger queryMerger,
                                              DatalogRule2QueryConverter datalogRuleConverter,
                                              TermFactory termFactory, SubstitutionFactory substitutionFactory,
                                              QueryTransformerFactory transformerFactory) {
        this.iqFactory = iqFactory;
        this.queryMerger = queryMerger;
        this.datalogRuleConverter = datalogRuleConverter;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.transformerFactory = transformerFactory;
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
    public IQ convertDatalogProgram(DBMetadata dbMetadata,
                                                   DatalogProgram queryProgram,
                                                   Collection<Predicate> tablePredicates,
                                                   ExecutorRegistry executorRegistry)
            throws InvalidDatalogProgramException, EmptyQueryException {
        List<CQIE> rules = queryProgram.getRules();

        DatalogDependencyGraphGenerator dependencyGraph = new DatalogDependencyGraphGenerator(rules);
        List<Predicate> topDownPredicates = Lists.reverse(dependencyGraph.getPredicatesInBottomUp());

        if (topDownPredicates.size() == 0) {
            throw new EmptyQueryException();
        }

        Predicate rootPredicate = topDownPredicates.get(0);
        if (tablePredicates.contains(rootPredicate))
            throw new InvalidDatalogProgramException("The root predicate must not be a table predicate");

        Multimap<Predicate, CQIE> ruleIndex = dependencyGraph.getRuleIndex();

        Optional<ImmutableQueryModifiers> topQueryModifiers = convertModifiers(queryProgram.getQueryModifiers());

        /*
         * TODO: explain
         */
        // Non-final
        IQ iq = convertDatalogDefinitions(dbMetadata, rootPredicate, ruleIndex, tablePredicates,
                topQueryModifiers, executorRegistry).get();

        /*
         * Rules (sub-queries)
         */
        for (int i=1; i < topDownPredicates.size() ; i++) {
            Predicate datalogAtomPredicate  = topDownPredicates.get(i);
            Optional<IQ> optionalSubQuery = convertDatalogDefinitions(dbMetadata, datalogAtomPredicate,
                    ruleIndex, tablePredicates, NO_QUERY_MODIFIER, executorRegistry);
            if (optionalSubQuery.isPresent()) {

                IntensionalQueryMerger intensionalQueryMerger = new IntensionalQueryMerger(
                        ImmutableMap.of(datalogAtomPredicate, optionalSubQuery.get()));
                iq = intensionalQueryMerger.optimize(iq);
            }
        }

        return iq;
    }


    /**
     * TODO: explain and comment
     */
    @Override
    public Optional<IQ> convertDatalogDefinitions(DBMetadata dbMetadata,
                                                                 Predicate datalogAtomPredicate,
                                                                 Multimap<Predicate, CQIE> datalogRuleIndex,
                                                                 Collection<Predicate> tablePredicates,
                                                                 Optional<ImmutableQueryModifiers> optionalModifiers,
                                                                 ExecutorRegistry executorRegistry)
            throws InvalidDatalogProgramException {

        Collection<CQIE> atomDefinitions = datalogRuleIndex.get(datalogAtomPredicate);

        return convertDatalogDefinitions(dbMetadata,atomDefinitions,tablePredicates,optionalModifiers,executorRegistry);

    }

    @Override
    public Optional<IQ> convertDatalogDefinitions(DBMetadata dbMetadata, Collection<CQIE> atomDefinitions,
                                                                 Collection<Predicate> tablePredicates,
                                                                 Optional<ImmutableQueryModifiers> optionalModifiers,
                                                                 ExecutorRegistry executorRegistry) throws InvalidDatalogProgramException {

        switch(atomDefinitions.size()) {
            case 0:
                return Optional.empty();
            case 1:
                CQIE definition = atomDefinitions.iterator().next();
                return Optional.of(datalogRuleConverter.convertDatalogRule(dbMetadata, definition, tablePredicates, optionalModifiers,
                        iqFactory, executorRegistry));
            default:
                List<IQ> convertedDefinitions = new ArrayList<>();
                for (CQIE datalogAtomDefinition : atomDefinitions) {

                    convertedDefinitions.add(
                            datalogRuleConverter.convertDatalogRule(dbMetadata, datalogAtomDefinition, tablePredicates,
                                    Optional.empty(), iqFactory, executorRegistry));
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


    private class IntensionalQueryMerger extends AbstractIntensionalQueryMerger {

        private final ImmutableMap<Predicate, IQ> map;

        private IntensionalQueryMerger(ImmutableMap<Predicate, IQ> map) {
            super(iqFactory);
            this.map = map;
        }

        @Override
        protected QueryMergingTransformer createTransformer(ImmutableSet<Variable> knownVariables) {
            return new DatalogQueryMergingTransformer(new VariableGenerator(knownVariables, termFactory));
        }

        private class DatalogQueryMergingTransformer extends AbstractIntensionalQueryMerger.QueryMergingTransformer {

            protected DatalogQueryMergingTransformer(VariableGenerator variableGenerator) {
                super(variableGenerator, iqFactory, substitutionFactory, transformerFactory);
            }

            @Override
            protected Optional<IQ> getDefinition(IntensionalDataNode dataNode) {
                return Optional.ofNullable(map.get(dataNode.getProjectionAtom().getPredicate()));
            }

            @Override
            protected IQTree handleIntensionalWithoutDefinition(IntensionalDataNode dataNode) {
                return dataNode;
            }
        }
    }


}
