package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.RelationID;

import java.util.stream.Collectors;

public class DatabaseTableDefinition extends AbstractDatabaseRelationDefinition {

    /**
     * used only in DummyDBMetadataBuilder
     *
     * @param id
     * @param allIds
     * @param builder
     */
    DatabaseTableDefinition(RelationID id, ImmutableSet<RelationID> allIds, AttributeListBuilder builder) {
        super(id, allIds, builder);
        System.out.println("NEW-TABLE: " + id + "(" + allIds + ")");
    }

    @Override
    public String toString() {
        return "CREATE TABLE " + getID() + " (\n   " +
                getAttributes().stream()
                        .map(Attribute::toString)
                        .collect(Collectors.joining(",\n   ")) +
                "\n)";
    }

}
