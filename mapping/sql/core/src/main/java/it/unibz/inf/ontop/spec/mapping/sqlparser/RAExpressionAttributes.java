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
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by roman on 24/01/2017.
 */
public class RAExpressionAttributes implements RAEntity<RAExpressionAttributes> {

    private final ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes;
    private final RAExpressionAttributeOccurrences occurrences;

    /**
     * constructs a relation expression
     *
     * @param attributes  an {@link ImmutableMap}<{@link QualifiedAttributeID}, {@link ImmutableTerm}>
     * @param occurrences an {@link RAExpressionAttributeOccurrences}>>
     */
    private RAExpressionAttributes(ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes,
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

    /**
     * CROSS JOIN (also denoted by , in SQL)
     *
     * @param re2 an {@link RAExpressionAttributes}
     * @return an {@link RAExpressionAttributes}
     * @throws IllegalJoinException if the same relation alias occurs in both arguments
     */
    @Override
    public RAExpressionAttributes crossJoin(RAExpressionAttributes re2) throws IllegalJoinException {

        checkRelationAliasesConsistency(re2);

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes = Stream.concat(
                this.selectAttributes(id ->
                        (id.getRelation() != null) || re2.occurrences.isAbsent(id.getAttribute())),

                re2.selectAttributes(id ->
                        (id.getRelation() != null) || this.occurrences.isAbsent(id.getAttribute())))

                .collect(ImmutableCollectors.toMap());

        return new RAExpressionAttributes(attributes,
                RAExpressionAttributeOccurrences.crossJoin(this.occurrences, re2.occurrences));
    }


    /**
     * JOIN USING
     *
     * @param re2 an {@link RAExpressionAttributes}
     * @param using an {@link ImmutableSet}<{@link QuotedID}>
     * @return an {@link RAExpressionAttributes}
     * @throws IllegalJoinException if the same relatio alias occurs in both arguments
     *          or one of the `using' attributes is ambiguous or absent
     */

    @Override
    public RAExpressionAttributes joinUsing(RAExpressionAttributes re2,
                                                   ImmutableSet<QuotedID> using) throws IllegalJoinException {

        checkRelationAliasesConsistency(re2);

        Optional<RAExpressionAttributeOccurrences> occurrences = RAExpressionAttributeOccurrences.joinUsing(
                this.occurrences, re2.occurrences, using);
        if (!occurrences.isPresent()) {

            ImmutableList<QuotedID> notFound = using.stream()
                    .filter(id -> this.occurrences.isAbsent(id) || re2.occurrences.isAbsent(id))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<QuotedID> ambiguous = using.stream()
                    .filter(id -> this.occurrences.isAmbiguous(id) || re2.occurrences.isAmbiguous(id))
                    .collect(ImmutableCollectors.toList());

            throw new IllegalJoinException(this, re2,
                    (!notFound.isEmpty() ? "Attribute(s) " + notFound + " cannot be found" : "") +
                            (!notFound.isEmpty() && !ambiguous.isEmpty() ? ", " : "") +
                            (!ambiguous.isEmpty() ? "Attribute(s) " + ambiguous + " are ambiguous" : ""));
        }

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes = Stream.concat(
                this.selectAttributes(id ->
                        (id.getRelation() != null && !using.contains(id.getAttribute()))
                                || (id.getRelation() == null && re2.occurrences.isAbsent(id.getAttribute()))
                                || (id.getRelation() == null && using.contains(id.getAttribute()))),

                re2.selectAttributes(id ->
                        (id.getRelation() != null && !using.contains(id.getAttribute()))
                                || (id.getRelation() == null && this.occurrences.isAbsent(id.getAttribute()))))

                .collect(ImmutableCollectors.toMap());

        return new RAExpressionAttributes(attributes, occurrences.get());
    }

    @Override
    public RAExpressionAttributes joinOn(RAExpressionAttributes right, Function<RAExpressionAttributes, ImmutableList<ImmutableExpression>> getAtomOnExpression) throws IllegalJoinException {
        return crossJoin(right);
    }

    @Override
    public RAExpressionAttributes naturalJoin(RAExpressionAttributes right) throws IllegalJoinException {
        return joinUsing(right, getSharedAttributeNames(right));
    }

    public ImmutableSet<QuotedID> getSharedAttributeNames(RAExpressionAttributes right) {
        return RAExpressionAttributeOccurrences.getShared(this.occurrences, right.occurrences);
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

        if (aliases.size() > 1)
            System.out.println("ALIASES: " + aliases);

        return new RAExpressionAttributes(attributes,
                RAExpressionAttributeOccurrences.create(
                        unqualifiedAttributes.keySet().stream(),
                        id -> aliases));
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

    @Override
    public RAExpressionAttributes withAlias(RelationID aliasId) {
        return create(getUnqualifiedAttributes(), ImmutableSet.of(aliasId));
    }



    private Stream<Map.Entry<QualifiedAttributeID, ImmutableTerm>> selectAttributes(Predicate<QualifiedAttributeID> condition) {
        return attributes.entrySet().stream()
                .filter(e -> condition.test(e.getKey()));
    }

    private ImmutableSet<RelationID> getRelationAliases() {
        return attributes.keySet().stream()
                .filter(id -> id.getRelation() != null)
                .map(QualifiedAttributeID::getRelation).collect(ImmutableCollectors.toSet());
    }


    /**
     * throw IllegalJoinException if a relation alias occurs in both arguments of the join
     *
     * @param re2 a {@link RAExpressionAttributes}
     * @throws IllegalJoinException if the same alias occurs in both arguments
     */

    private void checkRelationAliasesConsistency(RAExpressionAttributes re2) throws IllegalJoinException {

        Sets.SetView<RelationID> intersection = Sets.intersection(
                this.getRelationAliases(), re2.getRelationAliases());
        if (!intersection.isEmpty())
            throw new IllegalJoinException(this, re2, intersection.stream()
                    .map(RelationID::getSQLRendering)
                    .collect(Collectors.joining(", ", "Relation alias ", " occurs in both arguments of the JOIN")));
    }


    @Override
    public String toString() {
        return "attributes: " + attributes + " with " + occurrences;
    }
}
