package it.unibz.inf.ontop.iq.tools.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.tools.ProjectionDecomposer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ProjectionDecomposerImpl implements ProjectionDecomposer {

    private final Predicate<ImmutableFunctionalTerm> decompositionOracle;
    private final Predicate<NonFunctionalTerm> postprocessNonFunctionalDefinitionOracle;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    @AssistedInject
    private ProjectionDecomposerImpl(@Assisted Predicate<ImmutableFunctionalTerm> decompositionOracle,
                                    @Assisted Predicate<NonFunctionalTerm> postprocessNonFunctionalDefinitionOracle,
                                    SubstitutionFactory substitutionFactory, TermFactory termFactory) {
        this.decompositionOracle = decompositionOracle;
        this.postprocessNonFunctionalDefinitionOracle = postprocessNonFunctionalDefinitionOracle;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
    }

    @Override
    public ProjectionDecomposition decomposeSubstitution(ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                                         VariableGenerator variableGenerator) {
        ImmutableMap<Variable, DefinitionDecomposition> decompositionMap = substitution.getImmutableMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> decomposeDefinition(e.getValue(), variableGenerator, Optional.of(e.getKey()))
                ));

        ImmutableMap<Variable, ImmutableTerm> topSubstitutionMap = decompositionMap.entrySet().stream()
                // To avoid entries like t/t
                .filter(e -> !e.getKey().equals(e.getValue().term))
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().term));
        ImmutableSubstitution<ImmutableTerm> topSubstitution = substitutionFactory.getSubstitution(topSubstitutionMap);

        Optional<ImmutableSubstitution<ImmutableTerm>> subSubstitution = combineSubstitutions(
                decompositionMap.values().stream()
                        .map(d -> d.substitution));

        return subSubstitution
                .map(s -> topSubstitution.isEmpty()
                        ? ProjectionDecompositionImpl.createSubSubstitutionDecomposition(s)
                        : ProjectionDecompositionImpl.createDecomposition(topSubstitution, s))
                .orElseGet(() -> ProjectionDecompositionImpl.createTopSubstitutionDecomposition(topSubstitution));
    }

    /**
     * Recursive
     */
    private DefinitionDecomposition decomposeDefinition(ImmutableTerm term, VariableGenerator variableGenerator,
                                                        Optional<Variable> definedVariable) {
        if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;
            if (decompositionOracle.test(functionalTerm)) {
                // Recursive
                ImmutableList<DefinitionDecomposition> childDecompositions = functionalTerm.getTerms().stream()
                        .map(t -> decomposeDefinition(t, variableGenerator, Optional.empty()))
                        .collect(ImmutableCollectors.toList());

                Optional<ImmutableSubstitution<ImmutableTerm>> subSubstitution = combineSubstitutions(
                        childDecompositions.stream()
                                .map(d -> d.substitution));

                ImmutableFunctionalTerm newFunctionalTerm = subSubstitution
                        .map(s -> childDecompositions.stream()
                                .map(d -> d.term)
                                .collect(ImmutableCollectors.toList()))
                        .map(children -> termFactory.getImmutableFunctionalTerm(
                                functionalTerm.getFunctionSymbol(), children))
                        .orElse(functionalTerm);

                return subSubstitution
                        .map(s -> new DefinitionDecomposition(newFunctionalTerm, s))
                        .orElse(new DefinitionDecomposition(functionalTerm));
            }
            else {
                Variable variable = definedVariable
                        .orElseGet(variableGenerator::generateNewVariable);

                // Wraps variables replacing an expression into an IS_TRUE functional term
                ImmutableTerm newTerm = ((!definedVariable.isPresent())
                        && (functionalTerm instanceof ImmutableExpression))
                        ? termFactory.getIsTrue(variable)
                        : variable;

                return new DefinitionDecomposition(newTerm,
                        substitutionFactory.getSubstitution(variable, functionalTerm));
            }
        }
        /*
         * When the definition of the substitution (not a sub-term of a functional term) is not functional,
         * we may also decide not to post-process it.
         *
         * Useful for using Ontop for generating SQL queries with no post-processing
         * (for other purposes than SPARQL query answering)
         *
         */
        else if (definedVariable.isPresent()
                && (!postprocessNonFunctionalDefinitionOracle.test((NonFunctionalTerm) term))) {
            Variable variable = definedVariable.get();
            return new DefinitionDecomposition(variable, substitutionFactory.getSubstitution(variable, term));
        }
        else
            return new DefinitionDecomposition(term);
    }

    private Optional<ImmutableSubstitution<ImmutableTerm>> combineSubstitutions(
            Stream<Optional<ImmutableSubstitution<ImmutableTerm>>> stream) {
        return stream
                .filter(Optional::isPresent)
                .map(Optional::get)
                // The composition here behaves like an union (independent fresh variables)
                .reduce(ImmutableSubstitution::composeWith);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class DefinitionDecomposition {
        final ImmutableTerm term;
        final Optional<ImmutableSubstitution<ImmutableTerm>> substitution;

        private DefinitionDecomposition(ImmutableTerm term, ImmutableSubstitution<ImmutableTerm> substitution) {
            this.term = term;
            this.substitution = Optional.of(substitution);
        }

        private DefinitionDecomposition(ImmutableTerm term) {
            this.term = term;
            this.substitution = Optional.empty();
        }
    }
}
