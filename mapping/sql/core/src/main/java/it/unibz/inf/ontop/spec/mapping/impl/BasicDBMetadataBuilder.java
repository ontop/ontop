package it.unibz.inf.ontop.spec.mapping.impl;

import it.unibz.inf.ontop.dbschema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BasicDBMetadataBuilder {

    private final Map<RelationID, DatabaseRelationDefinition> tables = new HashMap<>();

    BasicDBMetadataBuilder() {
    }

    /**
     * creates a database table (which can also be a database view)
     * if the <name>id</name> contains schema than the relation is added
     * to the lookup table (see getDatabaseRelation and getRelation) with
     * both the fully qualified id and the table name only id
     *
     * @param builder
     * @return
     */
    DatabaseRelationDefinition createDatabaseRelation(RelationDefinition.AttributeListBuilder builder) {
        DatabaseRelationDefinition table = new DatabaseRelationDefinition(builder);
        tables.put(table.getID(), table);
        if (table.getID().hasSchema()) {
            RelationID noSchemaID = table.getID().getSchemalessID();
            if (!tables.containsKey(noSchemaID)) {
                tables.put(noSchemaID, table);
            }
            else {
                //schema.remove(noSchemaID);
                // TODO (ROMAN 8 Oct 2015): think of a better way of resolving ambiguities
            }
        }
        return table;
    }

    MetadataLookup getMetadataLookup() {
        return id -> {
            DatabaseRelationDefinition def = tables.get(id);
            if (def == null && id.hasSchema())
                def = tables.get(id.getSchemalessID());

            return Optional.ofNullable(def);
        };
    }
}
