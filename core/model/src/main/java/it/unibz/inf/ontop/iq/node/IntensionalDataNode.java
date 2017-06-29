package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

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
            QueryNode childNode, IntermediateQuery query);

    @Override
    SubstitutionResults<IntensionalDataNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query);

    @Override
    IntensionalDataNode newAtom(DataAtom newAtom);
}
