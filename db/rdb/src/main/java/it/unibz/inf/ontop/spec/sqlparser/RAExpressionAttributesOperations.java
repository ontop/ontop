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
        return new RAExpressionAttributes(ImmutableMap.of(), ImmutableSet.of(), id -> ImmutableSet.of());
    }

    @Override
    public RAExpressionAttributes create(NamedRelationDefinition relation, ImmutableList<Variable> variables) {
        ImmutableSet<RelationID> relationAllIds = relation.getAllIDs().stream()
                .flatMap(id -> Stream.of(id, id.getTableOnlyID()))
                .distinct()
                .collect(ImmutableCollectors.toSet());
        ImmutableMap<QuotedID, ImmutableTerm> unqualifiedAttributes = getRelationAttributeMap(relation, variables);
        return create(unqualifiedAttributes, relationAllIds, unqualifiedAttributes.keySet(), Optional.of(relation.getID()));
    }

    @Override
    public RAExpressionAttributes createWithoutName(RelationDefinition relation, ImmutableList<Variable> variables) {
        ImmutableMap<QuotedID, ImmutableTerm> unqualifiedAttributes = getRelationAttributeMap(relation, variables);
        return create(unqualifiedAttributes, ImmutableSet.of(), unqualifiedAttributes.keySet(), Optional.empty());
    }

    @Override
    public RAExpressionAttributes withAlias(RAExpressionAttributes rae, RelationID aliasId) {
        return create(rae.getUnqualifiedAttributesMap(), ImmutableSet.of(aliasId), rae.getAllUnqualifiedAttributes(), Optional.of(aliasId));
    }

    private ImmutableMap<QuotedID, ImmutableTerm> getRelationAttributeMap(RelationDefinition relation, ImmutableList<Variable> variables) {
        return relation.getAttributes().stream()
                .collect(ImmutableCollectors.toMap(Attribute::getID,
                        attribute -> variables.get(attribute.getIndex() - 1)));
    }

    private RAExpressionAttributes create(ImmutableMap<QuotedID, ImmutableTerm> attributeMap, ImmutableSet<RelationID> relationAllIds, ImmutableSet<QuotedID> allUnqualifiedAttributes, Optional<RelationID> optionalRelationId) {

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributeMapWithAliases = attributeMap.entrySet().stream()
                .flatMap(e -> Stream.concat(
                                Stream.of(new QualifiedAttributeID(null, e.getKey())),
                                relationAllIds.stream()
                                        .map(a -> new QualifiedAttributeID(a, e.getKey())))
                        .map(i -> Maps.immutableEntry(i, e.getValue())))
                .collect(ImmutableCollectors.toMap());

        ImmutableSet<RelationID> relationIds = optionalRelationId.map(ImmutableSet::of).orElseGet(ImmutableSet::of);

        return new RAExpressionAttributes(attributeMapWithAliases, allUnqualifiedAttributes, id -> relationIds);
    }


    /**
     * CROSS JOIN (also denoted by , in SQL)
     *
     * @param left  an {@link RAExpressionAttributes}
     * @param right an {@link RAExpressionAttributes}
     * @return an {@link RAExpressionAttributes}
     * @throws IllegalJoinException if the same relation alias occurs in both arguments
     */

    @Override
    public RAExpressionAttributes crossJoin(RAExpressionAttributes left, RAExpressionAttributes right) throws IllegalJoinException {
        checkRelationAliasesConsistency(left, right);

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes = Stream.concat(
                        left.getAttributesMapSelection(id -> id.isQualified() || right.isAbsent(id.getAttribute())),
                        right.getAttributesMapSelection(id -> id.isQualified() || left.isAbsent(id.getAttribute())))
                .collect(ImmutableCollectors.toMap());

        return new RAExpressionAttributes(
                attributes,
                /*
                 *   R.X, R.Y and S.X, S.Y -> R.X, RS.Y, S.Y
                 */
                union(left.getAllUnqualifiedAttributes(), right.getAllUnqualifiedAttributes()),
                id -> union(left.getOccurrences(id), right.getOccurrences(id)));
    }

    /**
     * JOIN USING
     *
     * @param left  an {@link RAExpressionAttributes}
     * @param right an {@link RAExpressionAttributes}
     * @param using an {@link ImmutableSet}<{@link QuotedID}>
     * @return an {@link RAExpressionAttributes}
     * @throws IllegalJoinException if the same relatio alias occurs in both arguments
     *                              or one of the `using' attributes is ambiguous or absent
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

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes = Stream.concat(
                        left.getAttributesMapSelection(id ->
                                (id.isQualified() && !using.contains(id.getAttribute()))
                                        || (!id.isQualified() && right.isAbsent(id.getAttribute()))
                                        || (!id.isQualified() && using.contains(id.getAttribute()))),

                        right.getAttributesMapSelection(id ->
                                (id.isQualified() && !using.contains(id.getAttribute()))
                                        || (!id.isQualified() && left.isAbsent(id.getAttribute()))))

                .collect(ImmutableCollectors.toMap());

        return new RAExpressionAttributes(
                attributes,
                union(left.getAllUnqualifiedAttributes(), right.getAllUnqualifiedAttributes()),
                /*
                 *      R.X, R.Y, R.U and S.Y, S.Z using U ->  empty
                 *      R.X, R.Y and S.Y, S.Z, S.U using U ->  empty
                 *      R.X, R.Y, R.U and S.Y, S.Z, S.U using U -> R.X, RS.Y, S.Y, R.U
                 *            (the choice or R/S is arbitrary, but we keep it unambiguous)
                 */
                id -> using.contains(id)
                        ? left.getOccurrences(id)
                        : union(left.getOccurrences(id), right.getOccurrences(id)));
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

    private static <T> ImmutableSet<T> union(ImmutableSet<T> left, ImmutableSet<T> right) {
        return Sets.union(left, right).immutableCopy();
    }
}
