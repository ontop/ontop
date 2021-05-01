package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.model.term.Variable;

import java.util.Optional;

/**
 * Flattens an attribute of a nested relation.
 * The attribute corresponds to a unary relation
 *
 * E.g. consider the nested relation R = {
 *     (1, [a,b])
 *     (2, [c])
 * }
 *
 * Then the query:
 *   FLATTEN X,O [ O/flatten(Y) ]
 *   R(X,Y)
 *
 * evaluated over R yields the relation R': {
 *     (1, a)
 *     (1, b)
 *     (2, c)
 * }
 * The variable O is used (by subsequent operators) to refer to the second attribute of R'.
 *
 * Optionally, viewing the unfolded relation as 0-indexed array,
 * the FLATTEN operator may also output the index of each value in this array.
 *
 * So the query:
 *   FLATTEN X, O, I [ O/flatten(Y), I/indexIn(Y) ]
 *   R(X,Y)
 *
 * evaluated over R yields the relation R'': {
 *     (1, a, 0)
 *     (1, b, 1)
 *     (2, c, 0)
 * }
 *
 * The variable I is used (by subsequent operators) to refer to the third attribute of R''.
 *
 * We call:
 *  X the "flattened variable"
 *  O the "output variable"
 *  I the "index variable"
 *
 */
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
