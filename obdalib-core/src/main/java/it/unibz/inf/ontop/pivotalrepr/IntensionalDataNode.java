package it.unibz.inf.ontop.pivotalrepr;

import it.unibz.inf.ontop.model.DataAtom;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;

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
    SubstitutionResults<IntensionalDataNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query);

    @Override
    SubstitutionResults<IntensionalDataNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query);

    @Override
    IntensionalDataNode newAtom(DataAtom newAtom);
}
