package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class JDBCRDBMetadataLoader implements RDBMetadataLoader {

    protected final Connection connection;
    protected final DatabaseMetaData metadata;
    protected final QuotedIDFactory idFactory;
    protected final DBTypeFactory dbTypeFactory;

    JDBCRDBMetadataLoader(Connection connection, QuotedIDFactory idFactory, DBTypeFactory dbTypeFactory) throws SQLException {
        this.connection = connection;
        this.metadata = connection.getMetaData();
        this.idFactory = idFactory;
        this.dbTypeFactory = dbTypeFactory;
    }

    @Override
    public ImmutableList<RelationID> getRelationIDs() throws SQLException {
        ImmutableList.Builder<RelationID> builder = ImmutableList.builder();
        try (ResultSet rs = metadata.getTables(null, null, null, new String[] { "TABLE", "VIEW" })) {
            while (rs.next()) {
                // String catalog = rs.getString("TABLE_CAT"); // not used
                String schema = rs.getString("TABLE_SCHEM");
                String table = rs.getString("TABLE_NAME");
                if (!ignoreSchema(schema)) {
                    RelationID id = RelationID.createRelationIdFromDatabaseRecord(idFactory, schema, table);
                    builder.add(id);
                }
            }
        }
        return builder.build();
    }

    @Override
    public ImmutableList<RelationID> getRelationIDs(ImmutableList<RelationID> tables) {
        return tables;
    }

    @Override
    public ImmutableList<RelationDefinition.AttributeListBuilder> getRelationAttributes(RelationID id) throws SQLException {

        ImmutableList.Builder<RelationDefinition.AttributeListBuilder> relations = ImmutableList.builder();
        RelationDefinition.AttributeListBuilder currentRelation = null;

        try (ResultSet rs = metadata.getColumns(getRelationCatalog(id), getRelationSchema(id), getRelationName(id), null)) {
            while (rs.next()) {
                RelationID relationId = getRelationID(rs);
                if (currentRelation == null || !currentRelation.getRelationID().equals(relationId)) {
                    // switch to the next database relation
                    currentRelation = new RelationDefinition.AttributeListBuilder(relationId);
                    relations.add(currentRelation);
                }

                QuotedID attributeId = QuotedID.createIdFromDatabaseRecord(idFactory, rs.getString("COLUMN_NAME"));
                // columnNoNulls, columnNullable, columnNullableUnknown
                boolean isNullable = rs.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls;
                String typeName = rs.getString("TYPE_NAME");
                int columnSize = rs.getInt("COLUMN_SIZE");
                DBTermType termType = dbTypeFactory.getDBTermType(typeName, columnSize);

                currentRelation.addAttribute(attributeId, termType, typeName, isNullable);
            }
        }
        return relations.build();
    }

    @Override
    public void insertIntegrityConstraints(RelationDefinition relation, DBMetadata dbMetadata) throws SQLException {
        DatabaseRelationDefinition r = (DatabaseRelationDefinition)relation;
        insertPrimaryKey(r);
        insertUniqueAttributes(r);
        insertForeignKeys(r, dbMetadata);
    }

    private void insertPrimaryKey(DatabaseRelationDefinition relation) throws SQLException {
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
                    QuotedID attrId = QuotedID.createIdFromDatabaseRecord(idFactory, primaryKeyAttributes.get(i));
                    builder.addDeterminant(relation.getAttribute(attrId));
                }
                relation.addUniqueConstraint(builder.build());
            }
        }
    }

    private void insertUniqueAttributes(DatabaseRelationDefinition relation) throws SQLException {
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
                    QuotedID attrId = QuotedID.createIdFromDatabaseRecord(idFactory, rs.getString("COLUMN_NAME"));
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
                        attrId = QuotedID.createIdFromDatabaseRecord(idFactory, "\"" + rs.getString("COLUMN_NAME") + "\"");
                        attr = relation.getAttribute(attrId);
                    }
                    builder.addDeterminant(attr);
                }
            }
            if (builder != null)
                relation.addUniqueConstraint(builder.build());
        }
    }

    private void insertForeignKeys(DatabaseRelationDefinition relation, DBMetadata dbMetadata) throws SQLException {

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
                    QuotedID attrId = QuotedID.createIdFromDatabaseRecord(idFactory, rs.getString("FKCOLUMN_NAME"));
                    QuotedID refAttrId = QuotedID.createIdFromDatabaseRecord(idFactory, rs.getString("PKCOLUMN_NAME"));
                    builder.add(relation.getAttribute(attrId), ref.getAttribute(refAttrId));
                }
            }
            if (builder != null)
                relation.addForeignKeyConstraint(builder.build(currentName));
        }
    }





    protected boolean ignoreSchema(String schema) {
        return false;
    }

    // catalog is ignored for now (rs.getString("TABLE_CAT"))
    protected String getRelationCatalog(RelationID relationID) { return null; }

    protected String getRelationSchema(RelationID relationID) { return relationID.getSchemaName(); }

    protected String getRelationName(RelationID relationID) { return relationID.getTableName(); }

    protected RelationID getRelationID(ResultSet rs) throws SQLException {
        return RelationID.createRelationIdFromDatabaseRecord(idFactory,
                rs.getString("TABLE_SCHEM"),
                rs.getString("TABLE_NAME"));
    }

    protected RelationID getPKRelationID(ResultSet rs) throws SQLException {
        return RelationID.createRelationIdFromDatabaseRecord(idFactory,
                rs.getString("PKTABLE_SCHEM"),
                rs.getString("PKTABLE_NAME"));
    }

}