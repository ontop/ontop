package it.unibz.inf.ontop.iq.tools.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.tools.ProjectionDecomposer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

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
    public ProjectionDecomposition decomposeSubstitution(Substitution<? extends ImmutableTerm> substitution,
                                                         VariableGenerator variableGenerator) {
        ImmutableMap<Variable, DefinitionDecomposition> decompositionMap = substitution.builder()
                .toMap((v, t) -> decomposeDefinition(t, variableGenerator, Optional.of(v)));

        Substitution<ImmutableTerm> topSubstitution = decompositionMap.entrySet().stream()
                .collect(substitutionFactory.toSubstitutionSkippingIdentityEntries(Map.Entry::getKey, e -> e.getValue().term));

        Optional<Substitution<ImmutableTerm>> subSubstitution = combineSubstitutions(decompositionMap.values());

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

                Optional<Substitution<ImmutableTerm>> subSubstitution = combineSubstitutions(childDecompositions);

                return subSubstitution
                        .map(s -> new DefinitionDecomposition(
                                termFactory.getImmutableFunctionalTerm(
                                        functionalTerm.getFunctionSymbol(),
                                        childDecompositions.stream()
                                                .map(d -> d.term)
                                                .collect(ImmutableCollectors.toList())),
                                s))
                        .orElseGet(() -> new DefinitionDecomposition(functionalTerm));
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

    private Optional<Substitution<ImmutableTerm>> combineSubstitutions(
           ImmutableCollection<DefinitionDecomposition> decompositions) {
        return decompositions.stream()
                .map(d -> d.substitution)
                .flatMap(Optional::stream)
                .reduce(substitutionFactory::union);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class DefinitionDecomposition {
        final ImmutableTerm term;
        final Optional<Substitution<ImmutableTerm>> substitution;

        private DefinitionDecomposition(ImmutableTerm term, Substitution<ImmutableTerm> substitution) {
            this.term = term;
            this.substitution = Optional.of(substitution);
        }

        private DefinitionDecomposition(ImmutableTerm term) {
            this.term = term;
            this.substitution = Optional.empty();
        }
    }
}
