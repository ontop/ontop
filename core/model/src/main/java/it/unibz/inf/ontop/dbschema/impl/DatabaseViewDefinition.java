package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.RelationID;

import java.util.stream.Collectors;

public class DatabaseViewDefinition extends AbstractDatabaseRelationDefinition {

    private final String body;

    /**
     * used only in DummyDBMetadataBuilder
     *
     * @param id
     * @param builder
     */
    DatabaseViewDefinition(RelationID id, AttributeListBuilder builder, String body) {
        super(id, builder);
        this.body = body;
    }

    @Override
    public String toString() {
        return "CREATE VIEW " + getID() + " (\n   " +
                getAttributes().stream()
                        .map(Attribute::toString)
                        .collect(Collectors.joining(",\n   ")) +
                "\n) AS " + body;
    }

}
