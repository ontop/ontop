package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.*;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.IllegalJoinException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by roman on 24/01/2017.
 */
public class RAExpressionAttributes {

    private final ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes;
    private final RAExpressionAttributeOccurrences occurrences;

    /**
     * constructs a relation expression
     *
     * @param attributes  an {@link ImmutableMap}<{@link QualifiedAttributeID}, {@link ImmutableTerm}>
     * @param occurrences an {@link RAExpressionAttributeOccurrences}>>
     */
    public RAExpressionAttributes(ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes,
                                  RAExpressionAttributeOccurrences occurrences) {
        this.attributes = attributes;
        this.occurrences = occurrences;
    }

    /**
     * constructs a relation expression
     *
     * @param attributes     an {@link ImmutableMap}<{@link QualifiedAttributeID}, {@link ImmutableTerm}>
     * @param occurrencesMap an {@link ImmutableMap}<{@link QuotedID}, {@link ImmutableSet}<{@link Variable}>>
     */
    public RAExpressionAttributes(ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes,
                                  ImmutableMap<QuotedID, ImmutableSet<RelationID>> occurrencesMap) {
        this(attributes, new RAExpressionAttributeOccurrences(occurrencesMap));
    }

    /**
     * constructs a relation expression
     *
     * @param attributes  an {@link ImmutableMap}<{@link QualifiedAttributeID}, {@link ImmutableTerm}>
     */
    public RAExpressionAttributes(ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes) {
        this.attributes = attributes;
        this.occurrences = null;
    }

    public ImmutableMap<QualifiedAttributeID, ImmutableTerm> getAttributes() {
        return attributes;
    }


    /**
     * CROSS JOIN (also denoted by , in SQL)
     *
     * @param re1 an {@link RAExpressionAttributes}
     * @param re2 an {@link RAExpressionAttributes}
     * @return an {@link RAExpressionAttributes}
     * @throws IllegalJoinException if the same relation alias occurs in both arguments
     */
    public static RAExpressionAttributes crossJoin(RAExpressionAttributes re1, RAExpressionAttributes re2) throws IllegalJoinException {

        checkRelationAliasesConsistency(re1, re2);

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes = Stream.concat(
                re1.selectAttributes(id ->
                        (id.getRelation() != null) || re2.occurrences.isAbsent(id.getAttribute())),

                re2.selectAttributes(id ->
                        (id.getRelation() != null) || re1.occurrences.isAbsent(id.getAttribute())))

                .collect(ImmutableCollectors.toMap());

        return new RAExpressionAttributes(attributes,
                RAExpressionAttributeOccurrences.crossJoin(re1.occurrences, re2.occurrences));
    }


    /**
     * JOIN USING
     *
     * @param re1 an {@link RAExpressionAttributes}
     * @param re2 an {@link RAExpressionAttributes}
     * @param using an {@link ImmutableSet}<{@link QuotedID}>
     * @return an {@link RAExpressionAttributes}
     * @throws IllegalJoinException if the same relatio alias occurs in both arguments
     *          or one of the `using' attributes is ambiguous or absent
     */

    public static RAExpressionAttributes joinUsing(RAExpressionAttributes re1,
                                                   RAExpressionAttributes re2,
                                                   ImmutableSet<QuotedID> using) throws IllegalJoinException {

        checkRelationAliasesConsistency(re1, re2);

        Optional<RAExpressionAttributeOccurrences> occurrences = RAExpressionAttributeOccurrences.joinUsing(
                re1.occurrences, re2.occurrences, using);
        if (!occurrences.isPresent()) {

            ImmutableList<QuotedID> notFound = using.stream()
                    .filter(id -> re1.occurrences.isAbsent(id) || re2.occurrences.isAbsent(id))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<QuotedID> ambiguous = using.stream()
                    .filter(id -> re1.occurrences.isAmbiguous(id) || re2.occurrences.isAmbiguous(id))
                    .collect(ImmutableCollectors.toList());

            throw new IllegalJoinException(re1, re2,
                    (!notFound.isEmpty() ? "Attribute(s) " + notFound + " cannot be found" : "") +
                            (!notFound.isEmpty() && !ambiguous.isEmpty() ? ", " : "") +
                            (!ambiguous.isEmpty() ? "Attribute(s) " + ambiguous + " are ambiguous" : ""));
        }

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes = Stream.concat(
                re1.selectAttributes(id ->
                        (id.getRelation() != null && !using.contains(id.getAttribute()))
                                || (id.getRelation() == null && re2.occurrences.isAbsent(id.getAttribute()))
                                || (id.getRelation() == null && using.contains(id.getAttribute()))),

                re2.selectAttributes(id ->
                        (id.getRelation() != null && !using.contains(id.getAttribute()))
                                || (id.getRelation() == null && re1.occurrences.isAbsent(id.getAttribute()))))

                .collect(ImmutableCollectors.toMap());

        return new RAExpressionAttributes(attributes, occurrences.get());
    }

    public static ImmutableSet<QuotedID> getShared(RAExpressionAttributes re1,
                                                   RAExpressionAttributes re2) {

        return RAExpressionAttributeOccurrences.getShared(re1.occurrences, re2.occurrences);
    }

    /**
     *
     * @param unqualifiedAttributes a {@link ImmutableMap}<{@link QuotedID}, {@link Variable}>
     * @param aliases a {@link RelationID}
     * @return a {@link RAExpressionAttributes}
     */

    public static RAExpressionAttributes create(ImmutableMap<QuotedID, ImmutableTerm> unqualifiedAttributes,
                                                ImmutableSet<RelationID> aliases) {

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes =
                unqualifiedAttributes.entrySet().stream()
                        .flatMap(e -> createQualifiedID(aliases, e.getKey())
                                        .map(i -> Maps.immutableEntry(i, e.getValue())))
                        .collect(ImmutableCollectors.toMap());

        ImmutableMap<QuotedID, ImmutableSet<RelationID>> occurrencesMap =
                unqualifiedAttributes.keySet().stream()
                        .collect(ImmutableCollectors.toMap(Function.identity(), id -> aliases));

        return new RAExpressionAttributes(attributes, occurrencesMap);
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


    private Stream<Map.Entry<QualifiedAttributeID, ImmutableTerm>> selectAttributes(Predicate<QualifiedAttributeID> condition) {
        return attributes.entrySet().stream()
                .filter(e -> condition.test(e.getKey()));
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

        Sets.SetView<RelationID> intersection = Sets.intersection(alias1, alias2);
        if (!intersection.isEmpty())
            throw new IllegalJoinException(re1, re2, intersection.stream()
                    .map(RelationID::getSQLRendering)
                    .collect(Collectors.joining(", ", "Relation alias ", " occurs in both arguments of the JOIN")));
    }


    @Override
    public String toString() {
        return "attributes: " + attributes + " with " + occurrences;
    }
}
