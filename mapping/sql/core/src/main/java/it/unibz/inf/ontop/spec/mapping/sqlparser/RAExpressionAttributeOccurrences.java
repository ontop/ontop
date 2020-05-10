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

    /**
     * NATURAL JOIN
     *
     * @param o1 an {@link RAExpressionAttributeOccurrences}
     * @param o2 an {@link RAExpressionAttributeOccurrences}
     * @return an {@link ImmutableSet}<{@link QuotedID}>
     */

    public static ImmutableSet<QuotedID> getShared(RAExpressionAttributeOccurrences o1,
                                                   RAExpressionAttributeOccurrences o2) {

        return Sets.intersection(o1.map.keySet(), o2.map.keySet()).immutableCopy();
    }

    /**
     * non-qualified attribute occurrences for CROSS JOIN
     *
     * @param o1 an {@link RAExpressionAttributeOccurrences}
     * @param o2 an {@link RAExpressionAttributeOccurrences}
     * @return an {@link RAExpressionAttributeOccurrences}
     *      R.X, R.Y and S.X, S.Y -> R.X, RS.Y, S.Y
     */

    public static RAExpressionAttributeOccurrences crossJoin(RAExpressionAttributeOccurrences o1,
                                                             RAExpressionAttributeOccurrences o2) {
        return combine(o1, o2,  id -> unionOf(o1.map.get(id), o2.map.get(id)));
    }

    /**
     * non-qualified attribute occurrences for JOIN USING
     *
     * @param o1 an {@link RAExpressionAttributeOccurrences}
     * @param o2 an {@link RAExpressionAttributeOccurrences}
     * @return an {@link RAExpressionAttributeOccurrences}
     *      R.X, R.Y, R.U and S.Y, S.Z using U ->  empty
     *      R.X, R.Y and S.Y, S.Z, S.U using U ->  empty
     *      R.X, R.Y, R.U and S.Y, S.Z, S.U using U -> R.X, RS.Y, S.Y, R.U
     *            (the choice or R/S is arbitrary, but we keep it unambiguous)
     */

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
     * union of two sets
     * treats null set references as empty sets
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
