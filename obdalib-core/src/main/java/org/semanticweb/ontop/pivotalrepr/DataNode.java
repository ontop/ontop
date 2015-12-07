package org.semanticweb.ontop.pivotalrepr;

import org.semanticweb.ontop.model.DataAtom;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.VariableOrGroundTerm;

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
