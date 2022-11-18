package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

/**
 * Flattens an attribute of a nested relation.
 * The attribute corresponds to a unary relation
 *
 * E.g. consider the nested relation R = {
 *     (1, [a,b]),
 *     (2, [c]),
 *     (3, []),
 *     (4, NULL)
 * }
 *
 * Then the query:
 *   FLATTEN [ O/flatten(Y) ]
 *     R(X,Y)
 *
 * evaluated over R yields the relation R': {
 *     (1, a),
 *     (1, b),
 *     (2, c),
 *     (3, NULL),
 *     (4, NULL)
 * }
 *  Variable O is used (by subsequent operators) to refer to the second attribute of R'.
 *
 * Optionally, viewing the unfolded relation as a 0-indexed array,
 * the FLATTEN operator may also output the index of each value in this array.
 *
 * So the query:
 *   FLATTEN [ O/flatten(Y), I/indexIn(Y) ]
 *     R(X,Y)
 *
 * evaluated over R yields the relation R'': {
 *     (1, a, 0),
 *     (1, b, 1),
 *     (2, c, 0),
 *     (3, NULL, NULL),
 *     (4, NULL, NULL)
 * }
 *
 * The variable I is used (by subsequent operators) to refer to the third attribute of R''.
 *
 * We call:
 *  Y the "flattened variable"
 *  O the "output variable"
 *  I the "index variable"
 */
public interface FlattenNode extends UnaryOperatorNode {

    @Override
    FlattenNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException;

    Variable getFlattenedVariable();

    DBTermType getFlattenedType();

    Variable getOutputVariable();

    Optional<Variable> getIndexVariable();

    Optional<TermType> inferOutputType(Optional<TermType> extractSingleTermType);

    Optional<TermType> getIndexVariableType();

    /**
     * Set of variables returned by a tree with this node as root, given the variables provided by the children
     */
    ImmutableSet<Variable> getVariables(ImmutableSet<Variable> childVariables);
}
