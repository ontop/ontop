package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.spec.sqlparser.exception.IllegalJoinException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RAExpressionAttributes  {

    private final ImmutableMap<QuotedID, Occurrences> map;
    private final ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes;

    /**
     * constructs a relation expression
     *
     */
    private RAExpressionAttributes(ImmutableMap<QuotedID, Occurrences> map) {
        this.map = map;
        this.attributes = map.entrySet().stream()
                .flatMap(e -> Streams.concat(
                        Stream.of(e.getValue())
                                .filter(Occurrences::isUnambiguous)
                                .map(m -> Maps.immutableEntry(new QualifiedAttributeID(null, e.getKey()), m.getTerm())),
                        e.getValue().map.entrySet().stream()
                                .flatMap(e2 -> e2.getKey().stream()
                                        .map(id -> Maps.immutableEntry(new QualifiedAttributeID(id, e.getKey()), e2.getValue())))))
                .collect(ImmutableCollectors.toMap());
    }

    public static class DuplicateAttrbuteEntriesException extends Exception {
        private final ImmutableSet<QuotedID> duplicates;

        public DuplicateAttrbuteEntriesException(ImmutableSet<QuotedID> duplicates) {
            this.duplicates = duplicates;
        }

        public ImmutableSet<QuotedID> getDuplicates() { return duplicates; }
    }

    public static RAExpressionAttributes of(Stream<? extends Map.Entry<QuotedID, ? extends ImmutableTerm>> stream) throws DuplicateAttrbuteEntriesException {

        var multimap = stream.collect(ImmutableCollectors.toMultimap(Map.Entry::getKey, Map.Entry::getValue));

        ImmutableSet<QuotedID> duplicateAttributeIds = multimap.asMap().entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());

        if (!duplicateAttributeIds.isEmpty())
            throw new DuplicateAttrbuteEntriesException(duplicateAttributeIds);

        return ofUnqualifiedAttributesMap(multimap.entries().stream().collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    static RAExpressionAttributes of(ImmutableMap<QuotedID, Occurrences> map) {
        return new RAExpressionAttributes(map);
    }

    /**
     * Internal for the package.
     *
     * @param map
     * @param allRelationIds
     * @return
     */
    static RAExpressionAttributes ofUnqualifiedAttributesMap(ImmutableMap<QuotedID, ImmutableTerm> map, ImmutableSet<RelationID> allRelationIds) {
        return of(map.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> Occurrences.of(allRelationIds, e.getValue()))));
    }

    public static RAExpressionAttributes ofUnqualifiedAttributesMap(ImmutableMap<QuotedID, ImmutableTerm> map) {
        return ofUnqualifiedAttributesMap(map, ImmutableSet.of());
    }


    static class Occurrences {
        private final ImmutableMap<ImmutableSet<RelationID>, ImmutableTerm> map;

        private Occurrences(ImmutableMap<ImmutableSet<RelationID>, ImmutableTerm> map) {
            this.map = map;
        }

        static Occurrences of(ImmutableSet<RelationID> relationIds, ImmutableTerm term) {
            return new Occurrences(ImmutableMap.of(relationIds, term));
        }

        static Occurrences of(ImmutableSet<RelationID> relationIds1, ImmutableTerm term1, ImmutableSet<RelationID> relationIds2, ImmutableTerm term2) {
            return new Occurrences(ImmutableMap.of(relationIds1, term1, relationIds2, term2));
        }

        static Occurrences merge(Occurrences occ1, Occurrences occ2) {
            return new Occurrences(Streams.concat(occ1.map.entrySet().stream(), occ2.map.entrySet().stream())
                    .collect(ImmutableCollectors.toMap()));
        }

        boolean isUnambiguous() {
            return map.size() == 1;
        }

        boolean isAbsent() {
            return map.isEmpty();
        }

        boolean isAmbiguous() {
            return map.size() > 1;
        }

        ImmutableTerm getTerm() {
            if (!isUnambiguous())
                throw new MinorOntopInternalBugException("Occurrences map is ambiguous");

            return map.entrySet().iterator().next().getValue();
        }

        ImmutableSet<RelationID> getRelationIDs() {
            return map.keySet().stream()
                    .flatMap(Collection::stream)
                    .collect(ImmutableCollectors.toSet());
        }

        @Override
        public String toString() {
            return map.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Occurrences) {
                Occurrences other = (Occurrences) o;
                return map.equals(other.map);
            }
            return false;
        }
    }

    Occurrences getOccurrences(QuotedID id) {
        return Optional.ofNullable(map.get(id)).orElseGet(() -> new Occurrences(ImmutableMap.of()));
    }


    public ImmutableSet<QualifiedAttributeID> getAttributes() {
        return attributes.keySet();
    }

    public ImmutableTerm get(QualifiedAttributeID id) {
        return attributes.get(id);
    }


    public ImmutableMap<QuotedID, ImmutableTerm> getUnqualifiedAttributesMap() {
        return getAttributesMapSelection(a -> !a.isQualified());
    }

    public ImmutableMap<QuotedID, ImmutableTerm> getRelationAttributesMap(RelationID id) {
        return getAttributesMapSelection(a -> id.equals(a.getRelation()));
    }

    private ImmutableMap<QuotedID, ImmutableTerm> getAttributesMapSelection(Predicate<QualifiedAttributeID> condition) {
        return attributes.entrySet().stream()
                .filter(e -> condition.test(e.getKey()))
                .collect(ImmutableCollectors.toMap(e -> e.getKey().getAttribute(), Map.Entry::getValue));
    }

    /**
     *
     * @param re1 a {@link RAExpressionAttributes}
     * @param re2 a {@link RAExpressionAttributes}
     * @throws IllegalJoinException  if a relation alias occurs in both arguments of the join for
     *      * the same attribute ID
     */

    static RAExpressionAttributes join(RAExpressionAttributes re1, RAExpressionAttributes re2, Function<QuotedID, Occurrences> joinFunction) throws IllegalJoinException {

        ImmutableSet<RelationID> intersection = getSharedAttributeNames(re1, re2).stream()
                .map(id -> Sets.intersection(re1.getOccurrences(id).getRelationIDs(), re2.getOccurrences(id).getRelationIDs()))
                .flatMap(Collection::stream)
                .collect(ImmutableCollectors.toSet());
        if (!intersection.isEmpty())
            throw new IllegalJoinException(re1, re2, intersection);

        return RAExpressionAttributes.of(
                Sets.union(re1.map.keySet(), re2.map.keySet()).stream()
                        .collect(ImmutableCollectors.toMap(id -> id, joinFunction)));
    }

    static Sets.SetView<QuotedID> getSharedAttributeNames(RAExpressionAttributes re1, RAExpressionAttributes re2) {
        return Sets.intersection(re1.map.keySet(), re2.map.keySet());
    }

    @Override
    public String toString() {
        return "attributes: " + map;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RAExpressionAttributes) {
            RAExpressionAttributes other = (RAExpressionAttributes) obj;
            return other.map.equals(this.map);
        }
        return false;
    }
}
