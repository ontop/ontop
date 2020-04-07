package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.dbschema.impl.RelationIDImpl;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLDBMetadataProvider extends DefaultDBMetadataProvider {

    private final String defaultDatabase;

    MySQLDBMetadataProvider(Connection connection, DBTypeFactory dbTypeFactory) throws MetadataExtractionException {
        super(connection, getIDFactory(connection), dbTypeFactory);

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT DATABASE()")) {
            defaultDatabase = (rs.next()) ? rs.getString(1) : null;
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    private static QuotedIDFactory getIDFactory(Connection connection) throws MetadataExtractionException {
        try {
            return new MySQLQuotedIDFactory(connection.getMetaData().storesMixedCaseIdentifiers());
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    @Override
    public RelationID getRelationCanonicalID(RelationID id) {
        return (id.hasSchema())
                ? id
                : idFactory.createRelationID(defaultDatabase, id.getTableNameSQLRendering());
    }

    // WORKAROUND for MySQL connector >= 8.0:
    // <https://github.com/ontop/ontop/issues/270>

    @Override
    protected String getRelationCatalog(RelationID relationID) { return relationID.getSchemaName(); }

    @Override
    protected RelationID getRelationID(ResultSet rs) throws SQLException {
        return RelationIDImpl.createRelationIdFromDatabaseRecord(idFactory,
                rs.getString("TABLE_CAT"),
                rs.getString("TABLE_NAME"));
    }

    protected RelationID getPKRelationID(ResultSet rs) throws SQLException {
        return RelationIDImpl.createRelationIdFromDatabaseRecord(idFactory,
                rs.getString("PKTABLE_CAT"),
                rs.getString("PKTABLE_NAME"));
    }

}
