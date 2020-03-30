package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class JDBCRDBMetadataLoader implements RDBMetadataLoader {

    protected final Connection connection;
    protected final QuotedIDFactory idFactory;

    JDBCRDBMetadataLoader(Connection connection, QuotedIDFactory idFactory) {
        this.connection = connection;
        this.idFactory = idFactory;
    }

    @Override
    public ImmutableList<RelationID> getRelationIDs() throws SQLException {
        DatabaseMetaData md = connection.getMetaData();
        ImmutableList.Builder<RelationID> builder = ImmutableList.builder();
        try (ResultSet rs = md.getTables(null, null, null, new String[] { "TABLE", "VIEW" })) {
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
    public RelationDefinition.AttributeListBuilder getRelationAttributes(RelationID relationID) {
        return null;
    }

    @Override
    public void insertIntegrityConstraints(RelationDefinition relation) {

    }

    protected boolean ignoreSchema(String schema) {
        return false;
    }

}