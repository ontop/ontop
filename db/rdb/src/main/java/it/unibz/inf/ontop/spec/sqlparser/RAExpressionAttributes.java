package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RAExpressionAttributes  {

    private final ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes;
    private final ImmutableMap<QuotedID, ImmutableSet<RelationID>> occurrences;

    /**
     * constructs a relation expression
     *
     * @param attributes  a map from {@link QualifiedAttributeID},to {@link ImmutableTerm}
     * @param occurrences a map from {@link QuotedID} to a set of {@link RelationID}
     */
    public RAExpressionAttributes(ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes,
                                  ImmutableMap<QuotedID, ImmutableSet<RelationID>> occurrences) {
        this.attributes = attributes;
        this.occurrences = occurrences;
    }

    public ImmutableMap<QualifiedAttributeID, ImmutableTerm> asMap() {
        return attributes;
    }

    public ImmutableTerm get(QuotedID attributeId) {
        return get(new QualifiedAttributeID(null, attributeId));
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

    /**
     * checks if occurrence of the non-qualified attribute is unique
     *     (exactly one relation contains the attribute)
     *
     * @param attribute a  {@link QuotedID}
     * @return true if the attribute is unique; otherwise false
     */

    public boolean isUnique(QuotedID attribute) {
        ImmutableSet<RelationID> occ = occurrences.get(attribute);
        return (occ != null) && occ.size() == 1;
    }



    public ImmutableMap<QuotedID, ImmutableTerm> getUnqualifiedAttributes() {
        return selectAttributes(id -> id.getRelation() == null)
                .collect(ImmutableCollectors.toMap(e -> e.getKey().getAttribute(), Map.Entry::getValue));
    }


    Stream<Map.Entry<QualifiedAttributeID, ImmutableTerm>> selectAttributes(Predicate<QualifiedAttributeID> condition) {
        return attributes.entrySet().stream()
                .filter(e -> condition.test(e.getKey()));
    }

    public ImmutableSet<QuotedID> getAllAttributes() {
        return occurrences.keySet();
    }

    public ImmutableSet<RelationID> getOccurrences(QuotedID id) {
        return occurrences.getOrDefault(id, ImmutableSet.of());
    }


    @Override
    public String toString() {
        return "attributes: " + attributes + " with " + occurrences;
    }
}
