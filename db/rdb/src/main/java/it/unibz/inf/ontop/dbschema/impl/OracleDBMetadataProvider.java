package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static it.unibz.inf.ontop.dbschema.RelationID.TABLE_INDEX;

public class OracleDBMetadataProvider extends DefaultSchemaDBMetadataProvider {

    private final RelationID sysDualId;

    @AssistedInject
    protected OracleDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, metadata -> new SQLStandardQuotedIDFactory(), coreSingletons);
        //        "SELECT user as TABLE_SCHEM FROM dual");
        // https://docs.oracle.com/cd/B19306_01/server.102/b14200/functions207.htm#i79833
        // https://docs.oracle.com/cd/B19306_01/server.102/b14200/queries009.htm
        this.sysDualId = rawIdFactory.createRelationID("DUAL");
    }

    private boolean isDual(RelationID id) {
        return id.getComponents().get(TABLE_INDEX).equals(sysDualId.getComponents().get(TABLE_INDEX));
    }

    @Override
    protected RelationID getCanonicalRelationId(RelationID relationID) {
        if (isDual(relationID))
            return sysDualId;

        return super.getCanonicalRelationId(relationID);
    }

    @Override
    protected void checkSameRelationID(RelationID extractedId, RelationID givenId) throws MetadataExtractionException {
        // DUAL is retrieved as SYS.DUAL, but its canonical name is DUAL
        if (isDual(extractedId) && isDual(givenId))
            return;

        super.checkSameRelationID(extractedId, givenId);
    }

    @Override
    protected String makeQueryMinimizeResultSet(String query) {
        return String.format("SELECT * FROM (%s) subQ FETCH NEXT 1 ROWS ONLY", query);
    }

    @Override
    protected ImmutableList<RelationID> getAllIDs(RelationID id) {
        if (isDual(id))
            return ImmutableList.of(sysDualId);

        return super.getAllIDs(id);
    }

    @Override
    protected String getRelationSchema(RelationID id) { return id.getComponents().size() > SCHEMA_INDEX ? id.getComponents().get(SCHEMA_INDEX).getName() : null; }

    @Override
    protected ResultSet getRelationIDsResultSet() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.closeOnCompletion();
        // Obtain the relational objects (i.e., tables and views)
        // filter out all irrelevant table and view names
        return stmt.executeQuery("SELECT NULL AS TABLE_CAT, user as TABLE_SCHEM, table_name as TABLE_NAME " +
                "FROM user_tables " +
                "WHERE " +
                "   NOT table_name LIKE 'MVIEW$_%' AND " +
                "   NOT table_name LIKE 'LOGMNR_%' AND " +
                "   NOT table_name LIKE 'AQ$_%' AND " +
                "   NOT table_name LIKE 'DEF$_%' AND " +
                "   NOT table_name LIKE 'REPCAT$_%' AND " +
                "   NOT table_name LIKE 'LOGSTDBY$%' AND " +
                "   NOT table_name LIKE 'OL$%' " +
                "UNION ALL " +
                "SELECT NULL AS TABLE_CAT, user as TABLE_SCHEM, view_name as TABLE_NAME " +
                "FROM user_views " +
                "WHERE " +
                "   NOT view_name LIKE 'MVIEW_%' AND " +
                "   NOT view_name LIKE 'LOGMNR_%' AND " +
                "   NOT view_name LIKE 'AQ$_%'");
    }
}
