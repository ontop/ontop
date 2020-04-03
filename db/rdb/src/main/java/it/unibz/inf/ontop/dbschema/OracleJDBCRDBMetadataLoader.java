package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OracleJDBCRDBMetadataLoader extends JDBCRDBMetadataLoader {

    private final String defaultTableOwner;

    OracleJDBCRDBMetadataLoader(Connection connection, QuotedIDFactory idFactory, DBTypeFactory dbTypeFactory) throws MetadataExtractionException {
        super(connection, idFactory, dbTypeFactory);
        this.defaultTableOwner = getDefaultOwner();
    }

    @Override
    public RelationID getRelationCanonicalID(RelationID id) {
        // DUAL is a special Oracle table
        return (id.hasSchema() || id.getTableName().equals("DUAL"))
            ? id
            : idFactory.createRelationID(defaultTableOwner, id.getTableNameSQLRendering());
    }

    @Override
    public ImmutableList<RelationID> getRelationIDs() throws MetadataExtractionException {
        // Obtain the relational objects (i.e., tables and views)
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(TABLE_LIST_QUERY)) {
            ImmutableList.Builder<RelationID> relationIds = ImmutableList.builder();
            while (rs.next()) {
                RelationID id = RelationID.createRelationIdFromDatabaseRecord(idFactory,
                        defaultTableOwner,
                        rs.getString("object_name"));
                relationIds.add(id);
            }
            return relationIds.build();
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    // filter out all irrelevant table and view names
    private static final String TABLE_LIST_QUERY =
        "SELECT table_name as object_name FROM user_tables WHERE " +
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

    private final String getDefaultOwner() throws MetadataExtractionException {
        // Obtain the table owner (i.e., schema name)
        try (Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT user FROM dual")) {
            return (resultSet.next())
                    ? resultSet.getString("user").toUpperCase()
                    : "SYSTEM"; // default value
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }
}
