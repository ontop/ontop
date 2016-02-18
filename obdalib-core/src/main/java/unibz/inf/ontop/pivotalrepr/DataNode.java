package unibz.inf.ontop.pivotalrepr;

import unibz.inf.ontop.model.DataAtom;
import unibz.inf.ontop.model.ImmutableSubstitution;
import unibz.inf.ontop.model.VariableOrGroundTerm;

/**
 * TODO: describe
 */
public interface DataNode extends SubTreeDelimiterNode {

    /**
     * Returns a new DataNode of the same type that will use the new atom
     */
    DataNode newAtom(DataAtom newAtom);

    @Override
    SubstitutionResults<? extends DataNode> applyAscendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query);

    @Override
    SubstitutionResults<? extends DataNode> applyDescendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution);
}
