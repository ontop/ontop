package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Represents Boolean expressions as disjunctions of conjunctions,
 * where each conjunction is non-empty,
 * but the list of disjunctions can be empty, which means that the expression is TRUE.
 * (This unusual assumption is convenient for most manipulations, including
 * termFactory.getDisjunction(termFactory.getConjunction).)
 * In some cases, a special check .isTrue() needs to be performed though.
 */
public class DisjunctionOfConjunctions {
    private final ImmutableSet<ImmutableSet<ImmutableExpression>> conjunctions;

    private DisjunctionOfConjunctions(ImmutableSet<ImmutableSet<ImmutableExpression>> conjunctions) {
        this.conjunctions = conjunctions;
    }

    public static DisjunctionOfConjunctions getOR(DisjunctionOfConjunctions o1, DisjunctionOfConjunctions o2) {
        if (o1.isTrue())
            return o1;

        if (o2.isTrue())
            return o2;

        Builder builder = new Builder(o1);
        o2.conjunctions.forEach(builder::add);
        return builder.build();
    }

    public static DisjunctionOfConjunctions getAND(DisjunctionOfConjunctions o1, DisjunctionOfConjunctions o2) {
        if (o1.isTrue())
            return o2;

        if (o2.isTrue())
            return o1;

        return new DisjunctionOfConjunctions(
                o1.conjunctions.stream()
                        .flatMap(s1 -> o2.conjunctions.stream().map(s2 -> Sets.union(s1, s2).immutableCopy()))
                        .collect(ImmutableCollectors.toSet()));
    }

    public static DisjunctionOfConjunctions getTrue() { return new DisjunctionOfConjunctions(ImmutableSet.of()); }

    public boolean isTrue() { return conjunctions.isEmpty(); }

    public int getNumberOfConjunctions() { return conjunctions.size(); }

    public static DisjunctionOfConjunctions of(ImmutableExpression e) {
        return e.flattenOR()
                .map(c -> c.flattenAND().collect(ImmutableCollectors.toSet()))
                .collect(toDisjunctionOfConjunctions());
    }

    public ImmutableSet<Variable> getVariables() {
        return conjunctions.stream()
                .flatMap(d -> d.stream()
                        .flatMap(ImmutableTerm::getVariableStream))
                .collect(ImmutableCollectors.toSet());
    }

    public Stream<ImmutableSet<ImmutableExpression>> stream() { return conjunctions.stream(); }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DisjunctionOfConjunctions) {
            DisjunctionOfConjunctions other = (DisjunctionOfConjunctions) o;
            return conjunctions.equals(other.conjunctions);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return conjunctions.hashCode();
    }

    @Override
    public String toString() {
        return conjunctions.toString();
    }

    private static class Builder {
        final Set<ImmutableSet<ImmutableExpression>> disjunctions;

        Builder() {
            disjunctions = new HashSet<>();
        }
        Builder(DisjunctionOfConjunctions disjunctionOfConjunctions) {
            disjunctions = new HashSet<>(disjunctionOfConjunctions.conjunctions);
        }

        void add(ImmutableSet<ImmutableExpression> conjunction) {
            if (disjunctions.stream().noneMatch(conjunction::containsAll)) {
                disjunctions.removeIf(c -> c.containsAll(conjunction));
                disjunctions.add(conjunction);
            }
        }

        Builder addAll(Builder other) {
            Builder builder = new Builder();
            builder.disjunctions.addAll(this.disjunctions);
            other.disjunctions.forEach(builder::add);
            return builder;
        }

        DisjunctionOfConjunctions build() {
            return new DisjunctionOfConjunctions(ImmutableSet.copyOf(disjunctions));
        }
    }

    public static Collector<ImmutableSet<ImmutableExpression>, Builder, DisjunctionOfConjunctions> toDisjunctionOfConjunctions() {
        return Collector.of(Builder::new, Builder::add, Builder::addAll, Builder::build, Collector.Characteristics.UNORDERED);
    }
}
