package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.model.type.DBTypeFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLDBMetadataLoader extends JDBCRDBMetadataLoader {

    private final String catalog;

    MySQLDBMetadataLoader(Connection connection, QuotedIDFactory idFactory, DBTypeFactory dbTypeFactory) throws SQLException {
        super(connection, idFactory, dbTypeFactory);

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT DATABASE()")) {
            catalog = (rs.next()) ? rs.getString(1) : null;
        }
    }

    @Override
    protected String getRelationCatalog(RelationID relationID) { return catalog; }

    @Override
    protected RelationID getRelationID(ResultSet rs) throws SQLException {
        return RelationID.createRelationIdFromDatabaseRecord(idFactory,
                rs.getString("TABLE_CAT"),
                rs.getString("TABLE_NAME"));
    }
}
