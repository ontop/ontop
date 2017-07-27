package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.QueryNode;

import java.util.Optional;

/**
 *  Retrieve bindings from the sub-tree,
 *  possible different implementations based on the search depth
 *
 *  TODO: explain
 */
public interface BindingExtractor {

    interface Extraction {
        Optional<ImmutableSubstitution<ImmutableTerm>> getOptionalSubstitution();
        ImmutableSet<Variable> getVariablesWithConflictingBindings();
    }

    Extraction extractInSubTree(IntermediateQuery query, QueryNode subTreeRootNode) ;

}
