package it.unibz.inf.ontop.dbschema;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Foreign Key constraints<br>
 * <p>
 * FOREIGN KEY (columnName (, columnName)*)
 * REFERENCES refTableName	(refColumnName (, refColumnName)*)<br>
 * <p>
 * (a particular case of linear tuple-generating dependencies<br>
 * \forall x (\exists y_1 R_1(x,y_1) \to \exists y_2 R_2(x,y_2))<br>
 * where x, y_1 and y_2 are *tuples* of variables)
 *
 * @author Roman Kontchakov
 */

public class ForeignKeyConstraint {

    public static final class Component {
        private final Attribute attribute, reference;

        private Component(Attribute attribute, Attribute reference) {
            this.attribute = attribute;
            this.reference = reference;
        }

        public Attribute getAttribute() {
            return attribute;
        }

        public Attribute getReference() {
            return reference;
        }
    }

    public static final class Builder {
        private final String name;
        private final ImmutableList.Builder<Component> builder = ImmutableList.builder();
        private final DatabaseRelationDefinition relation, referencedRelation;

        /**
         * creates a FOREIGN KEY builder
         *
         * @param relation
         * @param referencedRelation
         */

        private Builder(String name, DatabaseRelationDefinition relation, DatabaseRelationDefinition referencedRelation) {
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

        public Builder add(int attributeIndex, int referencedAttributeIndex) {
            builder.add(new Component(relation.getAttribute(attributeIndex), referencedRelation.getAttribute(referencedAttributeIndex)));
            return this;
        }

        public Builder add(QuotedID attributeId, QuotedID referencedAttributeId) throws RelationDefinition.AttributeNotFoundException {
            builder.add(new Component(relation.getAttribute(attributeId), referencedRelation.getAttribute(referencedAttributeId)));
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

            relation.addForeignKeyConstraint(new ForeignKeyConstraint(name, components));
        }
    }

    /**
     * creates a FOREIGN KEY builder
     *
     * @param name
     * @param relation
     * @param referencedRelation
     * @return
     */

    public static Builder builder(String name, DatabaseRelationDefinition relation, DatabaseRelationDefinition referencedRelation) {
        return new Builder(name, relation, referencedRelation);
    }

    /**
     * creates a single-attribute foreign key
     *
     * @param name
     * @param attribute
     * @param reference
     * @return
     */

    public static void of(String name, Attribute attribute, Attribute reference) {
        builder(name, (DatabaseRelationDefinition)attribute.getRelation(), (DatabaseRelationDefinition)reference.getRelation())
                .add(attribute.getIndex(), reference.getIndex()).build();
    }

    private final String name;
    private final ImmutableList<Component> components;
    private final RelationDefinition relation, referencedRelation;

    /**
     * private constructor (use Builder instead)
     *
     * @param name
     * @param components
     */

    private ForeignKeyConstraint(String name, ImmutableList<Component> components) {
        this.name = name;
        this.components = components;
        this.relation = components.get(0).getAttribute().getRelation();
        this.referencedRelation = components.get(0).getReference().getRelation();
    }

    /**
     * returns the name of the foreign key constraint
     *
     * @return name
     */

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

    public ImmutableList<Component> getComponents() {
        return components;
    }

    /**
     * returns referenced database relation
     *
     * @return referenced relation
     */

    public RelationDefinition getReferencedRelation() {
        return referencedRelation;
    }

    /**
     * returns the relation with the foreign key
     *
     * @return relation
     */

    public RelationDefinition getRelation() {
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
                        .map(c -> c.getReference().getID().toString())
                        .collect(Collectors.joining(", ")) +
                ")";
    }

    public static class ForeignKeyConstraintSerializer extends JsonSerializer<ForeignKeyConstraint> {
        @Override
        public void serialize(ForeignKeyConstraint value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

            gen.writeStartObject();
            {
                gen.writeStringField("name", value.getName());
                {
                    gen.writeFieldName("from");
                    gen.writeStartObject();
                    gen.writeStringField("relation", value.relation.getID().getSQLRendering());
                    {
                        gen.writeArrayFieldStart("columns");
                        for (Component component : value.getComponents()) {
                            gen.writeString(component.getAttribute().getID().getSQLRendering());
                        }
                        gen.writeEndArray();
                    }
                    gen.writeEndObject();
                }
                {
                    gen.writeFieldName("to");
                    gen.writeStartObject();
                    gen.writeStringField("relation", value.referencedRelation.getID().getSQLRendering());
                    {
                        gen.writeArrayFieldStart("columns");
                        for (Component component : value.getComponents()) {
                            gen.writeString(component.getReference().getID().getSQLRendering());
                        }
                        gen.writeEndArray();
                    }
                    gen.writeEndObject();
                }
            }
            gen.writeEndObject();
        }
    }
}
