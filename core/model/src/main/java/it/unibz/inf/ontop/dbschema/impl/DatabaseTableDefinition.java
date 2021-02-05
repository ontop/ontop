package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.RelationID;

import java.util.stream.Collectors;

public class DatabaseTableDefinition extends AbstractNamedRelationDefinition {

    /**
     *
     * @param allIds
     * @param builder
     */
    public DatabaseTableDefinition(ImmutableList<RelationID> allIds, AttributeListBuilder builder) {
        super(allIds, builder);
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
