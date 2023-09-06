package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AthenaDBMetadataProvider extends TrinoDBMetadataProvider {

    @AssistedInject
    AthenaDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, coreSingletons);
    }

    /**
     * System tables are only listed when querying information_schema
     * @return
     * @throws SQLException
     */
    @Override
    protected ResultSet getRelationIDsResultSet() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.closeOnCompletion();
        return stmt.executeQuery("SELECT TABLE_CATALOG AS TABLE_CAT, TABLE_SCHEMA AS TABLE_SCHEM, TABLE_NAME " +
                "FROM INFORMATION_SCHEMA.TABLES");
    }

    @Override
    protected boolean isRelationExcluded(RelationID id) {
        return getRelationSchema(id).equals("information_schema");
    }
}
