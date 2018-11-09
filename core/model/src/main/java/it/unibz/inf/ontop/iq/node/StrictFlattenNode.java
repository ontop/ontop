package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;

public interface StrictFlattenNode extends FlattenNode {

    @Override
    default boolean isStrict() {
        return true;
    }

    @Override
    StrictFlattenNode newAtom(DataAtom<RelationPredicate> newAtom);

//    @Override
//    SubstitutionResults<StrictFlattenNode> applyAscendingSubstitution(
//            ImmutableSubstitution<? extends ImmutableTerm> substitution, QueryNode childNode, IntermediateQuery query);

//    @Override
//    SubstitutionResults<StrictFlattenNode> applyDescendingSubstitution(
//            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query);

    @Override
    StrictFlattenNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer);

    @Override
    StrictFlattenNode clone();

//    @Override
//    StrictFlattenNode rename(InjectiveVar2VarSubstitution renamingSubstitution);
}
