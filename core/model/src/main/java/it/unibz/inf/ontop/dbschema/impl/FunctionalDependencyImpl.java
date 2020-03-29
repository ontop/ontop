package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.FunctionalDependency;

import java.util.HashSet;
import java.util.Set;

public class FunctionalDependencyImpl implements FunctionalDependency {

    private final ImmutableSet<Attribute> determinants;
    private final ImmutableSet<Attribute> dependents;

    private FunctionalDependencyImpl(ImmutableSet<Attribute> determinants,
                                     ImmutableSet<Attribute> dependents) {
        this.determinants = determinants;
        this.dependents = dependents;
    }

    @Override
    public ImmutableSet<Attribute> getDeterminants() {
        return determinants;
    }

    @Override
    public ImmutableSet<Attribute> getDependents() {
        return dependents;
    }


    public static class BuilderImpl implements Builder {

        private final Set<Attribute> determinants = new HashSet<>();
        private final Set<Attribute> dependents = new HashSet<>();

        @Override
        public Builder addDeterminant(Attribute determinant) {
            determinants.add(determinant);
            return this;
        }

        @Override
        public Builder addDependent(Attribute dependent) {
            dependents.add(dependent);
            return this;
        }

        @Override
        public FunctionalDependency build() {
            return new FunctionalDependencyImpl(ImmutableSet.copyOf(determinants),
                    ImmutableSet.copyOf(dependents));
        }
    }
}
