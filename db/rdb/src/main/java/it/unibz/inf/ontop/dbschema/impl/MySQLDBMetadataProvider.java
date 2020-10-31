package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLDBMetadataProvider extends DefaultDBMetadataProvider {

    @AssistedInject
    MySQLDBMetadataProvider(@Assisted Connection connection, TypeFactory typeFactory) throws MetadataExtractionException {
        super(connection,
                metadata -> new MySQLQuotedIDFactory(metadata.storesMixedCaseIdentifiers()),
                new QueryBasedDefaultSchemaProvider(connection,
                        "SELECT SELECT DATABASE() AS TABLE_CAT, NULLit.unibz.inf.ontop.owlapi.CanonicalIRIUniversityTest AS TABLE_SCHEM"),
                typeFactory);
        // https://dev.mysql.com/doc/refman/5.7/en/information-functions.html#function_schema
    }


    // WORKAROUND for MySQL connector >= 8.0:
    // <https://github.com/ontop/ontop/issues/270>

    @Override
    protected String getRelationCatalog(RelationID relationID) { return super.getRelationSchema(relationID); }

    @Override
    protected String getRelationSchema(RelationID relationID) { return null; }

    @Override
    protected RelationID getRelationID(ResultSet rs) throws SQLException {
        return getRelationID(rs, "TABLE_SCHEM",  "TABLE_CAT", "TABLE_NAME");
    }

    @Override
    protected RelationID getPKRelationID(ResultSet rs) throws SQLException {
        return getRelationID(rs, "PKTABLE_SCHEM", "PKTABLE_CAT", "PKTABLE_NAME");
    }

    @Override
    protected RelationID getFKRelationID(ResultSet rs) throws SQLException {
        return getRelationID(rs, "FKTABLE_SCHEM", "FKTABLE_CAT", "FKTABLE_NAME");
    }
}
