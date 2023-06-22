package it.unibz.inf.ontop.iq.request;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import it.unibz.inf.ontop.iq.request.impl.FunctionalDependenciesImpl;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface FunctionalDependencies {

    boolean isEmpty();

    Stream<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>> stream();

    FunctionalDependencies rename(InjectiveSubstitution<Variable> renamingSubstitution, SubstitutionFactory substitutionFactory);
    FunctionalDependencies concat(FunctionalDependencies other);

    boolean contains(ImmutableSet<Variable> determinants, ImmutableSet<Variable> dependents);

    static FunctionalDependencies of(ImmutableSet<Variable>... dependencies) {
        if(dependencies.length % 2 != 0)
            throw new IllegalArgumentException("FunctionalDependency must be built of 2n ImmutableSets.");
        var determinants = IntStream.range(0, dependencies.length)
                .filter(i -> i % 2 == 0)
                .mapToObj(i -> dependencies[i]);
        var dependents = IntStream.range(0, dependencies.length)
                .filter(i -> i % 2 == 1)
                .mapToObj(i -> dependencies[i]);
        return new FunctionalDependenciesImpl(Streams.zip(determinants, dependents, (a, b) -> Map.entry(a, b))
                .collect(ImmutableCollectors.toSet())
            );
    }

    static FunctionalDependencies empty() {
        return new FunctionalDependenciesImpl(ImmutableSet.of());
    }

    static Collector<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>, ?, FunctionalDependencies> toFunctionalDependencies() {
        return FunctionalDependenciesImpl.getCollector();
    }

}
