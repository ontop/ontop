package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OracleDBMetadataProvider extends DefaultDBMetadataProvider {

    private final RelationID sysDualId;

    @AssistedInject
    protected OracleDBMetadataProvider(@Assisted Connection connection, TypeFactory typeFactory) throws MetadataExtractionException {
        super(connection, "SELECT user FROM dual", typeFactory);
        // https://docs.oracle.com/cd/B19306_01/server.102/b14200/functions207.htm#i79833
        // https://docs.oracle.com/cd/B19306_01/server.102/b14200/queries009.htm
        this.sysDualId = rawIdFactory.createRelationID(null, "DUAL");
    }

    private boolean isDual(RelationID id) { return id.getTableID().equals(sysDualId.getTableID()); }

    @Override
    protected QuotedID getEffectiveRelationSchema(RelationID relationID) {
        if (isDual(relationID))
            return sysDualId.getSchemaID();

        return super.getEffectiveRelationSchema(relationID);
    }

    @Override
    protected void checkSameRelationID(RelationID extractedId, RelationID givenId) throws MetadataExtractionException {
        if (isDual(extractedId) && isDual(givenId))
            return;

        super.checkSameRelationID(extractedId, givenId);
    }

    @Override
    protected boolean isInDefaultSchema(RelationID id) {
        return isDual(id) || super.isInDefaultSchema(id);
    }


    @Override
    public ImmutableList<RelationID> getRelationIDs() throws MetadataExtractionException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(TABLE_LIST_QUERY)) {
            ImmutableList.Builder<RelationID> relationIds = ImmutableList.builder();
            while (rs.next()) {
                RelationID id = rawIdFactory.createRelationID(
                        rs.getString("schema_name"), rs.getString("object_name"));
                relationIds.add(id);
            }
            return relationIds.build();
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    // Obtain the relational objects (i.e., tables and views)
    // filter out all irrelevant table and view names
    private static final String TABLE_LIST_QUERY =
        "SELECT user as schema_name, table_name as object_name FROM user_tables WHERE " +
                "   NOT table_name LIKE 'MVIEW$_%' AND " +
                "   NOT table_name LIKE 'LOGMNR_%' AND " +
                "   NOT table_name LIKE 'AQ$_%' AND " +
                "   NOT table_name LIKE 'DEF$_%' AND " +
                "   NOT table_name LIKE 'REPCAT$_%' AND " +
                "   NOT table_name LIKE 'LOGSTDBY$%' AND " +
                "   NOT table_name LIKE 'OL$%' " +
                "UNION ALL " +
                "SELECT user as schema_name, view_name as object_name FROM user_views WHERE " +
                "   NOT view_name LIKE 'MVIEW_%' AND " +
                "   NOT view_name LIKE 'LOGMNR_%' AND " +
                "   NOT view_name LIKE 'AQ$_%'";
}
