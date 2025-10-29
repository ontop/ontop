package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;

/**
 * Immutable.
 *
 * See IntermediateQueryFactory for creating new instances.
 */
public interface QueryNode {

    /**
     * Set of variables MENTIONED in the node, INDEPENDENTLY of its sub-tree.
     *
     * See ImmutableQuery.getVariables(QueryNode node) for getting all the variables
     * returned by the QueryNode.
     *
     */
    default ImmutableSet<Variable> getLocalVariables() {
        return Sets.union(getLocallyDefinedVariables(), getLocallyRequiredVariables()).immutableCopy();
    }


    /**
     * Set of variables that this node, INDEPENDENTLY OF THE REQUIREMENTS OF ITS ANCESTORS,
     * requires to be defined in the sub-tree.
     *
     * Said differently, additional variable requirements may come from its ancestors.
     *
     */
    ImmutableSet<Variable> getLocallyRequiredVariables();

    /**
     * Locally defined variables must not appear in the sub-tree
     */
    ImmutableSet<Variable> getLocallyDefinedVariables();

    /**
     * Some nodes like aggregation and values nodes do not handle some descending terms in their trees
     * by externalize it a parent filter node.
     * Important to know for nodes with filtering conditions when normalizing, so to guarantee convergence.
     *
     */
    default boolean wouldKeepDescendingGroundTermInFilterAbove(Variable variable, boolean isConstant)  {
        return false;
    }

    QueryNode applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution);

        @Override
    int hashCode();

    @Override
    boolean equals(Object obj);
}
