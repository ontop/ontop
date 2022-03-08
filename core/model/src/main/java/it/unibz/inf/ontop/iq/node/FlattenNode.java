package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

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
 * A "strict" flatten discards instances with an empty relation or a NULL value for the flattened attribute.
 *
 * So the query:
 *   FLATTEN STRICT [ O/flatten(Y) ]
 *     R(X,Y)
 *
 * evaluated over R yields the relation: {
 *     (1, a),
 *     (1, b),
 *     (2, c)
 * }
 *
 * Optionally, viewing the unfolded relation as 0-indexed array,
 * the FLATTEN (or FLATTEN STRICT) operator may also output the index of each value in this array.
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

    Variable getOutputVariable();

    Optional<Variable> getIndexVariable();

    Optional<TermType> inferOutputType(Optional<TermType> extractSingleTermType);

    Optional<TermType> getIndexVariableType();

    ImmutableSet<Variable> getProjectedVariables(ImmutableSet<Variable> variablesProjectedByChildren);
}
