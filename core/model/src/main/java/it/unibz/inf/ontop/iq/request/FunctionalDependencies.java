package it.unibz.inf.ontop.iq.request;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.request.impl.FunctionalDependenciesImpl;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Stream;

public interface FunctionalDependencies {

    boolean isEmpty();

    Stream<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>> stream();

    FunctionalDependencies rename(InjectiveSubstitution<Variable> renamingSubstitution, SubstitutionFactory substitutionFactory);
    FunctionalDependencies concat(FunctionalDependencies other);

    boolean contains(ImmutableSet<Variable> determinants, ImmutableSet<Variable> dependents);

    static FunctionalDependencies of(ImmutableSet<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>> dependencies) {
        return new FunctionalDependenciesImpl(dependencies);
    }

    static FunctionalDependencies empty() {
        return new FunctionalDependenciesImpl(ImmutableSet.of());
    }

    static Collector<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>, ?, FunctionalDependencies> toFunctionalDependencies() {
        return FunctionalDependenciesImpl.getCollector();
    }

}
