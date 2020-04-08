package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.ImmutableDBMetadataImpl;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BasicDBMetadataBuilder {

    private final Map<RelationID, DatabaseRelationDefinition> tables;
    // tables.values() can contain duplicates due to schemaless table names
    private final List<DatabaseRelationDefinition> listOfTables;

    private final DBParameters dbParameters;

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicDBMetadataBuilder.class);

    public BasicDBMetadataBuilder(DBParameters dbParameters) {
        this.tables = new HashMap<>();
        this.listOfTables = new ArrayList<>();
        this.dbParameters = dbParameters;
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
    public DatabaseRelationDefinition createDatabaseRelation(RelationDefinition.AttributeListBuilder builder) {
        DatabaseRelationDefinition table = new DatabaseRelationDefinition(builder);
        tables.put(table.getID(), table);
        if (table.getID().hasSchema()) {
            RelationID noSchemaID = table.getID().getSchemalessID();
            if (!tables.containsKey(noSchemaID)) {
                tables.put(noSchemaID, table);
            }
            else {
                LOGGER.warn("DUPLICATE TABLE NAMES, USE QUALIFIED NAMES:\n" + table + "\nAND\n" + tables.get(noSchemaID));
                //schema.remove(noSchemaID);
                // TODO (ROMAN 8 Oct 2015): think of a better way of resolving ambiguities
            }
        }
        listOfTables.add(table);
        return table;
    }

    public MetadataLookup getMetadataLookup() {
        return new MetadataLookup() {
            @Override
            public DatabaseRelationDefinition get(RelationID id) throws RelationNotFoundException {
                DatabaseRelationDefinition def = tables.get(id);
                if (def == null && id.hasSchema()) {
                    def = tables.get(id.getSchemalessID());
                }
                if (def != null)
                    return def;
                throw new RelationNotFoundException(id);
            }

            @Override
            public QuotedIDFactory getQuotedIDFactory() {
                return dbParameters.getQuotedIDFactory();
            }

            @Override
            public DBTypeFactory getDBTypeFactory() {
                return dbParameters.getDBTypeFactory();
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder bf = new StringBuilder();
        for (Map.Entry<RelationID, DatabaseRelationDefinition> e : tables.entrySet()) {
            bf.append(e.getKey()).append("=").append(e.getValue()).append("\n");
        }
        // Prints all primary keys
        bf.append("\n====== constraints ==========\n");
        for (Map.Entry<RelationID, DatabaseRelationDefinition> e : tables.entrySet()) {
            for (UniqueConstraint uc : e.getValue().getUniqueConstraints())
                bf.append(uc).append(";\n");
            bf.append("\n");
            for (ForeignKeyConstraint fk : e.getValue().getForeignKeys())
                bf.append(fk).append(";\n");
            bf.append("\n");
        }
        return bf.toString();
    }

    public DBParameters getDBParameters() {
        return dbParameters;
    }


    public ImmutableDBMetadata build() {
        return new ImmutableDBMetadataImpl(dbParameters, ImmutableList.copyOf(listOfTables));
    }
}
