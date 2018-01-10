package it.unibz.inf.ontop.iq.impl;

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

import java.util.Optional;

@Singleton
public class IQTreeTools {

    private final TermFactory termFactory;
    private final ConstructionNodeTools constructionNodeTools;

    @Inject
    private IQTreeTools(TermFactory termFactory, ConstructionNodeTools constructionNodeTools) {
        this.termFactory = termFactory;
        this.constructionNodeTools = constructionNodeTools;
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
                .reduceDomainToIntersectionWith(tree.getVariables());

        if (reducedSubstitution.isEmpty())
            return Optional.empty();

        if (reducedSubstitution.getImmutableMap().values().stream().anyMatch(value ->
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
     * Typically thrown when a "null" variable is propagated down
     *
     */
    public static class UnsatisfiableDescendingSubstitutionException extends Exception {
    }


}
