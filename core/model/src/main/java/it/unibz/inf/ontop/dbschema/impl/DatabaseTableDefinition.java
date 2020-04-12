package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.RelationID;

import java.util.stream.Collectors;

public class DatabaseTableDefinition extends AbstractDatabaseRelationDefinition {

    /**
     * used only in DummyDBMetadataBuilder
     *
     * @param id
     * @param builder
     */
    DatabaseTableDefinition(RelationID id, AttributeListBuilder builder) {
        super(id, builder);
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
