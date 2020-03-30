package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OracleJDBCRDBMetadataLoader extends JDBCRDBMetadataLoader {

    private final String defaultTableOwner;

    OracleJDBCRDBMetadataLoader(Connection connection, QuotedIDFactory idFactory) throws SQLException {
        super(connection, idFactory);
        this.defaultTableOwner = getDefaultOwner();
    }

    @Override
    public ImmutableList<RelationID> getRelationIDs(ImmutableList<RelationID> tables) {
        ImmutableList.Builder<RelationID> builder = ImmutableList.builder();
        for (RelationID table : tables) {
            // DUAL is a special Oracle table
            if (table.hasSchema() || table.getTableName().equals("DUAL"))
                builder.add(table);
            else {
                RelationID qualifiedTableId = idFactory.createRelationID(defaultTableOwner, table.getTableNameSQLRendering());
                builder.add(qualifiedTableId);
            }
        }
        return builder.build();
    }


    @Override
    public ImmutableList<RelationID> getRelationIDs() throws SQLException {
        // Obtain the relational objects (i.e., tables and views)
        ImmutableList.Builder<RelationID> relationIds = ImmutableList.builder();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(getTableListQuery())) {
            while (rs.next())
                relationIds.add(getTableID(rs));
        }
        return relationIds.build();
    }


    protected String getTableListQuery() {
        // filter out all irrelevant table and view names
        return "SELECT table_name as object_name FROM user_tables WHERE " +
                "   NOT table_name LIKE 'MVIEW$_%' AND " +
                "   NOT table_name LIKE 'LOGMNR_%' AND " +
                "   NOT table_name LIKE 'AQ$_%' AND " +
                "   NOT table_name LIKE 'DEF$_%' AND " +
                "   NOT table_name LIKE 'REPCAT$_%' AND " +
                "   NOT table_name LIKE 'LOGSTDBY$%' AND " +
                "   NOT table_name LIKE 'OL$%' " +
                "UNION ALL " +
                "SELECT view_name as object_name FROM user_views WHERE " +
                "   NOT view_name LIKE 'MVIEW_%' AND " +
                "   NOT view_name LIKE 'LOGMNR_%' AND " +
                "   NOT view_name LIKE 'AQ$_%'";
    }


    private final String getDefaultOwner() throws SQLException {
        // Obtain the table owner (i.e., schema name)
        String loggedUser = "SYSTEM"; // default value
        try (Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT user FROM dual")) {
            if (resultSet.next()) {
                loggedUser = resultSet.getString("user");
            }
        }
        return loggedUser.toUpperCase();
    }

    protected RelationID getTableID(ResultSet rs) throws SQLException {
        return RelationID.createRelationIdFromDatabaseRecord(idFactory, defaultTableOwner, rs.getString("object_name"));
    }
}
