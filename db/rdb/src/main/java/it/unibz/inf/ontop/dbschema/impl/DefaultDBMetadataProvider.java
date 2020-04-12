package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DefaultDBMetadataProvider implements MetadataProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDBMetadataProvider.class);

    protected final Connection connection;
    protected final QuotedIDFactory idFactory;
    protected final DBTypeFactory dbTypeFactory;
    protected final DBParameters dbParameters;
    protected final DatabaseMetaData metadata;

    protected final QuotedIDFactory rawIdFactory;

    DefaultDBMetadataProvider(Connection connection, QuotedIDFactory idFactory, DBTypeFactory dbTypeFactory) throws MetadataExtractionException {
        this.connection = connection;
        this.dbTypeFactory = dbTypeFactory;
        try {
            this.metadata = connection.getMetaData();
            this.idFactory = idFactory;
            this.rawIdFactory = new RawQuotedIDFactory(idFactory);
            this.dbParameters = getDBParameters(metadata, idFactory, dbTypeFactory);
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    DefaultDBMetadataProvider(Connection connection, DBTypeFactory dbTypeFactory) throws MetadataExtractionException {
        this.connection = connection;
        this.dbTypeFactory = dbTypeFactory;
        try {
            this.metadata = connection.getMetaData();
            this.idFactory = getQuotedIDFactory(metadata);
            this.rawIdFactory = new RawQuotedIDFactory(idFactory);
            this.dbParameters = getDBParameters(metadata, idFactory, dbTypeFactory);
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    protected static DBParameters getDBParameters(DatabaseMetaData metadata, QuotedIDFactory idFactory, DBTypeFactory dbTypeFactory) throws SQLException {
        return new BasicDBParametersImpl(metadata.getDriverName(), metadata.getDriverVersion(),
                metadata.getDatabaseProductName(), metadata.getDatabaseProductVersion(), idFactory, dbTypeFactory);
    }

    protected static QuotedIDFactory getQuotedIDFactory(DatabaseMetaData md) throws SQLException {

        if (md.storesMixedCaseIdentifiers())
            // treat Exareme as a case-sensitive DB engine (like MS SQL Server)
            // "SQL Server" = MS SQL Server
            return new SQLServerQuotedIDFactory();

        else if (md.storesLowerCaseIdentifiers())
            // PostgreSQL treats unquoted identifiers as lower-case
            return new PostgreSQLQuotedIDFactory();

        else if (md.storesUpperCaseIdentifiers())
            // Oracle, DB2, H2, HSQL
            return new SQLStandardQuotedIDFactory();

        // UNKNOWN COMBINATION
        LOGGER.warn("Unknown combination of identifier handling rules: " + md.getDatabaseProductName());
        LOGGER.warn("storesLowerCaseIdentifiers: " + md.storesLowerCaseIdentifiers());
        LOGGER.warn("storesUpperCaseIdentifiers: " + md.storesUpperCaseIdentifiers());
        LOGGER.warn("storesMixedCaseIdentifiers: " + md.storesMixedCaseIdentifiers());
        LOGGER.warn("supportsMixedCaseIdentifiers: " + md.supportsMixedCaseIdentifiers());
        LOGGER.warn("storesLowerCaseQuotedIdentifiers: " + md.storesLowerCaseQuotedIdentifiers());
        LOGGER.warn("storesUpperCaseQuotedIdentifiers: " + md.storesUpperCaseQuotedIdentifiers());
        LOGGER.warn("storesMixedCaseQuotedIdentifiers: " + md.storesMixedCaseQuotedIdentifiers());
        LOGGER.warn("supportsMixedCaseQuotedIdentifiers: " + md.supportsMixedCaseQuotedIdentifiers());
        LOGGER.warn("getIdentifierQuoteString: " + md.getIdentifierQuoteString());

        return new SQLStandardQuotedIDFactory();
    }

    @Override
    public ImmutableList<RelationID> getRelationIDs() throws MetadataExtractionException {
        try (ResultSet rs = metadata.getTables(null, null, null, new String[] { "TABLE", "VIEW" })) {
            ImmutableList.Builder<RelationID> builder = ImmutableList.builder();
            while (rs.next()) {
                // String catalog = rs.getString("TABLE_CAT"); // not used
                String schema = rs.getString("TABLE_SCHEM");
                String table = rs.getString("TABLE_NAME");
                if (!isSchemaIgnored(schema)) {
                    RelationID id = rawIdFactory.createRelationID(schema, table);
                    builder.add(id);
                }
            }
            return builder.build();
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    protected RelationID getRelationCanonicalID(RelationID id) {  return id; }

    @Override
    public DatabaseRelationDefinition getRelation(RelationID id0) throws MetadataExtractionException {

        RelationID id = getRelationCanonicalID(id0);
        try (ResultSet rs = metadata.getColumns(getRelationCatalog(id), getRelationSchema(id), getRelationName(id), null)) {
            Map<RelationID, RelationDefinition.AttributeListBuilder> relations = new HashMap<>();

            while (rs.next()) {
                RelationID relationId = getRelationID(rs);
                RelationDefinition.AttributeListBuilder builder = relations.computeIfAbsent(relationId,
                        i -> DatabaseTableDefinition.attributeListBuilder());

                QuotedID attributeId = rawIdFactory.createAttributeID(rs.getString("COLUMN_NAME"));
                // columnNoNulls, columnNullable, columnNullableUnknown
                boolean isNullable = rs.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls;
                String typeName = rs.getString("TYPE_NAME");
                int columnSize = rs.getInt("COLUMN_SIZE");
                DBTermType termType = dbTypeFactory.getDBTermType(typeName, columnSize);

                builder.addAttribute(attributeId, termType, typeName, isNullable);
            }

            if (relations.isEmpty()) {
                throw new MetadataExtractionException("Cannot find relation id: " + id);
            }
            else if (relations.keySet().size() == 1) {
                Map.Entry<RelationID, RelationDefinition.AttributeListBuilder> r = relations.entrySet().iterator().next();
                return new DatabaseTableDefinition(r.getKey(), r.getValue());
            }
            else {
                RelationID canonicalId = getRelationCanonicalID(id);
                for (Map.Entry<RelationID, RelationDefinition.AttributeListBuilder> r : relations.entrySet())
                    if (r.getKey().equals(canonicalId))
                        return new DatabaseTableDefinition(r.getKey(), r.getValue());
            }
            throw new MetadataExtractionException("Cannot resolve ambiguous relation id: " + id);
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    @Override
    public void insertIntegrityConstraints(DatabaseRelationDefinition relation, MetadataLookup metadataLookup) throws MetadataExtractionException {
        insertPrimaryKey(relation);
        insertUniqueAttributes(relation);
        insertForeignKeys(relation, metadataLookup);
    }

    @Override
    public DBParameters getDBParameters() {
        return dbParameters;
    }

    private void insertPrimaryKey(DatabaseRelationDefinition relation) throws MetadataExtractionException {
        RelationID id = relation.getID();
        // Retrieves a description of the given table's primary key columns. They are ordered by COLUMN_NAME (sic!)
        try (ResultSet rs = metadata.getPrimaryKeys(getRelationCatalog(id), getRelationSchema(id), getRelationName(id))) {
            Map<Integer, QuotedID> primaryKeyAttributes = new HashMap<>();
            String currentName = null;
            while (rs.next()) {
                // TABLE_CAT is ignored for now; assume here that relation has a fully specified name
                RelationID id2 = getRelationID(rs);
                if (id2.equals(id)) {
                    currentName = rs.getString("PK_NAME"); // may be null
                    QuotedID attrId = rawIdFactory.createAttributeID(rs.getString("COLUMN_NAME"));
                    int seq = rs.getShort("KEY_SEQ");
                    primaryKeyAttributes.put(seq, attrId);
                }
            }
            if (!primaryKeyAttributes.isEmpty()) {
                try {
                    // use the KEY_SEQ values to restore the correct order of attributes in the PK
                    UniqueConstraint.Builder builder = UniqueConstraint.primaryKeyBuilder(relation, currentName);
                    for (int i = 1; i <= primaryKeyAttributes.size(); i++)
                        builder.addDeterminant(primaryKeyAttributes.get(i));
                    builder.build();
                }
                catch (AttributeNotFoundException e) {
                    throw new MetadataExtractionException(e);
                }
            }
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    private void insertUniqueAttributes(DatabaseRelationDefinition relation) throws MetadataExtractionException {
        RelationID id = relation.getID();
        // extracting unique
        try (ResultSet rs = metadata.getIndexInfo(getRelationCatalog(id), getRelationSchema(id), getRelationName(id), true, true)) {
            UniqueConstraint.Builder builder = null;
            while (rs.next()) {
                // TYPE: tableIndexStatistic - this identifies table statistics that are returned in conjunction with a table's index descriptions
                //       tableIndexClustered - this is a clustered index
                //       tableIndexHashed - this is a hashed index
                //       tableIndexOther (all are static final int in DatabaseMetaData)
                if (rs.getShort("TYPE") == DatabaseMetaData.tableIndexStatistic) {
                    if (builder != null)
                        builder.build();

                    builder = null;
                    continue;
                }
                if (rs.getShort("ORDINAL_POSITION") == 1) {
                    if (builder != null)
                        builder.build();

                    // TABLE_CAT is ignored for now; assume here that relation has a fully specified name
                    // and so, no need to check whether TABLE_SCHEM and TABLE_NAME match

                    if (!rs.getBoolean("NON_UNIQUE")) {
                        String name = rs.getString("INDEX_NAME");
                        builder = UniqueConstraint.builder(relation, name);
                    }
                    else
                        builder = null;
                }

                if (builder != null) {
                    QuotedID attrId = rawIdFactory.createAttributeID(rs.getString("COLUMN_NAME"));
                    // ASC_OR_DESC String => column sort sequence, "A" => ascending, "D" => descending,
                    //        may be null if sort sequence is not supported; null when TYPE is tableIndexStatistic
                    // CARDINALITY int => When TYPE is tableIndexStatistic, then this is the number of rows in the table;
                    //                      otherwise, it is the number of unique values in the index.
                    // PAGES int => When TYPE is tableIndexStatisic then this is the number of pages used for the table,
                    //                    otherwise it is the number of pages used for the current index.
                    // FILTER_CONDITION String => Filter condition, if any. (may be null)
                    try {
                        builder.addDeterminant(attrId);
                    }
                    catch (AttributeNotFoundException e) {
                        try {
                            // bug in PostgreSQL JBDC driver: it strips off the quotation marks
                            attrId = rawIdFactory.createAttributeID("\"" + rs.getString("COLUMN_NAME") + "\"");
                            builder.addDeterminant(attrId);
                        }
                        catch (AttributeNotFoundException ex) {
                           throw new MetadataExtractionException(e);
                        }
                    }
                }
            }
            if (builder != null)
                builder.build();
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    private void insertForeignKeys(DatabaseRelationDefinition relation, MetadataLookup dbMetadata) throws MetadataExtractionException {

        RelationID id = relation.getID();
        try (ResultSet rs = metadata.getImportedKeys(getRelationCatalog(id), getRelationSchema(id), getRelationName(id))) {
            ForeignKeyConstraint.Builder builder = null;
            while (rs.next()) {
                try {
                    int seq = rs.getShort("KEY_SEQ");
                    if (seq == 1) {
                        if (builder != null)
                            builder.build();

                        String name = rs.getString("FK_NAME"); // String => foreign key name (may be null)
                        DatabaseRelationDefinition ref = (DatabaseRelationDefinition) dbMetadata.getRelation(getPKRelationID(rs));

                        // FKTABLE_SCHEM and FKTABLE_NAME are ignored for now
                        builder = ForeignKeyConstraint.builder(name, relation, ref);
                    }
                    if (builder != null) {
                        try {
                            QuotedID attrId = rawIdFactory.createAttributeID(rs.getString("FKCOLUMN_NAME"));
                            QuotedID refAttrId = rawIdFactory.createAttributeID(rs.getString("PKCOLUMN_NAME"));
                            builder.add(attrId, refAttrId);
                        }
                        catch (AttributeNotFoundException e) {
                            throw new MetadataExtractionException(e);
                        }
                    }
                }
                catch (MetadataExtractionException e) {
                    builder = null; // do not add this foreign key
                    // because there is no table it refers to
                    System.out.println("Cannot find table: " + getPKRelationID(rs) + " for FK " + rs.getString("FK_NAME"));
                }
            }
            if (builder != null)
                builder.build();
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }





    protected boolean isSchemaIgnored(String schema) {
        return false;
    }

    // catalog is ignored for now (rs.getString("TABLE_CAT"))
    protected String getRelationCatalog(RelationID relationID) { return null; }

    protected String getRelationSchema(RelationID relationID) { return relationID.getSchemaID().getName(); }

    protected String getRelationName(RelationID relationID) { return relationID.getTableID().getName(); }

    protected RelationID getRelationID(ResultSet rs) throws SQLException {
        return rawIdFactory.createRelationID(
                rs.getString("TABLE_SCHEM"), rs.getString("TABLE_NAME"));
    }

    protected RelationID getPKRelationID(ResultSet rs) throws SQLException {
        return rawIdFactory.createRelationID(
                rs.getString("PKTABLE_SCHEM"), rs.getString("PKTABLE_NAME"));
    }

}