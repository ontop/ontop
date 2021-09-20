package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;



public class RAExpressionAttributeOccurrences {
    private final ImmutableMap<QuotedID, ImmutableSet<RelationID>> map;

    RAExpressionAttributeOccurrences(ImmutableMap<QuotedID, ImmutableSet<RelationID>> map) {
        this.map = map;
    }

    /**
     * checks if there is an occurrence of the non-qualified attribute
     *
     * @param attribute a  {@link QuotedID}
     * @return true if contains the attribute; otherwise false
     */

    public boolean isAbsent(QuotedID attribute) {
        ImmutableSet<RelationID> occurrences = map.get(attribute);
        return (occurrences == null) || occurrences.isEmpty();
    }

    /**
     * checks if occurrence of the non-qualified attribute are ambiguous
     *     (at least two relations contain the attribute)
     *
     * @param attribute a  {@link QuotedID}
     * @return true if the attribute is ambiguous; otherwise false
     */

    public boolean isAmbiguous(QuotedID attribute) {
        ImmutableSet<RelationID> occurrences = map.get(attribute);
        return (occurrences != null) && occurrences.size() > 1;
    }

    /**
     * checks if occurrence of the non-qualified attribute is unique
     *     (exactly one relation contains the attribute)
     *
     * @param attribute a  {@link QuotedID}
     * @return true if the attribute is unique; otherwise false
     */

    public boolean isUnique(QuotedID attribute) {
        ImmutableSet<RelationID> occurrences = map.get(attribute);
        return (occurrences != null) && occurrences.size() == 1;
    }

    public ImmutableSet<QuotedID> getAttributes() {
        return map.keySet();
    }

    public ImmutableSet<RelationID> get(QuotedID id) {
        return map.getOrDefault(id, ImmutableSet.of());
    }


    @Override
    public String toString() { return map.toString(); }
}
