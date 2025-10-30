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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RAExpressionAttributesOperations implements RAOperations<RAExpressionAttributes> {

    @Override
    public RAExpressionAttributes create() {
        return new RAExpressionAttributes(ImmutableMap.of(), ImmutableMap.of());
    }

    @Override
    public RAExpressionAttributes create(NamedRelationDefinition relation, ImmutableList<Variable> variables) {
        ImmutableMap<QuotedID, ImmutableTerm> map = getAttributesMap(relation, variables);
        ImmutableSet<RelationID> relationIds = ImmutableSet.of(relation.getID());
        return new RAExpressionAttributes(
                attachAliases(map, relation.getAllIDs().stream()
                        .flatMap(id -> Stream.of(id, id.getTableOnlyID()))
                        .distinct()
                        .collect(ImmutableCollectors.toSet())),
                create(relation.getAttributes().stream().map(Attribute::getID), id1 -> relationIds));
    }

    public RAExpressionAttributes create(ImmutableMap<QuotedID, ImmutableTerm> unqualifiedAttributes) {
        return new RAExpressionAttributes(
                attachAliases(unqualifiedAttributes, ImmutableSet.of()),
                create(unqualifiedAttributes.keySet().stream(), id -> ImmutableSet.of()));
    }

    @Override
    public RAExpressionAttributes withAlias(RAExpressionAttributes rae, RelationID aliasId) {
        ImmutableMap<QuotedID, ImmutableTerm> unqualifiedAttributes = rae.getUnqualifiedAttributes();
        ImmutableSet<RelationID> relationIds = ImmutableSet.of(aliasId);
        return new RAExpressionAttributes(attachAliases(unqualifiedAttributes, ImmutableSet.of(aliasId)),
                create(rae.getAttributes().stream(), id -> relationIds));
    }

    private ImmutableMap<QuotedID, ImmutableSet<RelationID>> create(Stream<QuotedID> attributeIds, Function<QuotedID, ImmutableSet<RelationID>> relationIdsFunction) {
        return attributeIds
                .collect(ImmutableCollectors.toMap(id -> id, relationIdsFunction));
    }

    private static boolean isQualified(QualifiedAttributeID id) {
        return id.getRelation() != null;
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
                        left.selectAttributes(id ->
                                isQualified(id) || right.isAbsent(id.getAttribute())),
                        right.selectAttributes(id ->
                                isQualified(id) || left.isAbsent(id.getAttribute())))
                .collect(ImmutableCollectors.toMap());

        return new RAExpressionAttributes(attributes,
                /*
                    R.X, R.Y and S.X, S.Y -> R.X, RS.Y, S.Y
                */
                create(idUnionStream(left, right), id -> unionOf(id, left, right)));
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

        if (!using.stream().allMatch(left::isUnique) || !using.stream().allMatch(right::isUnique)) {
            throw new IllegalJoinException(left, right,
                    using.stream()
                            .filter(id -> left.isAbsent(id) || right.isAbsent(id))
                            .collect(ImmutableCollectors.toList()),
                    using.stream()
                            .filter(id1 -> left.isAmbiguous(id1) || right.isAmbiguous(id1))
                            .collect(ImmutableCollectors.toList()));
        }

        /*
         *      R.X, R.Y, R.U and S.Y, S.Z using U ->  empty
         *      R.X, R.Y and S.Y, S.Z, S.U using U ->  empty
         *      R.X, R.Y, R.U and S.Y, S.Z, S.U using U -> R.X, RS.Y, S.Y, R.U
         *            (the choice or R/S is arbitrary, but we keep it unambiguous)
         */

        ImmutableMap<QuotedID, ImmutableSet<RelationID>> occurrences = create(idUnionStream(left, right),
                id -> using.contains(id) ? left.getOccurrences(id) : unionOf(id, left, right));

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes = Stream.concat(
                        left.selectAttributes(id ->
                                (isQualified(id) && !using.contains(id.getAttribute()))
                                        || (!isQualified(id) && right.isAbsent(id.getAttribute()))
                                        || (!isQualified(id) && using.contains(id.getAttribute()))),

                        right.selectAttributes(id ->
                                (isQualified(id) && !using.contains(id.getAttribute()))
                                        || (!isQualified(id) && left.isAbsent(id.getAttribute()))))

                .collect(ImmutableCollectors.toMap());

        return new RAExpressionAttributes(attributes, occurrences);
    }


    @Override
    public RAExpressionAttributes joinOn(RAExpressionAttributes left, RAExpressionAttributes right, Function<RAExpressionAttributes, Optional<ImmutableExpression>> getAtomOnExpression) throws IllegalJoinException {
        return crossJoin(left, right);
    }

    @Override
    public ImmutableSet<QuotedID> getSharedAttributeNames(RAExpressionAttributes left, RAExpressionAttributes right) {
        return Sets.intersection(left.getAttributes(), right.getAttributes()).immutableCopy();
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
        ImmutableSet<RelationID> intersection = Sets.intersection(getRelationAliases(re1), getRelationAliases(re2)).immutableCopy();
        if (!intersection.isEmpty())
            throw new IllegalJoinException(re1, re2, intersection);
    }

    private ImmutableSet<RelationID> getRelationAliases(RAExpressionAttributes rae) {
        return rae.selectAttributes(RAExpressionAttributesOperations::isQualified)
                .map(Map.Entry::getKey)
                .map(QualifiedAttributeID::getRelation)
                .collect(ImmutableCollectors.toSet());
    }




    public ImmutableMap<QuotedID, ImmutableTerm> getAttributesMap(RelationDefinition relation, ImmutableList<Variable> variables) {
        return relation.getAttributes().stream()
                .collect(ImmutableCollectors.toMap(Attribute::getID,
                        attribute -> variables.get(attribute.getIndex() - 1)));
    }

    private ImmutableMap<QualifiedAttributeID, ImmutableTerm> attachAliases(ImmutableMap<QuotedID, ImmutableTerm> unqualifiedAttributes, ImmutableSet<RelationID> allRelationIds) {
        return unqualifiedAttributes.entrySet().stream()
                        .flatMap(e -> Stream.concat(
                                Stream.of(new QualifiedAttributeID(null, e.getKey())),
                                allRelationIds.stream()
                                        .map(a -> new QualifiedAttributeID(a, e.getKey())))
                                .map(i -> Maps.immutableEntry(i, e.getValue())))
                        .collect(ImmutableCollectors.toMap());
    }

    private static Stream<QuotedID> idUnionStream(RAExpressionAttributes o1, RAExpressionAttributes o2) {
        return Stream.of(o1, o2)
                .map(RAExpressionAttributes::getAttributes)
                .flatMap(ImmutableSet::stream)
                .distinct();
    }

    private static ImmutableSet<RelationID> unionOf(QuotedID id, RAExpressionAttributes left, RAExpressionAttributes right) {
        return Sets.union(left.getOccurrences(id), right.getOccurrences(id)).immutableCopy();
    }
}
