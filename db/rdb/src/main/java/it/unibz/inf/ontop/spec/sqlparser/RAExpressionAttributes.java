package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RAExpressionAttributes  {

    private final ImmutableMap<QuotedID, ImmutableMap<ImmutableSet<RelationID>, ImmutableTerm>> map;
    private final ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes;
    private final ImmutableMap<QuotedID, ImmutableSet<RelationID>> occurrences;

    /**
     * constructs a relation expression
     *
     * @param attributes  a map from {@link QualifiedAttributeID},to {@link ImmutableTerm}
     */
    private RAExpressionAttributes(ImmutableMap<QuotedID, ImmutableMap<ImmutableSet<RelationID>, ImmutableTerm>> map,
                                  ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes,
                                  ImmutableSet<QuotedID> attributeIds,
                                  Function<QuotedID, ImmutableSet<RelationID>> relationIdsFunction) {
        this.map = map;
        this.attributes = attributes;
        this.occurrences = attributeIds.stream()
                .collect(ImmutableCollectors.toMap(id -> id, relationIdsFunction));
    }

    ImmutableMap<QuotedID, ImmutableMap<ImmutableSet<RelationID>, ImmutableTerm>> getMap() {
        return map;
    }

    public static class DuplicateAttrbuteEntriesException extends Exception {
        private final ImmutableSet<QuotedID> duplicates;

        public DuplicateAttrbuteEntriesException(ImmutableSet<QuotedID> duplicates) {
            this.duplicates = duplicates;
        }

        public ImmutableSet<QuotedID> getDuplicates() { return duplicates; }
    }

    public static RAExpressionAttributes of(ImmutableMultimap<QuotedID, ? extends ImmutableTerm> multimap) throws DuplicateAttrbuteEntriesException {

        ImmutableSet<QuotedID> duplicateAttributeIds = multimap.asMap().entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());

        if (!duplicateAttributeIds.isEmpty())
            throw new DuplicateAttrbuteEntriesException(duplicateAttributeIds);

        return of(multimap.entries().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> ImmutableMap.of(ImmutableSet.of(), e.getValue()))));
    }

    public static RAExpressionAttributes of(ImmutableMap<QuotedID, ImmutableMap<ImmutableSet<RelationID>, ImmutableTerm>> map) {
        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributeMapWithAliases = map.entrySet().stream()
                .flatMap(e -> Streams.concat(
                        Stream.of(e.getValue())
                                .filter(m -> m.size() == 1)
                                .map(m -> Maps.immutableEntry(new QualifiedAttributeID(null, e.getKey()), m.entrySet().iterator().next().getValue())),
                        e.getValue().entrySet().stream()
                                .flatMap(e2 -> e2.getKey().stream()
                                        .map(id -> Maps.immutableEntry(new QualifiedAttributeID(id, e.getKey()), e2.getValue())))))
                .collect(ImmutableCollectors.toMap());

        Function<QuotedID, ImmutableSet<RelationID>> f = id -> map.get(id).entrySet().stream()
                .flatMap(e -> e.getKey().stream()
                        .findFirst().stream())
                .collect(ImmutableCollectors.toSet());

        return new RAExpressionAttributes(map, attributeMapWithAliases, map.keySet(), f);
    }


    public ImmutableSet<QualifiedAttributeID> getAttributes() {
        return attributes.keySet();
    }

    public ImmutableTerm get(QualifiedAttributeID id) {
        return attributes.get(id);
    }



    /**
     * checks if there is an occurrence of the non-qualified attribute
     *
     * @param attribute a  {@link QuotedID}
     * @return true if contains the attribute; otherwise false
     */

    public boolean isAbsent(QuotedID attribute) {
        ImmutableSet<RelationID> occ = occurrences.get(attribute);
        return (occ == null) || occ.isEmpty();
    }

    /**
     * checks if occurrence of the non-qualified attribute are ambiguous
     *     (at least two relations contain the attribute)
     *
     * @param attribute a  {@link QuotedID}
     * @return true if the attribute is ambiguous; otherwise false
     */

    public boolean isAmbiguous(QuotedID attribute) {
        ImmutableSet<RelationID> occ = occurrences.get(attribute);
        return (occ != null) && occ.size() > 1;
    }


    public ImmutableMap<QuotedID, ImmutableTerm> getUnqualifiedAttributesMap() {
        return getAttributesMapSelection(id -> id.getRelation() == null)
                .collect(ImmutableCollectors.toMap(e -> e.getKey().getAttribute(), Map.Entry::getValue));
    }


    public Stream<Map.Entry<QualifiedAttributeID, ImmutableTerm>> getAttributesMapSelection(Predicate<QualifiedAttributeID> condition) {
        return attributes.entrySet().stream()
                .filter(e -> condition.test(e.getKey()));
    }

    public ImmutableSet<QuotedID> getAllUnqualifiedAttributes() {
        return occurrences.keySet();
    }


    @Override
    public String toString() {
        return "attributes: " + attributes + " with " + occurrences;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RAExpressionAttributes) {
            RAExpressionAttributes other = (RAExpressionAttributes) obj;
            return other.attributes.equals(this.attributes) && other.occurrences.equals(this.occurrences);
        }
        return false;
    }
}
