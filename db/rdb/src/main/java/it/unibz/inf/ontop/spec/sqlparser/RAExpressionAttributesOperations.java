package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.sqlparser.exception.IllegalJoinException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class RAExpressionAttributesOperations implements RAOperations<RAExpressionAttributes> {

    @Override
    public RAExpressionAttributes create() {
        return RAExpressionAttributes.of(ImmutableMap.of());
    }

    @Override
    public RAExpressionAttributes create(NamedRelationDefinition relation, ImmutableList<Variable> variables) {
        ImmutableSet<RelationID> relationAllIds = relation.getAllIDs().stream()
                .flatMap(id -> Stream.of(id, id.getTableOnlyID()))
                .distinct()
                .collect(ImmutableCollectors.toSet());
        ImmutableMap<QuotedID, ImmutableTerm> unqualifiedAttributes = getRelationAttributeMap(relation, variables);
        return RAExpressionAttributes.of(unqualifiedAttributes.entrySet().stream()
                .map(e -> Maps.immutableEntry(e.getKey(), ImmutableMap.of(relationAllIds, e.getValue())))
                .collect(ImmutableCollectors.toMap()));
    }

    @Override
    public RAExpressionAttributes createWithoutName(RelationDefinition relation, ImmutableList<Variable> variables) {
        ImmutableMap<QuotedID, ImmutableTerm> unqualifiedAttributes = getRelationAttributeMap(relation, variables);
        return RAExpressionAttributes.of(unqualifiedAttributes.entrySet().stream()
                .map(e -> Maps.immutableEntry(e.getKey(), ImmutableMap.of(ImmutableSet.<RelationID>of(), e.getValue())))
                .collect(ImmutableCollectors.toMap()));
    }

    @Override
    public RAExpressionAttributes withAlias(RAExpressionAttributes rae, RelationID aliasId) {
        ImmutableMap<QuotedID, ImmutableTerm> unqualifiedAttributes = rae.getUnqualifiedAttributesMap();
        return RAExpressionAttributes.of(unqualifiedAttributes.entrySet().stream()
                .map(e -> Maps.immutableEntry(e.getKey(), ImmutableMap.of(ImmutableSet.of(aliasId), e.getValue())))
                .collect(ImmutableCollectors.toMap()));
    }

    private ImmutableMap<QuotedID, ImmutableTerm> getRelationAttributeMap(RelationDefinition relation, ImmutableList<Variable> variables) {
        return relation.getAttributes().stream()
                .collect(ImmutableCollectors.toMap(Attribute::getID,
                        attribute -> variables.get(attribute.getIndex() - 1)));
    }


    /**
     * CROSS JOIN (also denoted by , in SQL)
     *      (R.X, R.Y) x (S.Y, S.Z) -> R.X, RS.Y, S.Z
     *
     * @param left  an {@link RAExpressionAttributes}
     * @param right an {@link RAExpressionAttributes}
     * @return an {@link RAExpressionAttributes}
     * @throws IllegalJoinException if the same relation alias occurs in both arguments
     */

    @Override
    public RAExpressionAttributes crossJoin(RAExpressionAttributes left, RAExpressionAttributes right) throws IllegalJoinException {
        checkRelationAliasesConsistency(left, right);

        var map = Sets.union(left.getMap().keySet(), right.getMap().keySet()).stream()
                .collect(ImmutableCollectors.toMap(
                        id -> id,
                        id -> Stream.concat(left.getMap().getOrDefault(id, ImmutableMap.of()).entrySet().stream(), right.getMap().getOrDefault(id, ImmutableMap.of()).entrySet().stream())
                                .collect(ImmutableCollectors.toMap())));

        return RAExpressionAttributes.of(map);
    }

    /**
     * JOIN USING
     *      (R.X, R.Y, R.U) and (S.Y, S.Z) using U ->  exception as S.U is missing
     *      (R.X, R.Y) and (S.Y, S.Z, S.U) using U ->  exception as R.U is missing
     *      (R.X, R.Y, R.U) and (S.Y, S.Z, S.U) using U -> R.X, RS.Y, S.Z, R.U
     *            (the choice or R/S is arbitrary, but we keep it unambiguous)
     *
     * @param left  an {@link RAExpressionAttributes}
     * @param right an {@link RAExpressionAttributes}
     * @param using an {@link ImmutableSet}<{@link QuotedID}>
     * @return an {@link RAExpressionAttributes}
     * @throws IllegalJoinException if the same relation alias occurs in both arguments
     *                              or one of the `using` attributes is ambiguous or absent
     */

    @Override
    public RAExpressionAttributes joinUsing(RAExpressionAttributes left, RAExpressionAttributes right, ImmutableSet<QuotedID> using) throws IllegalJoinException {
        checkRelationAliasesConsistency(left, right);

        ImmutableList<QuotedID> absent = using.stream()
                .filter(id -> left.isAbsent(id) || right.isAbsent(id))
                .collect(ImmutableCollectors.toList());

        ImmutableList<QuotedID> ambiguous = using.stream()
                .filter(id -> left.isAmbiguous(id) || right.isAmbiguous(id))
                .collect(ImmutableCollectors.toList());

        if (!absent.isEmpty() || !ambiguous.isEmpty()) {
            throw new IllegalJoinException(left, right, absent, ambiguous);
        }

        var map = Sets.union(left.getMap().keySet(), right.getMap().keySet()).stream()
                .collect(ImmutableCollectors.toMap(
                        id -> id,
                        id -> using.contains(id)
                                ? ImmutableMap.of(ImmutableSet.<RelationID>of(), left.getMap().get(id).entrySet().iterator().next().getValue())
                                : Stream.concat(left.getMap().getOrDefault(id, ImmutableMap.of()).entrySet().stream(), right.getMap().getOrDefault(id, ImmutableMap.of()).entrySet().stream())
                                .collect(ImmutableCollectors.toMap())));

        return RAExpressionAttributes.of(map);
    }


    @Override
    public RAExpressionAttributes joinOn(RAExpressionAttributes left, RAExpressionAttributes right, Function<RAExpressionAttributes, Optional<ImmutableExpression>> getAtomOnExpression) throws IllegalJoinException {
        return crossJoin(left, right);
    }

    @Override
    public ImmutableSet<QuotedID> getSharedAttributeNames(RAExpressionAttributes left, RAExpressionAttributes right) {
        return Sets.intersection(left.getAllUnqualifiedAttributes(), right.getAllUnqualifiedAttributes()).immutableCopy();
    }

    @Override
    public RAExpressionAttributes filter(RAExpressionAttributes rae, Optional<ImmutableExpression> filter) {
        return rae;
    }

    /**
     * throw IllegalJoinException if a relation alias occurs in both arguments of the join
     *
     * @param re2 a {@link RAExpressionAttributes}
     * @throws IllegalJoinException if the same alias occurs in both arguments
     */

    private void checkRelationAliasesConsistency(RAExpressionAttributes re1, RAExpressionAttributes re2) throws IllegalJoinException {
        Sets.SetView<RelationID> intersection = Sets.intersection(getRelationAliases(re1), getRelationAliases(re2));
        if (!intersection.isEmpty())
            throw new IllegalJoinException(re1, re2, intersection.immutableCopy());
    }

    private ImmutableSet<RelationID> getRelationAliases(RAExpressionAttributes rae) {
        return rae.getAttributesMapSelection(QualifiedAttributeID::isQualified)
                .map(Map.Entry::getKey)
                .map(QualifiedAttributeID::getRelation)
                .distinct()
                .collect(ImmutableCollectors.toSet());
    }
}
