package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.sqlparser.exception.IllegalJoinException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RAExpressionOperations implements RAOperations<RAExpression> {

    private final RAExpressionAttributesOperations aops = new RAExpressionAttributesOperations();
    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;

    public RAExpressionOperations(TermFactory termFactory, IntermediateQueryFactory iqFactory) {
        this.termFactory = termFactory;
        this.iqFactory = iqFactory;
    }

    @Override
    public RAExpression create() {
        return new RAExpression(ImmutableList.of(), ImmutableList.of(), aops.create());
    }

    @Override
    public RAExpression create(NamedRelationDefinition relation, ImmutableList<Variable> variables) {
        return new RAExpression(
                createExtensionalDataNodes(relation, variables),
                ImmutableList.of(),
                aops.create(relation, variables));
    }


    /**
     * (relational expression) AS A
     *
     * @param aliasId a {@link RelationID}
     * @return a {@link RAExpression}
     */

    @Override
    public RAExpression withAlias(RAExpression rae, RelationID aliasId) {
        return new RAExpression(rae.getDataAtoms(), rae.getFilterAtoms(),
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
        return product(left, right,
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
        return filter(
                product(left, right,
                        aops.joinUsing(left.getAttributes(), right.getAttributes(), using)),
                getJoinOnFilter(left.getAttributes(), right.getAttributes(), using));
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
        RAExpression rae = crossJoin(left, right);
        return filter(rae, getAtomOnExpression.apply(rae.getAttributes()));
    }

    @Override
    public ImmutableSet<QuotedID> getSharedAttributeNames(RAExpression left, RAExpression right) {
        return aops.getSharedAttributeNames(left.getAttributes(), right.getAttributes());
    }

    @Override
    public RAExpression filter(RAExpression rae, ImmutableList<ImmutableExpression> filter) {
        return new RAExpression(rae.getDataAtoms(),
                Stream.concat(rae.getFilterAtoms().stream(), filter.stream())
                        .collect(ImmutableCollectors.toList()),
                rae.getAttributes());
    }

    private RAExpression product(RAExpression left, RAExpression right, RAExpressionAttributes attributes) {
        return new RAExpression(
                Stream.concat(left.getDataAtoms().stream(), right.getDataAtoms().stream())
                        .collect(ImmutableCollectors.toList()),
                Stream.concat(left.getFilterAtoms().stream(), right.getFilterAtoms().stream())
                        .collect(ImmutableCollectors.toList()),
                attributes);
    }

    public RAExpression createWithoutName(RelationDefinition relation, ImmutableList<Variable> variables) {
        return new RAExpression(
                createExtensionalDataNodes(relation, variables),
                ImmutableList.of(),
                aops.create(aops.getAttributesMap(relation, variables)));
    }

    private ImmutableList<ExtensionalDataNode> createExtensionalDataNodes(RelationDefinition relation, ImmutableList<Variable> variables) {
        ImmutableMap<Integer, Variable> terms = IntStream.range(0, variables.size()).boxed()
                .collect(ImmutableCollectors.toMap(Function.identity(), variables::get));

        return ImmutableList.of(iqFactory.createExtensionalDataNode(relation, terms));
    }
}
