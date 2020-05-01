package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;


public class RAExpressionAttributeOccurrences {
    private final ImmutableMap<QuotedID, ImmutableSet<RelationID>> map;

    RAExpressionAttributeOccurrences(ImmutableMap<QuotedID, ImmutableSet<RelationID>> map) {
        this.map = map;
    }

    /**
     * checks if the attributeOccurrences contains the attribute
     *
     * @param attribute a  {@link QuotedID}
     * @return true if attributeOccurrences contains the attribute otherwise false
     */

    public boolean isAbsent(QuotedID attribute) {
        ImmutableSet<RelationID> occurrences = map.get(attribute);
        return (occurrences == null) || occurrences.isEmpty();
    }

    public boolean isAmbiguous(QuotedID attribute) {
        ImmutableSet<RelationID> occurrences = map.get(attribute);
        return (occurrences != null) && occurrences.size() > 1;
    }

    public boolean isUnique(QuotedID attribute) {
        ImmutableSet<RelationID> occurrences = map.get(attribute);
        return (occurrences != null) && occurrences.size() == 1;
    }

    /**
     * NATURAL JOIN
     *
     * @param o1 a {@link RAExpressionAttributeOccurrences}
     * @param o2 a {@link RAExpressionAttributeOccurrences}
     * @return a {@link ImmutableSet}<{@link QuotedID}>
     */

    public static ImmutableSet<QuotedID> getShared(RAExpressionAttributeOccurrences o1,
                                                   RAExpressionAttributeOccurrences o2) {
        return o1.map.keySet().stream()
                .filter(id -> !o2.isAbsent(id))
                .collect(ImmutableCollectors.toSet());
    }

    /**
     * treats null values as empty sets
     *
     * @param o1 a {@link RAExpressionAttributeOccurrences}
     * @param o2 a {@link RAExpressionAttributeOccurrences}
     * @return the union of occurrences of id in e1 and e2
     */

    public static RAExpressionAttributeOccurrences crossJoin(RAExpressionAttributeOccurrences o1,
                                                             RAExpressionAttributeOccurrences o2) {
        return combine(o1, o2,  id -> unionOf(o1.map.get(id), o2.map.get(id)));
    }

    public static Optional<RAExpressionAttributeOccurrences> joinUsing(RAExpressionAttributeOccurrences o1,
                                                                       RAExpressionAttributeOccurrences o2,
                                                                       ImmutableSet<QuotedID> using) {

        if (using.stream().allMatch(o1::isUnique) && using.stream().allMatch(o2::isUnique))
            return Optional.of(combine(o1, o2, id -> using.contains(id)
                    ? o1.map.get(id)
                    : unionOf(o1.map.get(id), o2.map.get(id))));

        return Optional.empty();
    }

    private static RAExpressionAttributeOccurrences combine(RAExpressionAttributeOccurrences o1,
                                                            RAExpressionAttributeOccurrences o2,
                                                            Function<QuotedID, ImmutableSet<RelationID>> valueSupplier) {

        return new RAExpressionAttributeOccurrences(
                Stream.concat(
                        o1.map.keySet().stream(),
                        o2.map.keySet().stream())
                        .distinct()
                        .collect(ImmutableCollectors.toMap(Function.identity(), valueSupplier)));
    }

    /**
     * treats null values as empty sets
     *
     * @param s1 a {@link ImmutableSet}<{@link RelationID}>
     * @param s2 a {@link ImmutableSet}<{@link RelationID}>
     * @return the union of occurrences of id in e1 and e2
     */

    private static ImmutableSet<RelationID> unionOf(ImmutableSet<RelationID> s1, ImmutableSet<RelationID> s2) {
        if (s1 == null)
            return s2;

        if (s2 == null)
            return s1;

        return Sets.union(s1, s2).immutableCopy();
    }


    @Override
    public String toString() {
        return map.toString();
    }
}
