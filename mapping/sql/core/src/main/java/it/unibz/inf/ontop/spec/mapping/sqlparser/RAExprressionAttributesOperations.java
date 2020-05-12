package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.DatabaseRelationDefinition;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.IllegalJoinException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class RAExprressionAttributesOperations implements RAOperations<RAExpressionAttributes> {


    @Override
    public RAExpressionAttributes withAlias(RAExpressionAttributes rae, RelationID aliasId) {
        return RAExpressionAttributes.create(rae.getUnqualifiedAttributes(), aliasId, ImmutableSet.of(aliasId));
    }

    /**
     * CROSS JOIN (also denoted by , in SQL)
     *
     * @param left an {@link RAExpressionAttributes}
     * @param right an {@link RAExpressionAttributes}
     * @return an {@link RAExpressionAttributes}
     * @throws IllegalJoinException if the same relation alias occurs in both arguments
     */

    @Override
    public RAExpressionAttributes crossJoin(RAExpressionAttributes left, RAExpressionAttributes right) throws IllegalJoinException {
        RAExpressionAttributes.checkRelationAliasesConsistency(left, right);

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes = Stream.concat(
                left.selectAttributes(id ->
                        (id.getRelation() != null) || right.getOccurrences().isAbsent(id.getAttribute())),

                right.selectAttributes(id ->
                        (id.getRelation() != null) || left.getOccurrences().isAbsent(id.getAttribute())))

                .collect(ImmutableCollectors.toMap());

        return new RAExpressionAttributes(attributes,
                RAExpressionAttributeOccurrences.crossJoin(left.getOccurrences(), right.getOccurrences()));
    }

    /**
     * JOIN USING
     *
     * @param left an {@link RAExpressionAttributes}
     * @param right an {@link RAExpressionAttributes}
     * @param using an {@link ImmutableSet}<{@link QuotedID}>
     * @return an {@link RAExpressionAttributes}
     * @throws IllegalJoinException if the same relatio alias occurs in both arguments
     *          or one of the `using' attributes is ambiguous or absent
     */

    @Override
    public RAExpressionAttributes joinUsing(RAExpressionAttributes left, RAExpressionAttributes right, ImmutableSet<QuotedID> using) throws IllegalJoinException {
        RAExpressionAttributes.checkRelationAliasesConsistency(left, right);

        Optional<RAExpressionAttributeOccurrences> occurrences = RAExpressionAttributeOccurrences.joinUsing(
                left.getOccurrences(), right.getOccurrences(), using);
        if (!occurrences.isPresent()) {

            ImmutableList<QuotedID> notFound = using.stream()
                    .filter(id -> left.getOccurrences().isAbsent(id) || right.getOccurrences().isAbsent(id))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<QuotedID> ambiguous = using.stream()
                    .filter(id -> left.getOccurrences().isAmbiguous(id) || right.getOccurrences().isAmbiguous(id))
                    .collect(ImmutableCollectors.toList());

            throw new IllegalJoinException(left, right,
                    (!notFound.isEmpty() ? "Attribute(s) " + notFound + " cannot be found" : "") +
                            (!notFound.isEmpty() && !ambiguous.isEmpty() ? ", " : "") +
                            (!ambiguous.isEmpty() ? "Attribute(s) " + ambiguous + " are ambiguous" : ""));
        }

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes = Stream.concat(
                left.selectAttributes(id ->
                        (id.getRelation() != null && !using.contains(id.getAttribute()))
                                || (id.getRelation() == null && right.getOccurrences().isAbsent(id.getAttribute()))
                                || (id.getRelation() == null && using.contains(id.getAttribute()))),

                right.selectAttributes(id ->
                        (id.getRelation() != null && !using.contains(id.getAttribute()))
                                || (id.getRelation() == null && left.getOccurrences().isAbsent(id.getAttribute()))))

                .collect(ImmutableCollectors.toMap());

        return new RAExpressionAttributes(attributes, occurrences.get());
    }

    @Override
    public RAExpressionAttributes joinOn(RAExpressionAttributes left, RAExpressionAttributes right, Function<RAExpressionAttributes, ImmutableList<ImmutableExpression>> getAtomOnExpression) throws IllegalJoinException {
        return crossJoin(left, right);
    }

    @Override
    public ImmutableSet<QuotedID> getSharedAttributeNames(RAExpressionAttributes left, RAExpressionAttributes right) {
        return RAExpressionAttributeOccurrences.getShared(left.getOccurrences(), right.getOccurrences());
    }
}
