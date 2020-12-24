package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;

import java.util.stream.Collectors;

public class ForeignKeyConstraintImpl implements ForeignKeyConstraint {

    private static final class ComponentImpl implements Component {
        private final Attribute attribute, referencedAttribute;

        private ComponentImpl(Attribute attribute, Attribute referencedAttribute) {
            this.attribute = attribute;
            this.referencedAttribute = referencedAttribute;
        }

        @Override
        public Attribute getAttribute() {
            return attribute;
        }

        @Override
        public Attribute getReferencedAttribute() {
            return referencedAttribute;
        }
    }

    private static final class BuilderImpl implements Builder {
        private final String name;
        private final ImmutableList.Builder<Component> builder = ImmutableList.builder();
        private final NamedRelationDefinition relation, referencedRelation;

        /**
         * creates a FOREIGN KEY builder
         *
         * @param relation
         * @param referencedRelation
         */

        private BuilderImpl(String name, NamedRelationDefinition relation, NamedRelationDefinition referencedRelation) {
            this.name = name;
            this.relation = relation;
            this.referencedRelation = referencedRelation;
        }

        /**
         * adds a pair (attribute, referenced attribute) to the FK constraint
         *
         * @param attributeIndex
         * @param referencedAttributeIndex
         * @return
         */

        @Override
        public Builder add(int attributeIndex, int referencedAttributeIndex) {
            builder.add(new ComponentImpl(relation.getAttribute(attributeIndex), referencedRelation.getAttribute(referencedAttributeIndex)));
            return this;
        }

        @Override
        public Builder add(QuotedID attributeId, QuotedID referencedAttributeId) throws AttributeNotFoundException {
            builder.add(new ComponentImpl(relation.getAttribute(attributeId), referencedRelation.getAttribute(referencedAttributeId)));
            return this;
        }

        /**
         * builds a FOREIGN KEY constraint
         *
         * @throws IllegalArgumentException if the list of components is empty
         */

        public void build() {
            ImmutableList<Component> components = builder.build();
            if (components.isEmpty())
                throw new IllegalArgumentException("No attributes in a foreign key");

            relation.addForeignKeyConstraint(new ForeignKeyConstraintImpl(name, components));
        }
    }

    public static Builder builder(String name, NamedRelationDefinition relation, NamedRelationDefinition referencedRelation) {
        return new BuilderImpl(name, relation, referencedRelation);
    }

    private final String name;
    private final ImmutableList<Component> components;
    private final NamedRelationDefinition relation, referencedRelation;

    /**
     * private constructor (use Builder instead)
     *
     * @param name
     * @param components
     */

    private ForeignKeyConstraintImpl(String name, ImmutableList<Component> components) {
        this.name = name;
        this.components = components;
        this.relation = (NamedRelationDefinition)components.get(0).getAttribute().getRelation();
        this.referencedRelation = (NamedRelationDefinition)components.get(0).getReferencedAttribute().getRelation();
    }

    /**
     * returns the name of the foreign key constraint
     *
     * @return name
     */

    @Override
    public String getName() {
        return name;
    }

    /**
     * returns the components of the foreign key constraint
     * each component defines a map from an attribute of the relation
     * to an attribute of the referenced relation
     *
     * @return
     */

    @Override
    public ImmutableList<Component> getComponents() {
        return components;
    }

    /**
     * returns referenced database relation
     *
     * @return referenced relation
     */

    @Override
    public NamedRelationDefinition getReferencedRelation() {
        return referencedRelation;
    }

    /**
     * returns the relation with the foreign key
     *
     * @return relation
     */

    @Override
    public NamedRelationDefinition getRelation() {
        return relation;
    }

    @Override
    public String toString() {
        return "ALTER TABLE " + relation.getID().getSQLRendering() +
                " ADD CONSTRAINT " + name + " FOREIGN KEY (" +
                components.stream()
                        .map(c -> c.getAttribute().getID().toString())
                        .collect(Collectors.joining(", ")) +
                ") REFERENCES " + referencedRelation.getID().getSQLRendering() +
                " (" +
                components.stream()
                        .map(c -> c.getReferencedAttribute().getID().toString())
                        .collect(Collectors.joining(", ")) +
                ")";
    }
}
