package it.unibz.inf.ontop.iq.request.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FunctionalDependenciesImpl implements FunctionalDependencies {

    private final ImmutableSet<FunctionalDependency>  dependencies;

    public FunctionalDependenciesImpl(ImmutableSet<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>> dependencies) {
        this.dependencies = dependencies.stream()
                .map(entry -> new FunctionalDependency(entry.getKey(), entry.getValue()))
                .collect(ImmutableCollectors.toSet());
    }

    private static Stream<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>> inferTransitiveDependencies(ImmutableSet<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>> dependencies) {
        return dependencies.stream()
                .flatMap(entry -> dependencies.stream()
                        .filter(entry2 -> Sets.union(entry2.getValue(), entry2.getKey()).containsAll(entry.getKey()))
                        .map(entry2 -> Maps.immutableEntry(entry2.getKey(), Sets.difference(entry.getValue(), entry2.getKey()).immutableCopy()))
                );
    }

    @Override
    public Stream<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>> stream() {
        return dependencies.stream()
                .map(fd -> Maps.immutableEntry(fd.determinants, fd.dependents));
    }

    @Override
    public FunctionalDependencies rename(InjectiveSubstitution<Variable> renamingSubstitution, SubstitutionFactory substitutionFactory) {
        return new FunctionalDependenciesImpl(dependencies.stream()
                .map(fd -> fd.rename(renamingSubstitution, substitutionFactory))
                .map(fd -> Maps.immutableEntry(fd.determinants, fd.dependents))
                .collect(ImmutableCollectors.toSet())
        ).complete();
    }

    @Override
    public boolean isEmpty() {
        return dependencies.isEmpty();
    }

    @Override
    public FunctionalDependencies concat(FunctionalDependencies other) {
        return new FunctionalDependenciesImpl(
                Stream.concat(stream(), other.stream())
                        .collect(ImmutableCollectors.toSet())
        ).complete();
    }

    @Override
    public boolean contains(ImmutableSet<Variable> determinants, ImmutableSet<Variable> dependents) {
        return dependencies.stream()
                .anyMatch(dp -> determinants.containsAll(dp.determinants) && dp.dependents.containsAll(dependents));
    }

    @Override
    public String toString() {
        return String.format("[%s]", String.join("; ", dependencies.stream()
                .map(FunctionalDependency::toString)
                .collect(ImmutableCollectors.toList())));
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> getDeterminantsOf(Variable variable) {
        return dependencies.stream()
                .filter(d -> d.dependents.contains(variable))
                .map(FunctionalDependency::getDeterminants)
                .collect(ImmutableCollectors.toSet());
    }

    /**
     * Completes the list of functional dependencies by applying the following actions:
     *     - Infer transitive dependencies
     *     - Merge dependencies with related determinants
     *     - Remove duplicate dependencies
     *     - Remove empty dependencies
     */
    protected FunctionalDependencies complete() {
        var dependencyPairs = stream().collect(ImmutableCollectors.toSet());
        var withTransitive = dependencyPairs;
        var lastSize = 0;
        while(lastSize != withTransitive.size()) {
            lastSize = withTransitive.size();
            withTransitive = Streams.concat(dependencyPairs.stream(), inferTransitiveDependencies(withTransitive))
                    .collect(ImmutableCollectors.toSet());
        }
        var collectedDependencies = withTransitive.stream()
                .collect(Collectors.groupingBy(Map.Entry::getKey))
                .entrySet().stream()
                .map(e -> new FunctionalDependency(e.getKey(), ImmutableSet.copyOf(e.getValue().stream()
                        .reduce(
                                Set.of(),
                                (set, entry) -> Sets.union(set, entry.getValue()),
                                (set1, set2) -> Sets.union(set1, set2)
                        )
                )))
                .collect(ImmutableCollectors.toSet());

        var dependenciesToRemove = collectedDependencies.stream()
                .collect(ImmutableCollectors.toMap(
                        fd -> fd,
                        fd -> collectedDependencies.stream()
                                .filter(dependencies2 -> !dependencies2.equals(fd))
                                .filter(dependency2 -> fd.determinants.containsAll(dependency2.determinants))
                                .flatMap(dependency2 -> Sets.intersection(fd.dependents, dependency2.dependents).stream())
                                .collect(ImmutableCollectors.toSet())
                ));
        var packedDependencies = Streams.concat(
                        collectedDependencies.stream()
                                .filter(fd -> dependenciesToRemove.get(fd).isEmpty()),
                        dependenciesToRemove.entrySet().stream()
                                .map(entry -> entry.getKey().removeDependents(entry.getValue()))
                                .filter(fd -> !fd.dependents.isEmpty())
                )
                .filter(fd -> !fd.dependents.isEmpty())
                .collect(ImmutableCollectors.toSet());
        return new FunctionalDependenciesImpl(packedDependencies.stream()
                .map(fd -> Maps.immutableEntry(fd.determinants, fd.dependents))
                .collect(ImmutableCollectors.toSet()));
    }

    public static Collector<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>, ?, FunctionalDependencies> getCollector() {
        return new FunctionalDependenciesCollector();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof FunctionalDependenciesImpl))
            return false;
        var fd = (FunctionalDependenciesImpl) obj;
        return fd.dependencies.equals(dependencies);
    }

    @Override
    public int hashCode() {
        return dependencies.stream()
                .reduce(0, (sum, n) -> sum ^ n.hashCode(), Integer::sum);
    }

    private static class FunctionalDependency {
        private final ImmutableSet<Variable> determinants;
        private final ImmutableSet<Variable> dependents;

        public FunctionalDependency(ImmutableSet<Variable> determinants, ImmutableSet<Variable> dependents) {
            this.determinants = determinants;
            this.dependents = dependents;
        }

        public ImmutableSet<Variable> getDeterminants() {
            return determinants;
        }

        public ImmutableSet<Variable> getDependents() {
            return dependents;
        }

        @Override
        public int hashCode() {
            return determinants.hashCode() ^ dependents.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof FunctionalDependency))
                return false;
            FunctionalDependency other = (FunctionalDependency) obj;
            return dependents.equals(other.dependents) && determinants.equals(other.determinants);
        }

        private FunctionalDependency rename(InjectiveSubstitution<Variable> renamingSubstitution, SubstitutionFactory substitutionFactory) {
            return new FunctionalDependency(
                    determinants.stream()
                            .map(vars -> substitutionFactory.apply(renamingSubstitution, vars))
                            .collect(ImmutableCollectors.toSet()),
                    dependents.stream()
                            .map(vars -> substitutionFactory.apply(renamingSubstitution, vars))
                            .collect(ImmutableCollectors.toSet())
            );
        }

        public FunctionalDependency removeDependents(ImmutableSet<Variable> dependents) {
            return new FunctionalDependency(determinants, Sets.difference(this.dependents, dependents).immutableCopy());
        }

        @Override
        public String toString() {
            return String.format("(%s) --> (%s)",
                            String.join(", ", determinants.stream()
                                    .map(Variable::toString)
                                    .collect(Collectors.toList())),
                            String.join(", ", dependents.stream()
                                    .map(Variable::toString)
                                    .collect(Collectors.toList()))
                    );
        }
    }

    protected static class FunctionalDependenciesCollector implements Collector<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>, ImmutableSet.Builder<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>>, FunctionalDependencies> {


        @Override
        public Supplier<ImmutableSet.Builder<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>>> supplier() {
            return ImmutableSet::builder;
        }

        @Override
        public BiConsumer<ImmutableSet.Builder<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>>, Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>> accumulator() {
            return (builder, fd) -> builder.add(fd);
        }

        @Override
        public BinaryOperator<ImmutableSet.Builder<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>>> combiner() {
            return (builder1, builder2) -> {
                builder1.addAll(builder2.build());
                return builder1;
            };
        }

        @Override
        public Function<ImmutableSet.Builder<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>>, FunctionalDependencies> finisher() {
            return builder -> new FunctionalDependenciesImpl(builder.build()).complete();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of(Characteristics.UNORDERED);
        }
    }
}
