package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;


public class RAExpressionAttributeOccurrences {
    private final ImmutableMap<QuotedID, ImmutableSet<RelationID>> map;

    private RAExpressionAttributeOccurrences(ImmutableMap<QuotedID, ImmutableSet<RelationID>> map) {
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
        return create(idUnionStream(o1, o2), unionOf(o1, o2));
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

        if (!using.stream().allMatch(o1::isUnique) || !using.stream().allMatch(o2::isUnique))
            return Optional.empty();

        Function<QuotedID, ImmutableSet<RelationID>> u = unionOf(o1, o2);
        return Optional.of(create(idUnionStream(o1, o2),
                id -> using.contains(id) ? o1.map.get(id) : u.apply(id)));
    }

    private static Stream<QuotedID> idUnionStream(RAExpressionAttributeOccurrences o1, RAExpressionAttributeOccurrences o2) {
        return Stream.of(o1, o2)
                .map(o -> o.map)
                .map(ImmutableMap::keySet)
                .flatMap(Collection::stream)
                .distinct();
    }

    public static RAExpressionAttributeOccurrences create(Stream<QuotedID> ids,
                                                          Function<QuotedID, ImmutableSet<RelationID>> valueSupplier) {

        return new RAExpressionAttributeOccurrences(
               ids.collect(ImmutableCollectors.toMap(Function.identity(), valueSupplier)));
    }

    /**
     * union of two sets
     * treats null set references as empty sets
     */

    private static Function<QuotedID, ImmutableSet<RelationID>> unionOf(RAExpressionAttributeOccurrences o1, RAExpressionAttributeOccurrences o2) {
        return id -> {
            ImmutableSet<RelationID> s1 = o1.map.get(id);
            ImmutableSet<RelationID> s2 = o2.map.get(id);
            if (s1 == null)
                return s2;

            if (s2 == null)
                return s1;

            return Sets.union(s1, s2).immutableCopy();
        };
    }

    @Override
    public String toString() { return map.toString(); }
}
