package it.unibz.inf.ontop.iq.request.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class FunctionalDependenciesImpl implements FunctionalDependencies {

    private final ImmutableSet<FunctionalDependency>  dependencies;


    public FunctionalDependenciesImpl(ImmutableSet<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>> dependencies) {
        this.dependencies = dependencies.stream()
                .map(e -> new FunctionalDependency(e.getKey(), e.getValue()))
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public Stream<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>> stream() {
        return dependencies.stream()
                .map(fd -> Map.entry(fd.determinants, fd.dependents));
    }

    @Override
    public FunctionalDependencies rename(InjectiveSubstitution<Variable> renamingSubstitution, SubstitutionFactory substitutionFactory) {
        return new FunctionalDependenciesImpl(dependencies.stream()
                .map(fd -> fd.rename(renamingSubstitution, substitutionFactory))
                .map(fd -> Map.entry(fd.determinants, fd.dependents))
                .collect(ImmutableCollectors.toSet())
        );
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
        );
    }

    @Override
    public boolean contains(ImmutableSet<Variable> determinants, ImmutableSet<Variable> dependents) {
        return dependencies.stream()
                .anyMatch(dp -> determinants.containsAll(dp.determinants) && dp.dependents.containsAll(dependents));
    }

    public static Collector<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>, ?, FunctionalDependencies> getCollector() {
        return new FunctionalDependenciesCollector();
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
            return builder -> new FunctionalDependenciesImpl(builder.build());
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of(Characteristics.UNORDERED);
        }
    }
}
