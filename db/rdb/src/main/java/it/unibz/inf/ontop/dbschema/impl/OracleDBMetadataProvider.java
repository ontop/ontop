package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;

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
    protected void checkSameRelationID(RelationID extractedId, RelationID givenId, String method) throws MetadataExtractionException {
        // DUAL is retrieved as SYS.DUAL, but its canonical name is DUAL
        if (isDual(extractedId) && isDual(givenId))
            return;

        super.checkSameRelationID(extractedId, givenId, method);
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

    private static final ImmutableSet<String> IGNORED_TABLE_SCHEMAS = ImmutableSet.of("SYS",
            "GSMADMIN_INTERNAL",
            "OUTLN",
            "DBSNMP",
            "DBSFWUSER",
            "XDB",
            "CTXSYS",
            "MDSYS",
            "APPQOSSYS",
            "LBACSYS",
            "DVSYS",
            "APPQOSSYS",
            "WMSYS",
            "ORDDATA",
            "AUDSYS");

    private static final ImmutableSet<String> IGNORED_TABLE_PREFIXES = ImmutableSet.of("MVIEW$_",
            "LOGMNR_",
            "AQ$_",
            "DEF$_",
            "REPCAT$_",
            "LOGSTDBY$",
            "OL$");

    private static final ImmutableSet<String> IGNORED_SYSTEM_TABLES = ImmutableSet.of("ROLLING$DIRECTIVES",
            "SCHEDULER_JOB_ARGS_TBL",
            "REDO_DB",
            "REDO_LOG",
            "ROLLING$DATABASES",
            "ROLLING$EVENTS",
            "SCHEDULER_PROGRAM_ARGS",
            "REPL_SUPPORT_MATRIX",
            "ROLLING$PARAMETERS",
            "ROLLING$STATISTICS",
            "SCHEDULER_PROGRAM_ARGS_TBL",
            "PRODUCT_PRIVS",
            "SQLPLUS_PRODUCT_PROFILE",
            "REPL_VALID_COMPAT",
            "SCHEDULER_JOB_ARGS",
            "ROLLING$CONNECTIONS",
            "ROLLING$PLAN",
            "HELP",
            "ROLLING$STATUS");

    private static final ImmutableSet<String> IGNORED_VIEW_PREFIXES = ImmutableSet.of("MVIEW_",
            "LOGMNR_" +
            "AQ$_");

    private static final ImmutableSet<String> IGNORED_VIEW_SCHEMAS = ImmutableSet.of("SYS",
            "GSMADMIN_INTERNAL",
            "OUTLN",
            "DBSNMP",
            "DBSFWUSER",
            "XDB",
            "WMSYS",
            "CTXSYS",
            "ORDDATA",
            "ORDSYS",
            "OLAPSYS",
            "MDSYS",
            "LBACSYS",
            "DVSYS",
            "APPQOSSYS",
            "AUDSYS");

    private static ImmutableSet<String> IGNORED_SYSTEM_VIEWS = ImmutableSet.of("SCHEDULER_PROGRAM_ARGS",
            "SCHEDULER_JOB_ARGS",
            "PRODUCT_PRIVS");

    @Override
    protected boolean isRelationExcluded(RelationID id) {
        /*
        We lose the information of whether a relation is a table or a view that was accessible
        while checking for these conditions in the original query. Therefore, we exclude ALL
        relations names that correspond to either system tables OR system views.
         */
        String schema = getRelationSchema(id);
        String table = getRelationName(id);
        return IGNORED_VIEW_SCHEMAS.contains(schema)
                || IGNORED_TABLE_SCHEMAS.contains(schema)
                || (schema.equals("SYSTEM") && IGNORED_SYSTEM_VIEWS.contains(table))
                || (schema.equals("SYSTEM") && IGNORED_SYSTEM_TABLES.contains(table))
                || IGNORED_VIEW_PREFIXES.stream()
                    .anyMatch(table::startsWith)
                || IGNORED_TABLE_PREFIXES.stream()
                    .anyMatch(table::startsWith);
    }

    @Override
    protected ResultSet getRelationIDsResultSet() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.closeOnCompletion();
        // Obtain the relational objects (i.e., tables and views)
        return stmt.executeQuery("SELECT NULL AS TABLE_CAT, OWNER as TABLE_SCHEM, table_name as TABLE_NAME " +
                "FROM all_tables " +
                "UNION ALL " +
                "SELECT NULL AS TABLE_CAT, owner as TABLE_SCHEM, view_name as TABLE_NAME " +
                "FROM all_views");
    }

    @Override
    protected boolean isPrimaryKeyDisabled(RelationID id, String primaryKeyId) {
        return isConstraintDisabled(id, primaryKeyId);
    }
    @Override
    protected boolean isUniqueConstraintDisabled(RelationID id, String uniqueConstraintId) {
        return isUniqueIndexDisabled(id, uniqueConstraintId);
    }
    @Override
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

    private boolean isUniqueIndexDisabled(RelationID id, String indexId) {
        /*
            OWNER VARCHAR2(128) NOT NULL Owner of the index
            INDEX_NAME VARCHAR2(128) NOT NULL Name of the index
            TABLE_NAME VARCHAR2(128) NOT NULL Name of the indexed object
            UNIQUENESS VARCHAR2(9) Indicates whether the index is unique (UNIQUE) or nonunique (NONUNIQUE)
            STATUS VARCHAR2(8) Indicates whether a non-partitioned index is VALID or UNUSABLE
     */
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT status\n" +
                        "FROM all_indexes\n" +
                        "WHERE index_name = :1\n" +
                        "  AND table_name = :2\n" +
                        "  AND owner = :3")) {
            stmt.setString(1, indexId);
            stmt.setString(2, getRelationName(id));
            stmt.setString(3, getRelationSchema(id));
            stmt.closeOnCompletion();
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                return !"VALID".equals(status);
            }
            throw new MinorOntopInternalBugException("Unique index " + indexId + " in " + id + " not found");
        }
        catch (SQLException e) {
            throw new MinorOntopInternalBugException("Error retrieving unique index " + indexId + " in " + id + " info: " + e.getMessage());
        }
    }
}

