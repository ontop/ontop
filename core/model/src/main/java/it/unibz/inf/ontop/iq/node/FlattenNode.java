package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

public interface FlattenNode<P extends FlattenNode> extends DataAtomQueryNode<RelationPredicate>, UnaryOperatorNode {


    Variable getArrayVariable();

    VariableOrGroundTerm getArrayIndexTerm();

    @Override
    P acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer);

    @Override
    NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer);

//    @Override
//    P newAtom(DataAtom<RelationPredicate> newAtom);

    /**
     * Returns a new FlattenNode of the same type, with the new arguments
     */
    P newNode(Variable arrayVariable,
              int arrayIndexIndex,
              DataAtom<RelationPredicate> dataAtom,
              ImmutableList<Boolean> argumentNullability);

    int getArrayIndexIndex();

    @Override
    P clone();

    /**
     * TODO: to be generalize to each QueryNode
     */
//    FlattenNode rename(InjectiveVar2VarSubstitution renamingSubstitution);

    public ImmutableList<Boolean> getArgumentNullability();
}
