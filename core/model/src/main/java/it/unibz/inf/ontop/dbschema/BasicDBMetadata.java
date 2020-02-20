package it.unibz.inf.ontop.dbschema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.unibz.inf.ontop.dbschema.impl.BasicDBParametersImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BasicDBMetadata implements DBMetadata {

    private final Map<RelationID, DatabaseRelationDefinition> tables;
    // tables.values() can contain duplicates due to schemaless table names
    private final List<DatabaseRelationDefinition> listOfTables;

    private final String driverName;
    private final String driverVersion;
    private final String databaseProductName;
    private final String databaseVersion;

    private final DBParameters dbParameters;
    private boolean isStillMutable;

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicDBMetadata.class);

    protected BasicDBMetadata(String driverName, String driverVersion, String databaseProductName, String databaseVersion,
                              QuotedIDFactory idfac) {
        this.driverName = driverName;
        this.driverVersion = driverVersion;
        this.databaseProductName = databaseProductName;
        this.databaseVersion = databaseVersion;
        this.tables = new HashMap<>();
        this.listOfTables = new ArrayList<>();
        this.isStillMutable = true;
        this.dbParameters = new BasicDBParametersImpl(idfac);
    }

    /**
     * creates a database table (which can also be a database view)
     * if the <name>id</name> contains schema than the relation is added
     * to the lookup table (see getDatabaseRelation and getRelation) with
     * both the fully qualified id and the table name only id
     *
     * @param id
     * @return
     */
    public DatabaseRelationDefinition createDatabaseRelation(RelationID id) {
        if (!isStillMutable) {
            throw new IllegalStateException("Too late, cannot create a DB relation");
        }
        DatabaseRelationDefinition table = new DatabaseRelationDefinition(id);
        add(table, tables);
        listOfTables.add(table);
        return table;
    }

    /**
     * Inserts a new data definition to this metadata object.
     *
     * @param td
     *            The data definition. It can be a {@link DatabaseRelationDefinition} or a
     *            {@link ParserViewDefinition} object.
     */
    protected <T extends RelationDefinition> void add(T td, Map<RelationID, T> schema) {
        if (!isStillMutable) {
            throw new IllegalStateException("Too late, cannot add a schema");
        }
        schema.put(td.getID(), td);
        if (td.getID().hasSchema()) {
            RelationID noSchemaID = td.getID().getSchemalessID();
            if (!schema.containsKey(noSchemaID)) {
                schema.put(noSchemaID, td);
            }
            else {
                LOGGER.warn("DUPLICATE TABLE NAMES, USE QUALIFIED NAMES:\n" + td + "\nAND\n" + schema.get(noSchemaID));
                //schema.remove(noSchemaID);
                // TODO (ROMAN 8 Oct 2015): think of a better way of resolving ambiguities
            }
        }
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

    @Override
    public void freeze() {
        isStillMutable = false;
    }

    @JsonIgnore
    public String getDriverName() {
        return driverName;
    }

    @JsonIgnore
    public String getDriverVersion() {
        return driverVersion;
    }

    @JsonIgnore
    public String getDbmsProductName() {
        return databaseProductName;
    }

    @JsonIgnore
    public String getDbmsVersion() {
        return databaseVersion;
    }

    @JsonIgnore
    public QuotedIDFactory getQuotedIDFactory() {
        return dbParameters.getQuotedIDFactory();
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

    protected boolean isStillMutable() {
        return isStillMutable;
    }

    @JsonIgnore
    @Override
    public DBParameters getDBParameters() {
        return dbParameters;
    }

}
