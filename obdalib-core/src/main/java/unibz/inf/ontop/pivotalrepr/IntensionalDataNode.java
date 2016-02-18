package unibz.inf.ontop.pivotalrepr;

import unibz.inf.ontop.model.DataAtom;
import unibz.inf.ontop.model.ImmutableSubstitution;
import unibz.inf.ontop.model.VariableOrGroundTerm;

/**
 * TODO: explain
 * TODO: find a better name
 */
public interface IntensionalDataNode extends DataNode {

    @Override
    IntensionalDataNode clone();

    @Override
    IntensionalDataNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException;

    @Override
    SubstitutionResults<IntensionalDataNode> applyAscendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query);

    @Override
    SubstitutionResults<IntensionalDataNode> applyDescendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution);

    @Override
    IntensionalDataNode newAtom(DataAtom newAtom);
}
