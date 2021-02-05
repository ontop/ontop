package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Collectors;

public class UniqueConstraintImpl implements UniqueConstraint {

    private static class UniqueConstraintBuilder implements Builder {
        protected final ImmutableList.Builder<Attribute> builder = ImmutableList.builder();
        protected final NamedRelationDefinition relation;
        protected final String name;

        private UniqueConstraintBuilder(NamedRelationDefinition relation, String name) {
            this.relation = relation;
            this.name = name;
        }

        @Override
        public Builder addDeterminant(int determinantIndex) {
            builder.add(relation.getAttribute(determinantIndex));
            return this;
        }

        @Override
        public Builder addDeterminant(QuotedID determinantId) throws AttributeNotFoundException {
            builder.add(relation.getAttribute(determinantId));
            return this;
        }

        @Override
        public Builder addDependent(int dependentIndex) {
            throw new IllegalArgumentException("No dependents");
        }

        @Override
        public Builder addDependent(QuotedID dependentId) {
            throw new IllegalArgumentException("No dependents");
        }

        @Override
        public void build() {
            ImmutableList<Attribute> attributes = builder.build();
            if (attributes.isEmpty())
                throw new IllegalArgumentException("UC cannot have no attributes");

            Optional<UniqueConstraint> pk = relation.getPrimaryKey();
            if (pk.isPresent() && pk.get().getAttributes().equals(attributes))
                return; // ignore a unique constraint with the same attributes as the primary key

            relation.addFunctionalDependency(new UniqueConstraintImpl(name, false, attributes));
        }
    }

    private static class PrimaryKeyBuilder extends UniqueConstraintBuilder {

        private PrimaryKeyBuilder(NamedRelationDefinition relation, String name) {
            super(relation, name);
        }

        @Override
        public Builder addDeterminant(int determinantIndex) {
            Attribute attribute = relation.getAttribute(determinantIndex);
            if (attribute.isNullable())
                throw new IllegalArgumentException("Nullable attribute " + attribute + " cannot be in a PK");

            builder.add(attribute);
            return this;
        }

        @Override
        public Builder addDeterminant(QuotedID determinantId) throws AttributeNotFoundException {
            Attribute attribute = relation.getAttribute(determinantId);
            if (attribute.isNullable())
                throw new IllegalArgumentException("Nullable attribute " + attribute + " cannot be in a PK");

            builder.add(attribute);
            return this;
        }

        @Override
        public void build() {
            ImmutableList<Attribute> attributes = builder.build();
            if (attributes.isEmpty())
                throw new IllegalArgumentException("PK cannot have no attributes");

            relation.addFunctionalDependency(new UniqueConstraintImpl(name, true, attributes));
        }
    }

    /**
     * creates a UNIQUE constraint builder
     *
     * @param relation
     * @param name
     * @return
     */

    public static Builder builder(NamedRelationDefinition relation, String name) {
        return new UniqueConstraintBuilder(relation, name);
    }

    /**
     * creates a PRIMARY KEY  builder
     *
     * @param relation
     * @param name
     * @return
     */

    public static Builder primaryKeyBuilder(NamedRelationDefinition relation, String name) {
        return new PrimaryKeyBuilder(relation, name);
    }

    private final String name;
    private final ImmutableList<Attribute> attributes;
    private final boolean isPrimaryKey;

    /**
     * private constructor (use Builder instead)
     *
     * @param name
     * @param attributes
     */

    private UniqueConstraintImpl(String name, boolean isPrimaryKey, ImmutableList<Attribute> attributes) {
        this.name = name;
        this.isPrimaryKey = isPrimaryKey;
        this.attributes = attributes;
    }

    /**
     * return the name of the constraint
     *
     * @return name
     */

    @Override
    public String getName() {
        return name;
    }

    /**
     * return true if it is a primary key and false otherwise
     *
     * @return true if it is a primary key constraint (false otherwise)
     */

    @Override
    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    /**
     * return the list of attributes in the unique constraint
     *
     * @return list of attributes
     */

    @Override
    public ImmutableList<Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public ImmutableSet<Attribute> getDeterminants() {
        return ImmutableSet.copyOf(attributes);
    }

    @Override
    public ImmutableSet<Attribute> getDependents() {
        return attributes.get(0).getRelation().getAttributes().stream()
                .filter(a -> !attributes.contains(a))
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public String toString() {
        return "ALTER TABLE " + ((NamedRelationDefinition)attributes.get(0).getRelation()).getID() +
                " ADD CONSTRAINT " + name + (isPrimaryKey ? " PRIMARY KEY " : " UNIQUE ") +
                "(" +
                attributes.stream()
                        .map(Attribute::getID)
                        .map(QuotedID::toString)
                        .collect(Collectors.joining(", ")) +
                ")";
    }

}
