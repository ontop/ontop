package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.*;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.IllegalJoinException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by roman on 24/01/2017.
 */
public class RAExpressionAttributes  {

    private final ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes;
    private final RAExpressionAttributeOccurrences occurrences;

    /**
     * constructs a relation expression
     *
     * @param attributes  an {@link ImmutableMap}<{@link QualifiedAttributeID}, {@link ImmutableTerm}>
     * @param occurrences an {@link RAExpressionAttributeOccurrences}>>
     */
    RAExpressionAttributes(ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes,
                                  RAExpressionAttributeOccurrences occurrences) {
        this.attributes = attributes;
        this.occurrences = occurrences;
    }

    public ImmutableMap<QualifiedAttributeID, ImmutableTerm> asMap() {
        return attributes;
    }

    public ImmutableTerm get(QuotedID attributeId) {
        return attributes.get(new QualifiedAttributeID(null, attributeId));
    }

    RAExpressionAttributeOccurrences getOccurrences() { return occurrences; }


    /**
     *
     * @param unqualifiedAttributes a {@link ImmutableMap}<{@link QuotedID}, {@link Variable}>
     * @return a {@link RAExpressionAttributes}
     */

    public static RAExpressionAttributes create(ImmutableMap<QuotedID, ImmutableTerm> unqualifiedAttributes, RelationID relationId,
                                                ImmutableSet<RelationID> allRelationIds) {

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes =
                unqualifiedAttributes.entrySet().stream()
                        .flatMap(e -> createQualifiedID(allRelationIds, e.getKey())
                                        .map(i -> Maps.immutableEntry(i, e.getValue())))
                        .collect(ImmutableCollectors.toMap());

        return new RAExpressionAttributes(attributes,
                RAExpressionAttributeOccurrences.create(
                        unqualifiedAttributes.keySet(), ImmutableSet.of(relationId)));
    }

    public static RAExpressionAttributes create(ImmutableMap<QuotedID, ImmutableTerm> unqualifiedAttributes) {
        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes =
                unqualifiedAttributes.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(e -> new QualifiedAttributeID(null, e.getKey()), Map.Entry::getValue));

        return new RAExpressionAttributes(attributes,
                RAExpressionAttributeOccurrences.create(ImmutableSet.of(), ImmutableSet.of()));
    }

    private static Stream<QualifiedAttributeID> createQualifiedID(ImmutableSet<RelationID> aliases, QuotedID attributeId) {
        return Stream.concat(Stream.of(new QualifiedAttributeID(null, attributeId)),
                        aliases.stream()
                                .flatMap(l -> l.getWithSchemalessID().stream())
                                .distinct()
                                .map(a -> new QualifiedAttributeID(a, attributeId)));
    }



    public ImmutableMap<QuotedID, ImmutableTerm> getUnqualifiedAttributes() {
        return selectAttributes(id -> id.getRelation() == null)
                .collect(ImmutableCollectors.toMap(e -> e.getKey().getAttribute(), Map.Entry::getValue));
    }


    Stream<Map.Entry<QualifiedAttributeID, ImmutableTerm>> selectAttributes(Predicate<QualifiedAttributeID> condition) {
        return attributes.entrySet().stream()
                .filter(e -> condition.test(e.getKey()));
    }


    /**
     * throw IllegalJoinException if a relation alias occurs in both arguments of the join
     *
     * @param re2 a {@link RAExpressionAttributes}
     * @throws IllegalJoinException if the same alias occurs in both arguments
     */

    static void checkRelationAliasesConsistency(RAExpressionAttributes re1, RAExpressionAttributes re2) throws IllegalJoinException {

        Sets.SetView<RelationID> intersection = Sets.intersection(
                re1.getRelationAliases(), re2.getRelationAliases());
        if (!intersection.isEmpty())
            throw new IllegalJoinException(re1, re2, intersection.stream()
                    .map(RelationID::getSQLRendering)
                    .collect(Collectors.joining(", ", "Relation alias ", " occurs in both arguments of the JOIN")));
    }

    private ImmutableSet<RelationID> getRelationAliases() {
        return attributes.keySet().stream()
                .filter(id -> id.getRelation() != null)
                .map(QualifiedAttributeID::getRelation)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public String toString() {
        return "attributes: " + attributes + " with " + occurrences;
    }
}
