package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

/**
 * TODO: explain
 */
public interface ExtensionalDataNode extends LeafIQTree {

    RelationDefinition getRelationDefinition();

    ImmutableMap<Integer, ? extends VariableOrGroundTerm> getArgumentMap();

    @Override
    default <T> T acceptVisitor(IQVisitor<T> visitor) {
        return visitor.transformExtensionalData(this);
    }

    static <T> ImmutableMap<Integer, ? extends T> union(ImmutableMap<Integer, ? extends T> argumentMap1, ImmutableMap<Integer, ? extends T> argumentMap2) {
        return Sets.union(argumentMap1.keySet(), argumentMap2.keySet()).stream()
                // For better readability
                .sorted()
                .collect(ImmutableCollectors.toMap(
                        i -> i,
                        i -> Optional.<T>ofNullable(argumentMap1.get(i)).orElseGet(() -> argumentMap2.get(i))));
    }

    static ImmutableMap<Integer, ? extends VariableOrGroundTerm> restrictTo(ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap, java.util.function.Predicate<Integer> indexes) {
        return argumentMap.entrySet().stream()
                .filter(e -> indexes.test(e.getKey()))
                .collect(ImmutableCollectors.toMap());
    }

}
