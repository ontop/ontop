package it.unibz.inf.ontop.datalog.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractIntensionalQueryMerger;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Converts a datalog program into an intermediate query
 */
public class DatalogProgram2QueryConverterImpl implements DatalogProgram2QueryConverter {

    private final IntermediateQueryFactory iqFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final DatalogRule2QueryConverter datalogRuleConverter;
    private final SubstitutionFactory substitutionFactory;
    private final CoreUtilsFactory coreUtilsFactory;
    private final QueryTransformerFactory transformerFactory;
    private final QueryTransformerFactory queryTransformerFactory;

    @Inject
    private DatalogProgram2QueryConverterImpl(IntermediateQueryFactory iqFactory,
                                              UnionBasedQueryMerger queryMerger,
                                              DatalogRule2QueryConverter datalogRuleConverter,
                                              SubstitutionFactory substitutionFactory,
                                              CoreUtilsFactory coreUtilsFactory,
                                              QueryTransformerFactory transformerFactory,
                                              QueryTransformerFactory queryTransformerFactory) {
        this.iqFactory = iqFactory;
        this.queryMerger = queryMerger;
        this.datalogRuleConverter = datalogRuleConverter;
        this.substitutionFactory = substitutionFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.transformerFactory = transformerFactory;
        this.queryTransformerFactory = queryTransformerFactory;
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

    @Override
    public IQ convertDatalogProgram(DatalogProgram queryProgram, ImmutableList<Predicate> tablePredicates,
                                    ImmutableList<Variable> signature) throws EmptyQueryException {

        List<CQIE> rules = queryProgram.getRules();

        DatalogDependencyGraphGenerator dependencyGraph = new DatalogDependencyGraphGenerator(rules);
        List<Predicate> topDownPredicates = Lists.reverse(dependencyGraph.getPredicatesInBottomUp());

        if (topDownPredicates.isEmpty())
            throw new EmptyQueryException();


        Predicate rootPredicate = topDownPredicates.get(0);
        if (tablePredicates.contains(rootPredicate))
            throw new InvalidDatalogProgramException("The root predicate must not be a table predicate");

        Multimap<Predicate, CQIE> ruleIndex = dependencyGraph.getRuleIndex();

        Optional<ImmutableQueryModifiers> topQueryModifiers = convertModifiers(queryProgram.getQueryModifiers());

        /*
         * TODO: explain
         */
        // Non-final
        IQ iq = convertDatalogDefinitions(rootPredicate, ruleIndex, tablePredicates,
                topQueryModifiers).get();

        /*
         * Rules (sub-queries)
         */
        for (int j = 1; j < topDownPredicates.size() ; j++) {
            Predicate datalogAtomPredicate  = topDownPredicates.get(j);
            Optional<IQ> optionalSubQuery = convertDatalogDefinitions(datalogAtomPredicate,
                    ruleIndex, tablePredicates, NO_QUERY_MODIFIER);
            if (optionalSubQuery.isPresent()) {

                IntensionalQueryMerger intensionalQueryMerger = new IntensionalQueryMerger(
                        ImmutableMap.of(datalogAtomPredicate, optionalSubQuery.get()));
                iq = intensionalQueryMerger.optimize(iq);
            }
        }

        return enforceSignature(iq, signature);
    }

    /**
     * Hacked logic: because of ORDER conditions that are expected to use signature variables,
     * this method DOES NOT look for conflicts between signature variables and variables only appearing in the sub-tree.
     *
     * See the history for a better logic breaking this ugly hack.
     *
     * TODO: after getting rid of Datalog for encoding SPARQL queries, could try to clean it
     */
    private IQ enforceSignature(IQ iq, ImmutableList<Variable> signature) {

        ImmutableList<Variable> projectedVariables = iq.getProjectionAtom().getArguments();

        if (projectedVariables.equals(signature))
            return iq;

        if (projectedVariables.size() != signature.size())
            throw new IllegalArgumentException("The arity of the signature does not match the iq");

        InjectiveVar2VarSubstitution renamingSubstitution = substitutionFactory.getInjectiveVar2VarSubstitution(
                IntStream.range(0, projectedVariables.size())
                        .boxed()
                        .map(i -> Maps.immutableEntry(projectedVariables.get(i), signature.get(i)))
                        .filter(e -> !e.getKey().equals(e.getValue()))
                        .collect(ImmutableCollectors.toMap()));

        return queryTransformerFactory.createRenamer(renamingSubstitution)
                .transform(iq);
    }


    /**
     * TODO: explain and comment
     */
    private Optional<IQ> convertDatalogDefinitions(Predicate datalogAtomPredicate,
                                                  Multimap<Predicate, CQIE> datalogRuleIndex,
                                                  Collection<Predicate> tablePredicates,
                                                  Optional<ImmutableQueryModifiers> optionalModifiers)
            throws InvalidDatalogProgramException {

        Collection<CQIE> atomDefinitions = datalogRuleIndex.get(datalogAtomPredicate);

        return convertDatalogDefinitions(atomDefinitions,tablePredicates,optionalModifiers);

    }

    @Override
    public Optional<IQ> convertDatalogDefinitions(Collection<CQIE> atomDefinitions,
                                                  Collection<Predicate> tablePredicates,
                                                  Optional<ImmutableQueryModifiers> optionalModifiers) throws InvalidDatalogProgramException {

        // ROMAN (27 JUNE 2019): queryMerger does the same job, so the switch below is redundant
        switch(atomDefinitions.size()) {
            case 0:
                return Optional.empty();
            case 1:
                CQIE definition = atomDefinitions.iterator().next();
                return Optional.of(datalogRuleConverter.convertDatalogRule(definition, tablePredicates, optionalModifiers,
                        iqFactory));
            default:
                List<IQ> convertedDefinitions = new ArrayList<>();
                for (CQIE datalogAtomDefinition : atomDefinitions) {

                    convertedDefinitions.add(
                            datalogRuleConverter.convertDatalogRule(datalogAtomDefinition, tablePredicates,
                                    Optional.empty(), iqFactory));
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
            super(DatalogProgram2QueryConverterImpl.this.iqFactory);
            this.map = map;
        }

        @Override
        protected QueryMergingTransformer createTransformer(ImmutableSet<Variable> knownVariables) {
            return new DatalogQueryMergingTransformer(coreUtilsFactory.createVariableGenerator(knownVariables));
        }

        private class DatalogQueryMergingTransformer extends AbstractIntensionalQueryMerger.QueryMergingTransformer {

            protected DatalogQueryMergingTransformer(VariableGenerator variableGenerator) {
                super(variableGenerator, DatalogProgram2QueryConverterImpl.this.iqFactory, substitutionFactory, transformerFactory);
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
