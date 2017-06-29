package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;

/**
 * TODO: describe
 */
public interface DataNode extends ExplicitVariableProjectionNode {

    /**
     * Data atom containing the projected variables
     */
    DataAtom getProjectionAtom();

    /**
     * Returns a new DataNode of the same type that will use the new atom
     */
    DataNode newAtom(DataAtom newAtom);

    @Override
    SubstitutionResults<? extends DataNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode childNode, IntermediateQuery query);

    @Override
    SubstitutionResults<? extends DataNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query);
}
