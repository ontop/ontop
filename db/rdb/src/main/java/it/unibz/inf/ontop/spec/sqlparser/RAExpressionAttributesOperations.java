package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.sqlparser.exception.IllegalJoinException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.spec.sqlparser.RAExpressionAttributes.Occurrences;

public class RAExpressionAttributesOperations implements RAOperations<RAExpressionAttributes> {

    @Override
    public RAExpressionAttributes create() {
        return RAExpressionAttributes.ofUnqualifiedAttributesMap(ImmutableMap.of());
    }

    @Override
    public RAExpressionAttributes create(NamedRelationDefinition relation, ImmutableList<Variable> variables) {
        ImmutableSet<RelationID> relationAllIds = relation.getAllIDs().stream()
                .flatMap(id -> Stream.of(id, id.getTableOnlyID()))
                .distinct()
                .collect(ImmutableCollectors.toSet());
        ImmutableMap<QuotedID, ImmutableTerm> unqualifiedAttributes = getRelationAttributeMap(relation, variables);
        return RAExpressionAttributes.ofUnqualifiedAttributesMap(unqualifiedAttributes, relationAllIds);
    }

    @Override
    public RAExpressionAttributes createWithoutName(RelationDefinition relation, ImmutableList<Variable> variables) {
        ImmutableMap<QuotedID, ImmutableTerm> unqualifiedAttributes = getRelationAttributeMap(relation, variables);
        return RAExpressionAttributes.ofUnqualifiedAttributesMap(unqualifiedAttributes, ImmutableSet.of());
    }

    @Override
    public RAExpressionAttributes withAlias(RAExpressionAttributes rae, RelationID aliasId) {
        ImmutableMap<QuotedID, ImmutableTerm> unqualifiedAttributes = rae.getUnqualifiedAttributesMap();
        return RAExpressionAttributes.ofUnqualifiedAttributesMap(unqualifiedAttributes, ImmutableSet.of(aliasId));
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
        return RAExpressionAttributes.join(left, right,
                id -> Occurrences.merge(left.getOccurrences(id), right.getOccurrences(id)));
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

        if (using.stream().anyMatch(id -> !left.getOccurrences(id).isUnambiguous() || !right.getOccurrences(id).isUnambiguous())) {
            throw new IllegalJoinException(left, right,
                    using.stream()
                            .filter(id -> left.getOccurrences(id).isAbsent() || right.getOccurrences(id).isAbsent())
                            .collect(ImmutableCollectors.toList()),
                    using.stream()
                            .filter(id -> left.getOccurrences(id).isAmbiguous() || right.getOccurrences(id).isAmbiguous())
                            .collect(ImmutableCollectors.toList()));
        }

        return RAExpressionAttributes.join(left, right,
                id -> using.contains(id)
                        ? Occurrences.of(
                                Sets.union(left.getOccurrences(id).getRelationIDs(), right.getOccurrences(id).getRelationIDs()).immutableCopy(),
                                left.getOccurrences(id).getTerm())
                        : Occurrences.merge(left.getOccurrences(id), right.getOccurrences(id)));
    }


    @Override
    public RAExpressionAttributes joinOn(RAExpressionAttributes left, RAExpressionAttributes right, Function<RAExpressionAttributes, Optional<ImmutableExpression>> getAtomOnExpression) throws IllegalJoinException {
        return crossJoin(left, right);
    }

    @Override
    public ImmutableSet<QuotedID> getSharedAttributeNames(RAExpressionAttributes left, RAExpressionAttributes right) {
        return RAExpressionAttributes.getSharedAttributeNames(left, right).immutableCopy();
    }

    @Override
    public RAExpressionAttributes filter(RAExpressionAttributes rae, Optional<ImmutableExpression> filter) {
        return rae;
    }
}
