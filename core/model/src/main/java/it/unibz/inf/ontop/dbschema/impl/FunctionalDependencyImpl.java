package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;

import java.util.Objects;

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

        private final DatabaseRelationDefinition relation;

        public BuilderImpl(DatabaseRelationDefinition relation) {
            this.relation = relation;
        }

        @Override
        public Builder addDeterminant(int determinantIndex) {
            determinants.add(relation.getAttribute(determinantIndex));
            return this;
        }

        @Override
        public Builder addDeterminant(QuotedID determinantId) {
            determinants.add(relation.getAttribute(determinantId));
            return this;
        }

        @Override
        public Builder addDependent(int dependentIndex) {
            dependents.add(relation.getAttribute(dependentIndex));
            return this;
        }

        @Override
        public Builder addDependent(QuotedID dependentId) {
            dependents.add(relation.getAttribute(dependentId));
            return this;
        }

        @Override
        public void build() {
            relation.addFunctionalDependency(new FunctionalDependencyImpl(determinants.build(), dependents.build()));
        }
    }
}
