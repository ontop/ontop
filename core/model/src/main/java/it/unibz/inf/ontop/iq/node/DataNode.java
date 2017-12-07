package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;

/**
 * TODO: describe
 */
public interface DataNode<P extends AtomPredicate> extends LeafIQTree {

    /**
     * Data atom containing the projected variables
     */
    DataAtom<P> getProjectionAtom();

    /**
     * Returns a new DataNode of the same type that will use the new atom
     */
    DataNode<P> newAtom(DataAtom<P> newAtom);

    @Override
    SubstitutionResults<? extends DataNode<P>> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode childNode, IntermediateQuery query);

    @Override
    SubstitutionResults<? extends DataNode<P>> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query);
}
