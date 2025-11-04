package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.sqlparser.exception.IllegalJoinException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

public class RAExpressionOperations implements RAOperations<RAExpression> {

    private final RAOperations<RAExpressionAttributes> aops = new RAExpressionAttributesOperations();
    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;

    public RAExpressionOperations(TermFactory termFactory, IntermediateQueryFactory iqFactory) {
        this.termFactory = termFactory;
        this.iqFactory = iqFactory;
    }

    @Override
    public RAExpression create() {
        return new RAExpression(iqFactory.createTrueNode(), aops.create());
    }

    @Override
    public RAExpression create(NamedRelationDefinition relation, ImmutableList<Variable> variables) {
        return create(relation, variables, aops.create(relation, variables));
    }

    @Override
    public RAExpression createWithoutName(RelationDefinition relation, ImmutableList<Variable> variables) {
        return create(relation, variables, aops.createWithoutName(relation, variables));
    }

    private RAExpression create(RelationDefinition relation, ImmutableList<Variable> variables, RAExpressionAttributes attributes) {
        ImmutableMap<Integer, Variable> terms = IntStream.range(0, variables.size()).boxed()
                .collect(ImmutableCollectors.toMap(Function.identity(), variables::get));

        ExtensionalDataNode node = iqFactory.createExtensionalDataNode(relation, terms);
        return new RAExpression(node, attributes);
    }


    /**
     * (relational expression) AS A
     *
     * @param aliasId a {@link RelationID}
     * @return a {@link RAExpression}
     */

    @Override
    public RAExpression withAlias(RAExpression rae, RelationID aliasId) {
        return new RAExpression(rae.getIQTree(), aops.withAlias(rae.getAttributes(), aliasId));
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
        return product(left, right, aops.crossJoin(left.getAttributes(), right.getAttributes()));
    }

    /**
     * JOIN USING
     *
     * @param left a {@link RAExpression}
     * @param right a {@link RAExpression}
     * @param using a {@link ImmutableSet}<{@link QuotedID}>
     * @return a {@link RAExpression}
     * @throws IllegalJoinException if the same alias occurs in both arguments
     *          or one of the `using` attributes is ambiguous or absent.
     */

    @Override
    public RAExpression joinUsing(RAExpression left, RAExpression right, ImmutableSet<QuotedID> using) throws IllegalJoinException {
        RAExpressionAttributes leftAttributes = left.getAttributes();
        RAExpressionAttributes rightAttributes = right.getAttributes();

        RAExpressionAttributes attributes = aops.joinUsing(leftAttributes, rightAttributes, using);

        Optional<ImmutableExpression> joinOnFilter = using.stream()
                .map(id -> new QualifiedAttributeID(null, id))
                .map(id -> termFactory.getNotYetTypedEquality(leftAttributes.get(id), rightAttributes.get(id)))
                .reduce(termFactory::getConjunction);

        return filter(product(left, right, attributes), joinOnFilter);
    }


    /**
     * JOIN ON
     *
     * @param left a {@link RAExpression}
     * @param right a {@link RAExpression}
     * @param atomOnExpressionProvider a mapper from {@link RAExpressionAttributes} to optional {@link ImmutableExpression}
     * @return a {@link RAExpression}
     * @throws IllegalJoinException if the same alias occurs in both arguments
     */

    @Override
    public RAExpression joinOn(RAExpression left, RAExpression right, Function<RAExpressionAttributes, Optional<ImmutableExpression>> atomOnExpressionProvider) throws IllegalJoinException {
        RAExpression rae = crossJoin(left, right);
        return filter(rae, atomOnExpressionProvider.apply(rae.getAttributes()));
    }

    @Override
    public ImmutableSet<QuotedID> getSharedAttributeNames(RAExpression left, RAExpression right) {
        return aops.getSharedAttributeNames(left.getAttributes(), right.getAttributes());
    }

    @Override
    public RAExpression filter(RAExpression rae, Optional<ImmutableExpression> filter) {
        return filter
                .map(f -> new RAExpression(
                        iqFactory.createUnaryIQTree(iqFactory.createFilterNode(f), rae.getIQTree()),
                        rae.getAttributes()))
                .orElse(rae);
    }

    private RAExpression product(RAExpression left, RAExpression right, RAExpressionAttributes attributes) {
        return new RAExpression(
                iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(),
                    ImmutableList.of(left.getIQTree(), right.getIQTree())),
                attributes);
    }
}
