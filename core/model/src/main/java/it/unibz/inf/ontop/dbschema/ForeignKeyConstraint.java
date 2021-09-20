package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.impl.ForeignKeyConstraintImpl;


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

public interface ForeignKeyConstraint {

    interface Component {
        Attribute getAttribute();
        Attribute getReferencedAttribute();
    }


    interface Builder {

        /**
         * adds a pair (attribute, referenced attribute) to the FK constraint
         *
         * @param attributeIndex
         * @param referencedAttributeIndex
         * @return
         */

        Builder add(int attributeIndex, int referencedAttributeIndex);

        Builder add(QuotedID attributeId, QuotedID referencedAttributeId) throws AttributeNotFoundException;

        /**
         * builds a FOREIGN KEY constraint
         *
         * @throws IllegalArgumentException if the list of components is empty
         */

        void build();
    }


    /**
     * creates a FOREIGN KEY builder
     *
     * @param name
     * @param relation
     * @param referencedRelation
     * @return
     */

    static Builder builder(String name, NamedRelationDefinition relation, NamedRelationDefinition referencedRelation) {
        return ForeignKeyConstraintImpl.builder(name, relation, referencedRelation);
    }

    /**
     * creates a single-attribute foreign key
     */
    static void of(String name, Attribute attribute, Attribute referencedAttribute) {
        builder(name, (NamedRelationDefinition)attribute.getRelation(), (NamedRelationDefinition)referencedAttribute.getRelation())
                .add(attribute.getIndex(), referencedAttribute.getIndex()).build();
    }

    /**
     * returns the name of the foreign key constraint
     *
     * @return name
     */

    String getName();

    /**
     * returns the components of the foreign key constraint
     * each component defines a map from an attribute of the relation
     * to an attribute of the referenced relation
     *
     * @return
     */

    ImmutableList<Component> getComponents();

    /**
     * returns referenced database relation
     *
     * @return referenced relation
     */

    NamedRelationDefinition getReferencedRelation();

    /**
     * returns the relation with the foreign key
     *
     * @return relation
     */

    NamedRelationDefinition getRelation();

}

