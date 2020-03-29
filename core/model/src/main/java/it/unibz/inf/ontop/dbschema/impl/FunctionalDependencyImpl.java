package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.FunctionalDependency;
import it.unibz.inf.ontop.dbschema.RelationDefinition;

import java.util.HashSet;
import java.util.Set;

public class FunctionalDependencyImpl implements FunctionalDependency {

    private final ImmutableSet<Attribute> determinants, dependents;

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

        private final ImmutableSet.Builder<Attribute>
                determinants = ImmutableSet.builder(),
                dependents = ImmutableSet.builder();

        private final RelationDefinition relation;

        public BuilderImpl(RelationDefinition relation) {
            this.relation = relation;
        }

        @Override
        public Builder addDeterminant(Attribute determinant) {
            if (determinant.getRelation() != relation)
                throw new IllegalArgumentException("Relation does not match");
            determinants.add(determinant);
            return this;
        }

        @Override
        public Builder addDependent(Attribute dependent) {
            if (dependent.getRelation() != relation)
                throw new IllegalArgumentException("Relation does not match");
            dependents.add(dependent);
            return this;
        }

        @Override
        public FunctionalDependency build() {
            return new FunctionalDependencyImpl(determinants.build(), dependents.build());
        }
    }
}
