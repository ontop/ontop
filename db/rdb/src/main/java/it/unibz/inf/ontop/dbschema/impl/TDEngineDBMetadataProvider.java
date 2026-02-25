package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TDEngineDBMetadataProvider extends DefaultSchemaDBMetadataProvider {

    @AssistedInject
    TDEngineDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection,
                metadata -> metadata.storesMixedCaseIdentifiers()
                    ? new TDEngineCaseSensitiveTableNamesQuotedIDFactory()
                    : new TDEngineCaseNotSensitiveTableNamesQuotedIDFactory(),
                coreSingletons,
                c -> getTDEngineDefaultSchema(c));
    }

    private static String[] getTDEngineDefaultSchema(Connection connection) throws SQLException {
        // For TDEngine, we need to extract the database name from the connection URL or use a default
        String url = connection.getMetaData().getURL();
        String database = extractDatabaseFromUrl(url);
        
        if (database == null || database.isEmpty()) {
            // Try to get current database using TDEngine specific query
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DATABASE()")) {
                if (rs.next()) {
                    database = rs.getString(1);
                }
            } catch (SQLException e) {
                // If that fails, use a default database name
                database = "test";
            }
        }
        
        return new String[] { database, "DUMMY" };
    }

    private static String extractDatabaseFromUrl(String url) {
        if (url == null) return null;
        
        // TDEngine JDBC URL format: jdbc:TAOS://host:port/database_name?param=value
        // or jdbc:TAOS-RS://host:port/database_name?param=value  
        // or jdbc:TAOS-WS://host:port/database_name?param=value
        try {
            if (url.startsWith("jdbc:TAOS")) {
                int hostPortEnd = url.indexOf('/', url.indexOf("://") + 3);
                if (hostPortEnd > 0) {
                    int queryStart = url.indexOf('?', hostPortEnd);
                    String database = queryStart > 0 
                        ? url.substring(hostPortEnd + 1, queryStart)
                        : url.substring(hostPortEnd + 1);
                    return database.isEmpty() ? null : database;
                }
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return null;
    }

    // Similar to MySQL provider structure for TDEngine specifics
    @Override
    protected String getRelationCatalog(RelationID relationID) { 
        return super.getRelationSchema(relationID); 
    }

    @Override
    protected String getRelationSchema(RelationID relationID) { 
        return null; 
    }

    @Override
    protected RelationID getRelationID(ResultSet rs, String catalogNameColumn, String schemaNameColumn, String tableNameColumn) throws SQLException {
        return rawIdFactory.createRelationID(rs.getString(catalogNameColumn), rs.getString(tableNameColumn));
    }

    // Case sensitive table names factory for TDEngine
    private static class TDEngineCaseSensitiveTableNamesQuotedIDFactory extends MySQLAbstractQuotedIDFactory {
        @Override
        public QuotedID createAttributeID(String s) {
            return createFromString(s, false);
        }

        @Override
        protected QuotedID createFromString(String s) {
            return createFromString(s, true);
        }
    }

    // Case insensitive table names factory for TDEngine  
    private static class TDEngineCaseNotSensitiveTableNamesQuotedIDFactory extends MySQLAbstractQuotedIDFactory {
        @Override
        public QuotedID createAttributeID(String s) {
            return createFromString(s, false);
        }

        @Override
        protected QuotedID createFromString(String s) {
            return createFromString(s, false);
        }
    }
}
