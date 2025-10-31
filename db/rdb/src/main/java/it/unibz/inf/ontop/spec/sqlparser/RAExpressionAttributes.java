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

    private final ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes;
    private final ImmutableMap<QuotedID, ImmutableSet<RelationID>> occurrences;

    /**
     * constructs a relation expression
     *
     * @param attributes  a map from {@link QualifiedAttributeID},to {@link ImmutableTerm}
     */
    public RAExpressionAttributes(ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes,
                                  ImmutableSet<QuotedID> attributeIds,
                                  Function<QuotedID, ImmutableSet<RelationID>> relationIdsFunction) {
        this.attributes = attributes;
        this.occurrences = attributeIds.stream()
                .collect(ImmutableCollectors.toMap(id -> id, relationIdsFunction));
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

        return new RAExpressionAttributes(
                multimap.entries().stream()
                        .collect(ImmutableCollectors.toMap(e -> new QualifiedAttributeID(null, e.getKey()), Map.Entry::getValue)),
                multimap.keySet(),
                id -> ImmutableSet.of());
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

    public ImmutableSet<RelationID> getOccurrences(QuotedID id) {
        return occurrences.getOrDefault(id, ImmutableSet.of());
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
