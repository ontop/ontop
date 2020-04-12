package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.RelationID;

import java.util.stream.Collectors;

public class DatabaseTableDefinition extends AbstractDatabaseRelationDefinition {

    /**
     * used only in DummyDBMetadataBuilder
     *
     * @param allIds
     * @param builder
     */
    DatabaseTableDefinition(ImmutableList<RelationID> allIds, AttributeListBuilder builder) {
        super(allIds.get(0), ImmutableSet.copyOf(allIds), builder);
        System.out.println("NEW-TABLE: " + allIds);
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
