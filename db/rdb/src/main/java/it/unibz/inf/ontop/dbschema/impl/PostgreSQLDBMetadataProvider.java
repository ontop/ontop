package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgreSQLDBMetadataProvider extends DefaultDBMetadataProvider {

    private final QuotedID defaultSchema;

    PostgreSQLDBMetadataProvider(Connection connection, DBTypeFactory dbTypeFactory) throws MetadataExtractionException {
        super(connection, dbTypeFactory);
        this.defaultSchema = getDefaultSchema();
    }

    @Override
    public RelationID getRelationCanonicalID(RelationID id) {
        return id.extendWithDefaultSchemaID(defaultSchema);
    }

    private final QuotedID getDefaultSchema() throws MetadataExtractionException {
        // default schema name
        // https://www.postgresql.org/docs/9.3/functions-info.html
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT current_schema AS S")) {
            rs.next();
            return rawIdFactory.createRelationID(rs.getString("S"), "DUMMY").getSchemaID();
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

}
