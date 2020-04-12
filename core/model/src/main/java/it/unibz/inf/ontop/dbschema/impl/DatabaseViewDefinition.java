package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableSet;
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
    DatabaseViewDefinition(RelationID id, ImmutableSet<RelationID> allIds, AttributeListBuilder builder, String body) {
        super(id, allIds, builder);
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
