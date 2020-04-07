package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLServerDBMetadataProvider extends DefaultDBMetadataProvider {

    private final ImmutableSet<String> ignoreSchema = ImmutableSet.of("sys", "INFORMATION_SCHEMA");
    private final QuotedID defaultSchema;

    SQLServerDBMetadataProvider(Connection connection, DBTypeFactory dbTypeFactory) throws MetadataExtractionException {
        super(connection, dbTypeFactory);
        this.defaultSchema = getDefaultSchema();
    }

    @Override
    public RelationID getRelationCanonicalID(RelationID id) {
        return id.extendWithDefaultSchemaID(defaultSchema);
    }

    @Override
    protected boolean isSchemaIgnored(String schema) {
        return ignoreSchema.contains(schema);
    }

    /*
		public String getQuery() {
			return "SELECT TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME " +
					"FROM INFORMATION_SCHEMA.TABLES " +
					"WHERE TABLE_TYPE='BASE TABLE' OR TABLE_TYPE='VIEW'";
		}
    */

    private final QuotedID getDefaultSchema() throws MetadataExtractionException {
        // default schema name
        // https://msdn.microsoft.com/en-us/library/ms175068.aspx
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SCHEMA_NAME() AS U")) {
            rs.next();
            return rawIdFactory.createRelationID(rs.getString("U"), "DUMMY").getSchemaID();
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }
}
