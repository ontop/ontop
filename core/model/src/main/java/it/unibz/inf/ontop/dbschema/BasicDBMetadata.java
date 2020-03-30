package it.unibz.inf.ontop.dbschema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.impl.BasicDBParametersImpl;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BasicDBMetadata implements DBMetadata {

    private final Map<RelationID, DatabaseRelationDefinition> tables;
    // tables.values() can contain duplicates due to schemaless table names
    private final List<DatabaseRelationDefinition> listOfTables;

    private final DBParameters dbParameters;
    private boolean isStillMutable;

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicDBMetadata.class);

    protected BasicDBMetadata(String driverName, String driverVersion, String databaseProductName, String databaseVersion,
                              QuotedIDFactory idfac, DBTypeFactory dbTypeFactory) {
        this.tables = new HashMap<>();
        this.listOfTables = new ArrayList<>();
        this.isStillMutable = true;
        this.dbParameters = new BasicDBParametersImpl(driverName,  driverVersion,  databaseProductName, databaseVersion, idfac, dbTypeFactory);
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
        if (!isStillMutable) {
            throw new IllegalStateException("Too late, cannot create a DB relation");
        }
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


    @Override
    public DatabaseRelationDefinition getDatabaseRelation(RelationID id) {
        DatabaseRelationDefinition def = tables.get(id);
        if (def == null && id.hasSchema()) {
            def = tables.get(id.getSchemalessID());
        }
        return def;
    }

    @JsonProperty("relations")
    @Override
    public Collection<DatabaseRelationDefinition> getDatabaseRelations() {
        return Collections.unmodifiableCollection(listOfTables);
    }

    public void freeze() {
        isStillMutable = false;
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

    @JsonIgnore
    @Override
    public DBParameters getDBParameters() {
        return dbParameters;
    }

    @JsonProperty("metadata")
    Map<String, String> getMedadataForJsonExport() {
        return ImmutableMap.of(
                "dbmsProductName", getDBParameters().getDbmsProductName(),
                "dbmsVersion", getDBParameters().getDbmsVersion(),
                "driverName", getDBParameters().getDriverName(),
                "driverVersion", getDBParameters().getDriverVersion(),
                "quotationString", getDBParameters().getQuotedIDFactory().getIDQuotationString()
        );
    }

}
