package it.unibz.inf.ontop.pivotalrepr;

import it.unibz.inf.ontop.model.DataAtom;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;

/**
 * TODO: describe
 */
public interface DataNode extends ConstructionOrDataNode {

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
