package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.model.term.Variable;

import java.util.Optional;

public interface FlattenNode extends UnaryOperatorNode {


    Variable getFlattenedVariable();

    Variable getOutputVariable();

    Optional<Variable> getPositionVariable();

    boolean isStrict();

//    @Override
//    P acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer);

//    @Override
//    NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer);

//    /**
//     * Returns a new FlattenNode of the same type, with the new arguments
//     */
//    P newNode(Variable flattenedVariable,
//              Variable outputVariable);

//    int getArrayIndexIndex();

//    @Override
//    P clone();

   // public ImmutableList<Boolean> getArgumentNullability();
}
