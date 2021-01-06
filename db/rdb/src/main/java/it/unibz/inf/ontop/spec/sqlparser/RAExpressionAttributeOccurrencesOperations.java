package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.sqlparser.exception.IllegalJoinException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

public class RAExpressionAttributeOccurrencesOperations implements RAOperations<RAExpressionAttributeOccurrences> {


    @Override
    public RAExpressionAttributeOccurrences create() {
        return new RAExpressionAttributeOccurrences(ImmutableMap.of());
    }

    @Override
    public RAExpressionAttributeOccurrences create(NamedRelationDefinition relation, ImmutableList<Variable> variables) {
        ImmutableSet<RelationID> relationIds = ImmutableSet.of(relation.getID());
        return new RAExpressionAttributeOccurrences(relation.getAttributes().stream()
                .collect(ImmutableCollectors.toMap(
                        Attribute::getID,
                        id -> relationIds)));
    }

    public RAExpressionAttributeOccurrences create(ImmutableSet<QuotedID> attributeIds, ImmutableSet<RelationID> relationIds) {
        return new RAExpressionAttributeOccurrences(attributeIds.stream()
                .collect(ImmutableCollectors.toMap(
                        Function.identity(),
                        id -> relationIds)));
    }

    @Override
    public RAExpressionAttributeOccurrences withAlias(RAExpressionAttributeOccurrences rae, RelationID aliasId) {
        ImmutableSet<RelationID> relationIds = ImmutableSet.of(aliasId);
        return new RAExpressionAttributeOccurrences(rae.getAttributes().stream()
                .collect(ImmutableCollectors.toMap(
                        Function.identity(),
                        id -> relationIds)));
    }


    /**
     * non-qualified attribute occurrences for CROSS JOIN
     *
     * @param left an {@link RAExpressionAttributeOccurrences}
     * @param right an {@link RAExpressionAttributeOccurrences}
     * @return an {@link RAExpressionAttributeOccurrences}
     *      R.X, R.Y and S.X, S.Y -> R.X, RS.Y, S.Y
     */

    @Override
    public RAExpressionAttributeOccurrences crossJoin(RAExpressionAttributeOccurrences left, RAExpressionAttributeOccurrences right) throws IllegalJoinException {
        return new RAExpressionAttributeOccurrences(idUnionStream(left, right)
            .collect(ImmutableCollectors.toMap(
                    Function.identity(),
                    unionOf(left, right))));
    }

    /**
     * non-qualified attribute occurrences for JOIN USING
     *
     * @param left an {@link RAExpressionAttributeOccurrences}
     * @param right an {@link RAExpressionAttributeOccurrences}
     * @return an {@link RAExpressionAttributeOccurrences} or null
     *      R.X, R.Y, R.U and S.Y, S.Z using U ->  empty
     *      R.X, R.Y and S.Y, S.Z, S.U using U ->  empty
     *      R.X, R.Y, R.U and S.Y, S.Z, S.U using U -> R.X, RS.Y, S.Y, R.U
     *            (the choice or R/S is arbitrary, but we keep it unambiguous)
     */

    @Override
    public RAExpressionAttributeOccurrences joinUsing(RAExpressionAttributeOccurrences left, RAExpressionAttributeOccurrences right, ImmutableSet<QuotedID> using) throws IllegalJoinException {
        if (!using.stream().allMatch(left::isUnique) || !using.stream().allMatch(right::isUnique))
            return null;

        Function<QuotedID, ImmutableSet<RelationID>> u = unionOf(left, right);
        return new RAExpressionAttributeOccurrences(idUnionStream(left, right)
                .collect(ImmutableCollectors.toMap(
                        Function.identity(),
                        id -> using.contains(id) ? left.get(id) : u.apply(id))));
    }

    @Override
    public RAExpressionAttributeOccurrences joinOn(RAExpressionAttributeOccurrences left, RAExpressionAttributeOccurrences right, Function<RAExpressionAttributes, ImmutableList<ImmutableExpression>> getAtomOnExpression) throws IllegalJoinException {
        return crossJoin(left, right);
    }

    @Override
    public ImmutableSet<QuotedID> getSharedAttributeNames(RAExpressionAttributeOccurrences left, RAExpressionAttributeOccurrences right) {
        return Sets.intersection(left.getAttributes(), right.getAttributes()).immutableCopy();
    }

    @Override
    public RAExpressionAttributeOccurrences filter(RAExpressionAttributeOccurrences rae, ImmutableList<ImmutableExpression> filter) {
        return rae;
    }


    private static Stream<QuotedID> idUnionStream(RAExpressionAttributeOccurrences o1, RAExpressionAttributeOccurrences o2) {
        return Stream.of(o1, o2)
                .map(RAExpressionAttributeOccurrences::getAttributes)
                .flatMap(Collection::stream)
                .distinct();
    }

    private static Function<QuotedID, ImmutableSet<RelationID>> unionOf(RAExpressionAttributeOccurrences left, RAExpressionAttributeOccurrences right) {
        return id -> Sets.union(left.get(id), right.get(id)).immutableCopy();
    }
}
