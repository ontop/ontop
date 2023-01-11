package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.impl.ConstructionNodeTools;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;

@Singleton
public class IQTreeTools {

    private final TermFactory termFactory;
    private final ConstructionNodeTools constructionNodeTools;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private IQTreeTools(TermFactory termFactory, ConstructionNodeTools constructionNodeTools,
                        SubstitutionFactory substitutionFactory) {
        this.termFactory = termFactory;
        this.constructionNodeTools = constructionNodeTools;
        this.substitutionFactory = substitutionFactory;
    }

    /**
     * Excludes the variables that are not projected by the IQTree
     *
     * If a "null" variable is propagated down, throws an UnsatisfiableDescendingSubstitutionException.
     *
     */
    public Optional<ImmutableSubstitution<? extends VariableOrGroundTerm>> normalizeDescendingSubstitution(
            IQTree tree, ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution)
            throws UnsatisfiableDescendingSubstitutionException {

        ImmutableSubstitution<? extends VariableOrGroundTerm> reducedSubstitution = descendingSubstitution
                .builder().restrictDomain(tree.getVariables()).build();

        if (reducedSubstitution.isEmpty())
            return Optional.empty();

        if (reducedSubstitution.getRange().stream().anyMatch(value ->
                value.equals(termFactory.getNullConstant()))) {
            throw new UnsatisfiableDescendingSubstitutionException();
        }

        return Optional.of(reducedSubstitution);
    }

    public ImmutableSet<Variable> computeNewProjectedVariables(
            ImmutableSubstitution<? extends ImmutableTerm> descendingSubstitution,
            ImmutableSet<Variable> projectedVariables) {
        return constructionNodeTools.computeNewProjectedVariables(descendingSubstitution, projectedVariables);
    }

    /**
     * If the substitution is an fresh renaming, returns it as an injective substitution
     */
    public Optional<InjectiveVar2VarSubstitution> extractFreshRenaming(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            ImmutableSet<Variable> projectedVariables) {
        ImmutableSubstitution<Variable> var2VarFragment = descendingSubstitution.builder().restrictRangeTo(Variable.class).build();
        ImmutableSet<Map.Entry<Variable, Variable>> var2VarMap = var2VarFragment.entrySet();

        int size = descendingSubstitution.entrySet().size();
        if (var2VarMap.size() != size)
            return Optional.empty();

        ImmutableSet<Variable> coDomain = var2VarFragment.getRange().stream()
                .filter(v -> !projectedVariables.contains(v))
                .collect(ImmutableCollectors.toSet());
        return (coDomain.size() == size)
                ? Optional.of(substitutionFactory.getInjectiveVar2VarSubstitution(var2VarFragment.getImmutableMap()))
                : Optional.empty();
    }


    /**
     * Typically thrown when a "null" variable is propagated down
     *
     */
    public static class UnsatisfiableDescendingSubstitutionException extends Exception {
    }


}
