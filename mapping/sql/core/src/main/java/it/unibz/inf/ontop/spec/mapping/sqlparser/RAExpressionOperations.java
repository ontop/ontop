package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.IllegalJoinException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.function.Function;
import java.util.stream.Stream;

public class RAExpressionOperations implements RAOperations<RAExpression> {

    private final RAExprressionAttributesOperations aops = new RAExprressionAttributesOperations();
    private final TermFactory termFactory;

    public RAExpressionOperations(TermFactory termFactory) {
        this.termFactory = termFactory;
    }

    /**
     * (relational expression) AS A
     *
     * @param aliasId a {@link RelationID}
     * @return a {@link RAExpression}
     */

    @Override
    public RAExpression withAlias(RAExpression rae, RelationID aliasId) {
        return new RAExpression(rae.getDataAtoms(),
                rae.getFilterAtoms(),
                aops.withAlias(rae.getAttributes(), aliasId));
    }

    /**
     * CROSS JOIN (also denoted by , in SQL)
     *
     * @param left a {@link RAExpression}
     * @param right a {@link RAExpression}
     * @return a {@link RAExpression}
     * @throws IllegalJoinException if the same alias occurs in both arguments
     */
    @Override
    public RAExpression crossJoin(RAExpression left, RAExpression right) throws IllegalJoinException {
        return filterProduct(
                left,
                right,
                ImmutableList.of(),
                aops.crossJoin(left.getAttributes(), right.getAttributes()));
    }

    /**
     * JOIN USING
     *
     * @param left a {@link RAExpression}
     * @param right a {@link RAExpression}
     * @param using a {@link ImmutableSet}<{@link QuotedID}>
     * @return a {@link RAExpression}
     * @throws IllegalJoinException if the same alias occurs in both arguments
     *          or one of the `using' attributes is ambiguous or absent
     */

    @Override
    public RAExpression joinUsing(RAExpression left, RAExpression right, ImmutableSet<QuotedID> using) throws IllegalJoinException {
        RAExpressionAttributes attributes =
                aops.joinUsing(left.getAttributes(), right.getAttributes(), using);

        return filterProduct(
                left,
                right,
                getJoinOnFilter(left.getAttributes(), right.getAttributes(), using),
                attributes);
    }

    /**
     * internal implementation of JOIN USING and NATURAL JOIN
     *
     * @param re1 a {@link RAExpressionAttributes}
     * @param re2 a {@link RAExpressionAttributes}
     * @param using a {@link ImmutableSet}<{@link QuotedID}>
     * @return a {@Link ImmutableList}<{@link ImmutableExpression}>
     */
    private ImmutableList<ImmutableExpression> getJoinOnFilter(RAExpressionAttributes re1,
                                                               RAExpressionAttributes re2,
                                                               ImmutableSet<QuotedID> using) {

        return using.stream()
                .map(id -> termFactory.getNotYetTypedEquality(re1.get(id), re2.get(id)))
                .collect(ImmutableCollectors.toList());
    }


    /**
     * JOIN ON
     *
     * @param left a {@link RAExpression}
     * @param right a {@link RAExpression}
     * @param getAtomOnExpression
     * @return a {@link RAExpression}
     * @throws IllegalJoinException if the same alias occurs in both arguments
     */

    @Override
    public RAExpression joinOn(RAExpression left, RAExpression right, Function<RAExpressionAttributes, ImmutableList<ImmutableExpression>> getAtomOnExpression) throws IllegalJoinException {
        RAExpressionAttributes attributes =
                aops.crossJoin(left.getAttributes(), right.getAttributes());

        return filterProduct(
                left,
                right,
                getAtomOnExpression.apply(attributes),
                attributes);
    }

    @Override
    public ImmutableSet<QuotedID> getSharedAttributeNames(RAExpression left, RAExpression right) {
        return aops.getSharedAttributeNames(left.getAttributes(), right.getAttributes());
    }

    private RAExpression filterProduct(RAExpression left, RAExpression right, ImmutableList<ImmutableExpression> filter, RAExpressionAttributes attributes) {
        return new RAExpression(
                Stream.concat(left.getDataAtoms().stream(), right.getDataAtoms().stream())
                        .collect(ImmutableCollectors.toList()),
                Stream.concat(Stream.concat(left.getFilterAtoms().stream(), right.getFilterAtoms().stream()), filter.stream())
                        .collect(ImmutableCollectors.toList()),
                attributes);
    }
}
