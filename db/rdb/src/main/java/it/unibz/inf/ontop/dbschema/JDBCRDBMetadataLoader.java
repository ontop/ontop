package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

import java.sql.*;

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
                QuotedID attributeId = QuotedID.createIdFromDatabaseRecord(idFactory, rs.getString("COLUMN_NAME"));

                if (currentRelation == null || !currentRelation.getRelationID().equals(relationId)) {
                    // switch to the next database relation
                    currentRelation = new RelationDefinition.AttributeListBuilder(relationId);
                    relations.add(currentRelation);
                }

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
    public void insertIntegrityConstraints(RelationDefinition relation) {

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

}