package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLDBMetadataProvider extends DefaultDBMetadataProvider {

    private final QuotedID defaultDatabase;

    MySQLDBMetadataProvider(Connection connection, DBTypeFactory dbTypeFactory) throws MetadataExtractionException {
        super(connection, getIDFactory(connection), dbTypeFactory);
        defaultDatabase = retrieveDefaultSchema("SELECT DATABASE()");
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
    protected QuotedID getDefaultSchema() {
        return defaultDatabase;
    }


    // WORKAROUND for MySQL connector >= 8.0:
    // <https://github.com/ontop/ontop/issues/270>

    @Override
    protected String getRelationCatalog(RelationID relationID) { return getEffectiveRelationSchema(relationID); }

    @Override
    protected String getRelationSchema(RelationID relationID) { return null; }

    @Override
    protected RelationID getRelationID(ResultSet rs) throws SQLException {
        return rawIdFactory.createRelationID(
                rs.getString("TABLE_CAT"), rs.getString("TABLE_NAME"));
    }

    protected RelationID getPKRelationID(ResultSet rs) throws SQLException {
        return rawIdFactory.createRelationID(
                rs.getString("PKTABLE_CAT"), rs.getString("PKTABLE_NAME"));
    }

    protected RelationID getFKRelationID(ResultSet rs) throws SQLException {
        return rawIdFactory.createRelationID(
                rs.getString("FKTABLE_CAT"), rs.getString("FKTABLE_NAME"));
    }

}
