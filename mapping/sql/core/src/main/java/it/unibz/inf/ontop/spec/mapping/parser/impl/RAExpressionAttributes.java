package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.spec.mapping.parser.exception.IllegalJoinException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;

import static java.util.function.Function.identity;

/**
 * Created by roman on 24/01/2017.
 */
public class RAExpressionAttributes {

    private ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes;
    private ImmutableMap<QuotedID, ImmutableSet<RelationID>> attributeOccurrences;

    /**
     * constructs a relation expression
     *
     * @param attributes           an {@link ImmutableMap}<{@link QualifiedAttributeID}, {@link Variable}>
     * @param attributeOccurrences an {@link ImmutableMap}<{@link QuotedID}, {@link ImmutableSet}<{@link RelationID}>>
     */
    public RAExpressionAttributes(ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes,
                                  ImmutableMap<QuotedID, ImmutableSet<RelationID>> attributeOccurrences) {
        this.attributes = attributes;
        this.attributeOccurrences = attributeOccurrences;
    }

    /**
     * checks if the attributeOccurrences contains the attribute
     *
     * @param attribute a  {@link QuotedID}
     * @return true if attributeOccurrences contains the attribute otherwise false
     */

    private boolean isAbsent(QuotedID attribute) {
        ImmutableSet<RelationID> occurrences = attributeOccurrences.get(attribute);
        return (occurrences == null) || occurrences.isEmpty();
    }

    private boolean isAmbiguous(QuotedID attribute) {
        ImmutableSet<RelationID> occurrences = attributeOccurrences.get(attribute);
        return (occurrences != null) && occurrences.size() > 1;
    }

    private boolean isUnique(QuotedID attribute) {
        ImmutableSet<RelationID> occurrences = attributeOccurrences.get(attribute);
        return (occurrences != null) && occurrences.size() == 1;
    }

    public ImmutableMap<QualifiedAttributeID, ImmutableTerm> getAttributes() {
        return attributes;
    }

    /**
     * CROSS JOIN (also denoted by , in SQL)
     *
     * @param re1 a {@link RAExpressionAttributes}
     * @param re2 a {@link RAExpressionAttributes}
     * @return a {@link RAExpressionAttributes}
     * @throws IllegalJoinException if the same alias occurs in both arguments
     */
    public static RAExpressionAttributes crossJoin(RAExpressionAttributes re1, RAExpressionAttributes re2) throws IllegalJoinException {

        checkRelationAliasesConsistency(re1, re2);

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes = merge(
                re1.selectAttributes(id ->
                        (id.getRelation() != null) || re2.isAbsent(id.getAttribute())),

                re2.selectAttributes(id ->
                        (id.getRelation() != null) || re1.isAbsent(id.getAttribute())));

        return new RAExpressionAttributes(attributes,
                getAttributeOccurrences(re1, re2, id -> attributeOccurrencesUnion(id, re1, re2)));
    }

    /**
     * NATURAL JOIN
     *
     * @param re1 a {@link RAExpressionAttributes}
     * @param re2 a {@link RAExpressionAttributes}
     * @return a {@link ImmutableSet}<{@link QuotedID}>
     */

    public static ImmutableSet<QuotedID> getShared(RAExpressionAttributes re1,
                                                   RAExpressionAttributes re2) {

        return re1.attributeOccurrences.keySet().stream()
                .filter(id -> !re1.isAbsent(id) && !re2.isAbsent(id))
                .collect(ImmutableCollectors.toSet());
    }

    /**
     * JOIN USING
     *
     * @param re1 a {@link RAExpressionAttributes}
     * @param re2 a {@link RAExpressionAttributes}
     * @param using a {@link ImmutableSet}<{@link QuotedID}>
     * @return a {@link RAExpressionAttributes}
     * @throws IllegalJoinException if the same alias occurs in both arguments
     *          or one of the `using' attributes is ambiguous or absent
     */

    public static RAExpressionAttributes joinUsing(RAExpressionAttributes re1,
                                                   RAExpressionAttributes re2,
                                                   ImmutableSet<QuotedID> using) throws IllegalJoinException {

        checkRelationAliasesConsistency(re1, re2);

        if (using.stream().anyMatch(id -> !re1.isUnique(id) || !re2.isUnique(id))) {

            ImmutableList<QuotedID> notFound = using.stream()
                    .filter(id -> re1.isAbsent(id) || re2.isAbsent(id))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<QuotedID> ambiguous = using.stream()
                    .filter(id -> re1.isAmbiguous(id) || re2.isAmbiguous(id))
                    .collect(ImmutableCollectors.toList());

            throw new IllegalJoinException(re1, re2,
                    (!notFound.isEmpty() ? "Attribute(s) " + notFound + " cannot be found" : "") +
                            (!notFound.isEmpty() && !ambiguous.isEmpty() ? ", " : "") +
                            (!ambiguous.isEmpty() ? "Attribute(s) " + ambiguous + " are ambiguous" : ""));
        }

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes = merge(
                re1.selectAttributes(id ->
                        (id.getRelation() != null && !using.contains(id.getAttribute()))
                                || (id.getRelation() == null && re2.isAbsent(id.getAttribute()))
                                || (id.getRelation() == null && using.contains(id.getAttribute()))),

                re2.selectAttributes(id ->
                        (id.getRelation() != null && !using.contains(id.getAttribute()))
                                || (id.getRelation() == null && re1.isAbsent(id.getAttribute()))));

        ImmutableMap<QuotedID, ImmutableSet<RelationID>> attributeOccurrences =
                getAttributeOccurrences(re1, re2,
                        id -> using.contains(id)
                                ? re1.attributeOccurrences.get(id)
                                : attributeOccurrencesUnion(id, re1, re2));

        return new RAExpressionAttributes(attributes, attributeOccurrences);
    }

    /**
     *
     * @param unqualifiedAttributes a {@link ImmutableMap}<{@link QuotedID}, {@link Variable}>
     * @param alias a {@link RelationID}
     * @return a {@link RAExpressionAttributes}
     */

    public static RAExpressionAttributes create(ImmutableMap<QuotedID, ImmutableTerm> unqualifiedAttributes,
                                                RelationID alias) {

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes = merge(
                unqualifiedAttributes.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                e -> new QualifiedAttributeID(alias, e.getKey()), Map.Entry::getValue)),

                unqualifiedAttributes.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                e -> new QualifiedAttributeID(null, e.getKey()), Map.Entry::getValue))
        );

        ImmutableMap<QuotedID, ImmutableSet<RelationID>> attributeOccurrences =
                unqualifiedAttributes.keySet().stream()
                        .collect(ImmutableCollectors.toMap(identity(), id -> ImmutableSet.of(alias)));

        return new RAExpressionAttributes(attributes, attributeOccurrences);
    }

    public static RAExpressionAttributes create(ImmutableMap<QuotedID, ImmutableTerm> unqualifiedAttributes,
                                                RelationID alias, RelationID schemalessId) {

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes = merge(
                unqualifiedAttributes.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                e -> new QualifiedAttributeID(alias, e.getKey()), Map.Entry::getValue)),

                unqualifiedAttributes.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                e -> new QualifiedAttributeID(schemalessId, e.getKey()), Map.Entry::getValue)),

                unqualifiedAttributes.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                e -> new QualifiedAttributeID(null, e.getKey()), Map.Entry::getValue))
        );

        ImmutableMap<QuotedID, ImmutableSet<RelationID>> attributeOccurrences =
                unqualifiedAttributes.keySet().stream()
                        .collect(ImmutableCollectors.toMap(identity(), id -> ImmutableSet.of(alias)));

        return new RAExpressionAttributes(attributes, attributeOccurrences);
    }

    /**
     * (relational expression) AS A
     *
     * @param re a {@link RAExpressionAttributes}
     * @param alias a {@link QuotedID}
     * @return a {@link RAExpressionAttributes}
     */

    public static RAExpressionAttributes alias(RAExpressionAttributes re, RelationID alias) {

        ImmutableMap<QuotedID, ImmutableTerm> unqualifiedAttributes =
                re.attributes.entrySet().stream()
                        .filter(e -> e.getKey().getRelation() == null)
                        .collect(ImmutableCollectors.toMap(
                                e -> e.getKey().getAttribute(), Map.Entry::getValue));

        return create(unqualifiedAttributes, alias);
    }


    private static ImmutableMap<QualifiedAttributeID, ImmutableTerm> merge(ImmutableMap<QualifiedAttributeID, ImmutableTerm> attrs1,
                                                                      ImmutableMap<QualifiedAttributeID, ImmutableTerm> attrs2) {
        return ImmutableMap.<QualifiedAttributeID, ImmutableTerm>builder().putAll(attrs1).putAll(attrs2).build();
    }

    private static ImmutableMap<QualifiedAttributeID, ImmutableTerm> merge(ImmutableMap<QualifiedAttributeID, ImmutableTerm> attrs1,
                                                                      ImmutableMap<QualifiedAttributeID, ImmutableTerm> attrs2,
                                                                      ImmutableMap<QualifiedAttributeID, ImmutableTerm> attrs3) {
        return ImmutableMap.<QualifiedAttributeID, ImmutableTerm>builder().putAll(attrs1).putAll(attrs2).putAll(attrs3).build();
    }

    /**
     * treats null values as empty sets
     *
     * @param id a {@link QuotedID}
     * @param re1 a {@link RAExpressionAttributes}
     * @param re2 a {@link RAExpressionAttributes}
     * @return the union of occurrences of id in e1 and e2
     */

    private static ImmutableSet<RelationID> attributeOccurrencesUnion(QuotedID id,
                                                                      RAExpressionAttributes re1,
                                                                      RAExpressionAttributes re2) {

        ImmutableSet<RelationID> s1 = re1.attributeOccurrences.get(id);
        ImmutableSet<RelationID> s2 = re2.attributeOccurrences.get(id);

        if (s1 == null)
            return s2;

        if (s2 == null)
            return s1;

        return ImmutableSet.<RelationID>builder().addAll(s1).addAll(s2).build();
    }

    private ImmutableMap<QualifiedAttributeID, ImmutableTerm> selectAttributes(java.util.function.Predicate<QualifiedAttributeID> condition) {

        return attributes.entrySet().stream()
                .filter(e -> condition.test(e.getKey()))
                .collect(ImmutableCollectors.toMap());
    }


    private static ImmutableMap<QuotedID, ImmutableSet<RelationID>>
    getAttributeOccurrences(RAExpressionAttributes re1,
                            RAExpressionAttributes re2,
                            java.util.function.Function<QuotedID, ImmutableSet<RelationID>> collector) {

        ImmutableSet<QuotedID> keys = ImmutableSet.<QuotedID>builder()
                .addAll(re1.attributeOccurrences.keySet())
                .addAll(re2.attributeOccurrences.keySet())
                .build();

        return keys.stream()
                .collect(ImmutableCollectors.toMap(identity(), collector));
    }


    /**
     * throw IllegalJoinException if a relation alias occurs in both arguments of the join
     *
     * @param re1 a {@link RAExpressionAttributes}
     * @param re2 a {@link RAExpressionAttributes}
     * @throws IllegalJoinException if the same alias occurs in both arguments
     */

    private static void checkRelationAliasesConsistency(RAExpressionAttributes re1,
                                                        RAExpressionAttributes re2) throws IllegalJoinException {

        ImmutableSet<RelationID> alias1 = re1.attributes.keySet().stream()
                .filter(id -> id.getRelation() != null)
                .map(QualifiedAttributeID::getRelation).collect(ImmutableCollectors.toSet());

        ImmutableSet<RelationID> alias2 = re2.attributes.keySet().stream()
                .filter(id -> id.getRelation() != null)
                .map(QualifiedAttributeID::getRelation).collect(ImmutableCollectors.toSet());

        if (alias1.stream().anyMatch(alias2::contains))
            throw new IllegalJoinException(re1, re2 ,"Relation alias " +
                    alias1.stream().filter(alias2::contains).collect(ImmutableCollectors.toList()) +
                    " occurs in both arguments of the JOIN");
    }


    @Override
    public String toString() {
        return "attributes: " + attributes + " with " + attributeOccurrences;
    }
}
