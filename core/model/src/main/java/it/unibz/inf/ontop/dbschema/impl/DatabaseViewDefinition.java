package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.RelationID;

import java.util.stream.Collectors;

public class DatabaseViewDefinition extends AbstractNamedRelationDefinition {

    private final String body;

    /**
     *
     * @param allIds
     * @param builder
     */
    DatabaseViewDefinition(ImmutableList<RelationID> allIds, AttributeListBuilder builder, String body) {
        super(allIds, builder);
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
