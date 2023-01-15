package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.impl.ConstructionNodeTools;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

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

        ImmutableSubstitution<? extends VariableOrGroundTerm> reducedSubstitution = descendingSubstitution.restrictDomainTo(tree.getVariables());

        if (reducedSubstitution.isEmpty())
            return Optional.empty();

        Constant nullConstant = termFactory.getNullConstant();

        if (reducedSubstitution.getRange().stream().anyMatch(v -> v.equals(nullConstant))) {
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
     * If the substitution is a fresh renaming, returns it as an injective substitution
     */
    public Optional<InjectiveVar2VarSubstitution> extractFreshRenaming(ImmutableSubstitution<? extends ImmutableTerm> descendingSubstitution,
                                                                       ImmutableSet<Variable> projectedVariables) {

        ImmutableSubstitution<Variable> var2VarFragment = descendingSubstitution.restrictRangeTo(Variable.class);
        int size = descendingSubstitution.getDomain().size();

        if (var2VarFragment.getDomain().size() != size
                || var2VarFragment.getRange().stream()
                        .filter(v -> !projectedVariables.contains(v))
                        .distinct()
                        .count()  != size)
            return Optional.empty();

        return Optional.of(substitutionFactory.injectiveVar2VarSubstitutionOf(var2VarFragment));
    }


    /**
     * Typically thrown when a "null" variable is propagated down
     *
     */
    public static class UnsatisfiableDescendingSubstitutionException extends Exception {
    }


}
