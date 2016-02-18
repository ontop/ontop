package it.unibz.inf.ontop.pivotalrepr;

import it.unibz.inf.ontop.model.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.DataAtom;
import it.unibz.inf.ontop.model.ImmutableSubstitution;

/**
 * TODO: explain
 */
public interface ExtensionalDataNode extends DataNode {

    @Override
    ExtensionalDataNode clone();

    @Override
    ExtensionalDataNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;

    @Override
    SubstitutionResults<ExtensionalDataNode> applyAscendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query);

    @Override
    SubstitutionResults<ExtensionalDataNode> applyDescendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution);

    @Override
    ExtensionalDataNode newAtom(DataAtom newAtom);

}
