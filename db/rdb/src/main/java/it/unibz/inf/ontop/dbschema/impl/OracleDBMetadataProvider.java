package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.*;

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
        return stmt.executeQuery("SELECT NULL AS TABLE_CAT, OWNER as TABLE_SCHEM, table_name as TABLE_NAME " +
                "FROM all_tables " +
                "WHERE " +
                "   NOT table_name LIKE 'MVIEW$_%' AND " +
                "   NOT table_name LIKE 'LOGMNR_%' AND " +
                "   NOT table_name LIKE 'AQ$_%' AND " +
                "   NOT table_name LIKE 'DEF$_%' AND " +
                "   NOT table_name LIKE 'REPCAT$_%' AND " +
                "   NOT table_name LIKE 'LOGSTDBY$%' AND " +
                "   NOT table_name LIKE 'OL$%' AND" +
                "   owner NOT IN ('SYS', " +
                        "'GSMADMIN_INTERNAL', " +
                        "'OUTLN', " +
                        "'DBSNMP', " +
                        "'DBSFWUSER', " +
                        "'XDB', " +
                        "'LBACSYS', " +
                        "'DVSYS', " +
                        "'APPQOSSYS', " +
                        "'AUDSYS') AND " +
                "   NOT (owner = 'SYSTEM' AND table_name IN ('ROLLING$DIRECTIVES', " +
                        "'SCHEDULER_JOB_ARGS_TBL', " +
                        "'REDO_DB', " +
                        "'REDO_LOG', " +
                        "'ROLLING$DATABASES', " +
                        "'ROLLING$EVENTS', " +
                        "'SCHEDULER_PROGRAM_ARGS', " +
                        "'REPL_SUPPORT_MATRIX', " +
                        "'ROLLING$PARAMETERS', " +
                        "'ROLLING$STATISTICS', " +
                        "'SCHEDULER_PROGRAM_ARGS_TBL', " +
                        "'PRODUCT_PRIVS', " +
                        "'SQLPLUS_PRODUCT_PROFILE', " +
                        "'REPL_VALID_COMPAT', " +
                        "'SCHEDULER_JOB_ARGS', " +
                        "'ROLLING$CONNECTIONS', " +
                        "'ROLLING$PLAN', " +
                        "'HELP', " +
                        "'ROLLING$STATUS' )) " +
                "UNION ALL " +
                "SELECT NULL AS TABLE_CAT, owner as TABLE_SCHEM, view_name as TABLE_NAME " +
                "FROM all_views " +
                "WHERE " +
                "   NOT view_name LIKE 'MVIEW_%' AND " +
                "   NOT view_name LIKE 'LOGMNR_%' AND " +
                "   NOT view_name LIKE 'AQ$_%' AND " +
                "   owner NOT IN ('SYS', " +
                                "'GSMADMIN_INTERNAL', " +
                                "'OUTLN', " +
                                "'DBSNMP', " +
                                "'DBSFWUSER', " +
                                "'XDB', " +
                                "'LBACSYS', " +
                                "'DVSYS', " +
                                "'APPQOSSYS', " +
                                "'AUDSYS') AND " +
                "   NOT (owner = 'SYSTEM' AND view_name IN ('SCHEDULER_PROGRAM_ARGS', " +
                                "'SCHEDULER_JOB_ARGS', " +
                                "'PRODUCT_PRIVS'))");
    }

    @Override
    protected boolean isPrimaryKeyDisabled(RelationID id, String primaryKeyId) {
        return isConstraintDisabled(id, primaryKeyId);
    }
    @Override
    protected boolean isUniqueConstraintDisabled(RelationID id, String uniqueConstraintId) {
        return isConstraintDisabled(id, uniqueConstraintId);
    }
    protected boolean isForeignKeyDisabled(RelationID id, String foreignKeyId) {
        return isConstraintDisabled(id, foreignKeyId);
    }


    private boolean isConstraintDisabled(RelationID id, String constraintId) {
        /*
            OWNER	VARCHAR2(30)	NOT NULL	Owner of the constraint definition
            CONSTRAINT_NAME	VARCHAR2(30)	NOT NULL	Name of the constraint definition
            CONSTRAINT_TYPE	VARCHAR2(1)	 	Type of constraint definition:
                C (check constraint on a table)
                P (primary key)
                U (unique key)
                R (referential integrity)
                V (with check option, on a view)
                O (with read only, on a view)
            TABLE_NAME	VARCHAR2(30)	NOT NULL	Name associated with the table (or view) with constraint definition
            STATUS	VARCHAR2(8)	 	Enforcement status of constraint (ENABLED or DISABLED)
     */
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT status\n" +
                        "FROM all_constraints\n" +
                        "WHERE constraint_name = :1\n" +
                        "  AND table_name = :2\n" +
                        "  AND owner = :3")) {
            stmt.setString(1, constraintId);
            stmt.setString(2, getRelationName(id));
            stmt.setString(3, getRelationSchema(id));
            stmt.closeOnCompletion();
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                return "DISABLED".equals(status);
            }
            throw new MinorOntopInternalBugException("Constraint " + constraintId + " in " + id + " not found");
        }
        catch (SQLException e) {
            throw new MinorOntopInternalBugException("Error retrieving constraint " + constraintId + " in " + id + " info: " + e.getMessage());
        }
    }
}

