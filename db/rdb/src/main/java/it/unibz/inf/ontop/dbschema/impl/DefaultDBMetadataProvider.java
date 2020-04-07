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

public class DefaultDBMetadataProvider implements RDBMetadataProvider {

    private static Logger log = LoggerFactory.getLogger(DefaultDBMetadataProvider.class);

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
        log.warn("Unknown combination of identifier handling rules: " + md.getDatabaseProductName());
        log.warn("storesLowerCaseIdentifiers: " + md.storesLowerCaseIdentifiers());
        log.warn("storesUpperCaseIdentifiers: " + md.storesUpperCaseIdentifiers());
        log.warn("storesMixedCaseIdentifiers: " + md.storesMixedCaseIdentifiers());
        log.warn("supportsMixedCaseIdentifiers: " + md.supportsMixedCaseIdentifiers());
        log.warn("storesLowerCaseQuotedIdentifiers: " + md.storesLowerCaseQuotedIdentifiers());
        log.warn("storesUpperCaseQuotedIdentifiers: " + md.storesUpperCaseQuotedIdentifiers());
        log.warn("storesMixedCaseQuotedIdentifiers: " + md.storesMixedCaseQuotedIdentifiers());
        log.warn("supportsMixedCaseQuotedIdentifiers: " + md.supportsMixedCaseQuotedIdentifiers());
        log.warn("getIdentifierQuoteString: " + md.getIdentifierQuoteString());

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

    @Override
    public RelationID getRelationCanonicalID(RelationID id) {  return id; }

    @Override
    public ImmutableList<RelationDefinition.AttributeListBuilder> getRelationAttributes(RelationID id) throws MetadataExtractionException {

        try (ResultSet rs = metadata.getColumns(getRelationCatalog(id), getRelationSchema(id), getRelationName(id), null)) {
            ImmutableList.Builder<RelationDefinition.AttributeListBuilder> relations = ImmutableList.builder();
            RelationDefinition.AttributeListBuilder currentRelation = null;

            while (rs.next()) {
                RelationID relationId = getRelationID(rs);
                if (currentRelation == null || !currentRelation.getRelationID().equals(relationId)) {
                    // switch to the next database relation
                    currentRelation = new RelationDefinition.AttributeListBuilder(relationId);
                    relations.add(currentRelation);
                }

                QuotedID attributeId = rawIdFactory.createAttributeID(rs.getString("COLUMN_NAME"));
                // columnNoNulls, columnNullable, columnNullableUnknown
                boolean isNullable = rs.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls;
                String typeName = rs.getString("TYPE_NAME");
                int columnSize = rs.getInt("COLUMN_SIZE");
                DBTermType termType = dbTypeFactory.getDBTermType(typeName, columnSize);

                currentRelation.addAttribute(attributeId, termType, typeName, isNullable);
            }
            return relations.build();
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    @Override
    public void insertIntegrityConstraints(BasicDBMetadataBuilder md) throws MetadataExtractionException {
        // TODO:
    }

    @Override
    public void insertIntegrityConstraints(RelationDefinition relation, ImmutableDBMetadata dbMetadata) throws MetadataExtractionException {
        DatabaseRelationDefinition r = (DatabaseRelationDefinition)relation;
        insertPrimaryKey(r);
        insertUniqueAttributes(r);
        insertForeignKeys(r, dbMetadata);
    }

    @Override
    public DBParameters getDBParameters() {
        return dbParameters;
    }

    private void insertPrimaryKey(DatabaseRelationDefinition relation) throws MetadataExtractionException {
        RelationID id = relation.getID();
        // Retrieves a description of the given table's primary key columns. They are ordered by COLUMN_NAME (sic!)
        try (ResultSet rs = metadata.getPrimaryKeys(getRelationCatalog(id), getRelationSchema(id), getRelationName(id))) {
            Map<Integer, String> primaryKeyAttributes = new HashMap<>();
            String currentName = null;
            while (rs.next()) {
                // TABLE_CAT is ignored for now; assume here that relation has a fully specified name
                RelationID id2 = getRelationID(rs);
                if (id2.equals(id)) {
                    currentName = rs.getString("PK_NAME"); // may be null
                    String attr = rs.getString("COLUMN_NAME");
                    int seq = rs.getShort("KEY_SEQ");
                    primaryKeyAttributes.put(seq, attr);
                }
            }
            if (!primaryKeyAttributes.isEmpty()) {
                // use the KEY_SEQ values to restore the correct order of attributes in the PK
                UniqueConstraint.BuilderImpl builder = UniqueConstraint.primaryKeyBuilder(relation, currentName);
                for (int i = 1; i <= primaryKeyAttributes.size(); i++) {
                    QuotedID attrId = rawIdFactory.createAttributeID(primaryKeyAttributes.get(i));
                    builder.addDeterminant(relation.getAttribute(attrId));
                }
                relation.addUniqueConstraint(builder.build());
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
            UniqueConstraint.BuilderImpl builder = null;
            while (rs.next()) {
                // TYPE: tableIndexStatistic - this identifies table statistics that are returned in conjunction with a table's index descriptions
                //       tableIndexClustered - this is a clustered index
                //       tableIndexHashed - this is a hashed index
                //       tableIndexOther (all are static final int in DatabaseMetaData)
                if (rs.getShort("TYPE") == DatabaseMetaData.tableIndexStatistic) {
                    if (builder != null)
                        relation.addUniqueConstraint(builder.build());

                    builder = null;
                    continue;
                }
                if (rs.getShort("ORDINAL_POSITION") == 1) {
                    if (builder != null)
                        relation.addUniqueConstraint(builder.build());

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
                    Attribute attr = relation.getAttribute(attrId);
                    if (attr == null) { // Compensate for the bug in PostgreSQL JBDC driver that
                        // strips off the quotation marks
                        attrId = rawIdFactory.createAttributeID("\"" + rs.getString("COLUMN_NAME") + "\"");
                        attr = relation.getAttribute(attrId);
                    }
                    builder.addDeterminant(attr);
                }
            }
            if (builder != null)
                relation.addUniqueConstraint(builder.build());
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    private void insertForeignKeys(DatabaseRelationDefinition relation, ImmutableDBMetadata dbMetadata) throws MetadataExtractionException {

        RelationID id = relation.getID();
        try (ResultSet rs = metadata.getImportedKeys(getRelationCatalog(id), getRelationSchema(id), getRelationName(id))) {
            ForeignKeyConstraint.Builder builder = null;
            String currentName = null;
            while (rs.next()) {
                RelationID refId = getPKRelationID(rs);
                DatabaseRelationDefinition ref = dbMetadata.getDatabaseRelation(refId);
                // FKTABLE_SCHEM and FKTABLE_NAME are ignored for now
                int seq = rs.getShort("KEY_SEQ");
                if (seq == 1) {
                    if (builder != null)
                        relation.addForeignKeyConstraint(builder.build(currentName));

                    currentName = rs.getString("FK_NAME"); // String => foreign key name (may be null)

                    if (ref != null) {
                        builder = new ForeignKeyConstraint.Builder(relation, ref);
                    }
                    else {
                        builder = null; // do not add this foreign key
                        // because there is no table it refers to
                        System.out.println("Cannot find table: " + refId + " for FK " + currentName);
                    }
                }
                if (builder != null) {
                    QuotedID attrId = rawIdFactory.createAttributeID(rs.getString("FKCOLUMN_NAME"));
                    QuotedID refAttrId = rawIdFactory.createAttributeID(rs.getString("PKCOLUMN_NAME"));
                    builder.add(relation.getAttribute(attrId), ref.getAttribute(refAttrId));
                }
            }
            if (builder != null)
                relation.addForeignKeyConstraint(builder.build(currentName));
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