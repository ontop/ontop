package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

public interface FlattenNode extends DataAtomQueryNode<RelationPredicate>{


    Variable getArrayVariable();

    VariableOrGroundTerm getArrayIndexTerm();

    boolean isStrict();

    @Override
    FlattenNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer);

    @Override
    NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer);

    @Override
    FlattenNode newAtom(DataAtom<RelationPredicate> newAtom);

    @Override
    FlattenNode clone();

    /**
     * TODO: to be generalize to each QueryNode
     */
//    FlattenNode rename(InjectiveVar2VarSubstitution renamingSubstitution);

    public ImmutableList<Boolean> getArgumentNullability();
}
