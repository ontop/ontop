package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.dbschema.RelationID.TABLE_INDEX;

public class OracleDBMetadataProvider extends DefaultSchemaDBMetadataProvider {

    private final RelationID sysDualId;
    private final boolean mapDateToTimestamp;
    private final boolean j2ee13Compliant;
    private final short versionNumber;
    private final boolean includeSynonyms;

    @AssistedInject
    protected OracleDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, metadata -> new SQLStandardQuotedIDFactory(), coreSingletons);
        //        "SELECT user as TABLE_SCHEM FROM dual");
        // https://docs.oracle.com/cd/B19306_01/server.102/b14200/functions207.htm#i79833
        // https://docs.oracle.com/cd/B19306_01/server.102/b14200/queries009.htm
        this.sysDualId = rawIdFactory.createRelationID("DUAL");

        // see https://docs.oracle.com/en/database/oracle/oracle-database/23/jajdb/oracle/jdbc/OracleConnection.html
        this.versionNumber = getProperty(connection, "getVersionNumber", null, null, (short)12000);
        this.mapDateToTimestamp = getProperty(connection, "getMapDateToTimestamp", "oracle.jdbc.mapDateToTimestamp", Boolean::parseBoolean, true);
        this.j2ee13Compliant = getProperty(connection, "getJ2EE13Compliant", "oracle.jdbc.J2EE13Compliant", Boolean::parseBoolean, true);
        this.includeSynonyms = getProperty(connection, "getIncludeSynonyms", "includeSynonyms", Boolean::parseBoolean, false);

        LOGGER.debug("[DB-METADATA] Oracle version {} with mapDateToTimestamp {}, j2ee13Compliant {} and includeSynonyms {}",
                versionNumber, mapDateToTimestamp, j2ee13Compliant, includeSynonyms);
    }

    private static <T> T getProperty(Connection connection, String name, String property, Function<String, T> parser, T defValue) {
        try {
            Method m = connection.getClass().getMethod(name);
            m.setAccessible(true);
            return (T)m.invoke(connection);
        }
        catch (Exception e) {
            if (property != null) {
                try {
                    Method pm = connection.getClass().getMethod("getProperties");
                    pm.setAccessible(true);
                    Properties props = (Properties) pm.invoke(connection);
                    String v = props.getProperty(property);
                    if (v != null)
                        return parser.apply(v);
                }
                catch (Exception ignored) {
                }
            }
        }
        return defValue;
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

    private static final int PREFETCH_SIZE = 4048;

    @Override
    protected ResultSet getColumnsResultSet(RelationID id) throws SQLException {
        if (isDual(id))
            return super.getColumnsResultSet(id);

        try {
            String query = getColumnsSql();
            PreparedStatement stmt = connection.prepareStatement(query);
            String schema = getRelationSchema(id);
            stmt.setString(1, schema);
            String table = getRelationName(id);
            stmt.setString(2, table);
            if (includeSynonyms) {
                stmt.setString(3, schema);
                stmt.setString(4, table);
            }
            stmt.closeOnCompletion();
            stmt.setPoolable(false);
            ResultSet rs = stmt.executeQuery();
            if (rs.getFetchSize() < PREFETCH_SIZE)
                rs.setFetchSize(PREFETCH_SIZE);

            return rs;
        }
        catch (Throwable e) {
            LOGGER.debug("[DB-METADATA] Reverting to the default implementation: {}", e.toString());
            return super.getColumnsResultSet(id);
        }
    }

    private String getColumnsSql() {

        ImmutableMap<String, String> otherColumns = ImmutableMap.of(
                "COLUMN_NAME", sqlColumn("column_name"),
                // see https://docs.oracle.com/en/database/oracle/oracle-database/23/sqlrf/Data-Types.htm
                "DATA_TYPE", sqlDecode(sqlSubstring(sqlColumn("data_type"), 1, "TIMESTAMP".length()), ImmutableMap.of(
                                // TIMESTAMP [(fractional_seconds_precision)], where fractional_seconds_precision is a single digit
                                // TIMESTAMP [(fractional_seconds_precision)] WITH [LOCAL] TIME ZONE
                                "TIMESTAMP", sqlDecode(sqlSubstring(sqlColumn("data_type"), "TIMESTAMP".length() + 1, 1),
                                        ImmutableMap.of("(",
                                                // with (fractional_seconds_precision)
                                                sqlDecode(sqlSubstring(sqlColumn("data_type"), "TIMESTAMP(X) WITH ".length() + 1, "LOCAL".length()), ImmutableMap.of(
                                                        "LOCAL", -102, "TIME", -101), Types.TIMESTAMP)),
                                        // no (fractional_seconds_precision)
                                        sqlDecode(sqlSubstring(sqlColumn("data_type"), "TIMESTAMP WITH ".length() + 1, "LOCAL".length()), ImmutableMap.of(
                                                "LOCAL", -102, "TIME", -101), Types.TIMESTAMP)),
                                // INTERVAL YEAR [(year_precision)] TO MONTH
                                // INTERVAL DAY [(day_precision)] TO SECOND [(fractional_seconds_precision)]
                                "INTERVAL", sqlDecode(sqlSubstring(sqlColumn("data_type"),  "INTERVAL ".length() + 1, "DAY".length()), ImmutableMap.of(
                                        "DAY", -104, "YEA", -103))),
                        sqlDecode(sqlColumn("data_type"),
                                getSupportedSimpleTypes(),
                                sqlDecode("(SELECT a.typecode " +
                                                "                      FROM ALL_TYPES a " +
                                                "                      WHERE a.type_name = t.data_type" +
                                                "                           AND ((a.owner IS NULL AND t.data_type_owner IS NULL)" +
                                                "                             OR (a.owner = t.data_type_owner)))", ImmutableMap.of(
                                                "OBJECT", Types.STRUCT,
                                                "COLLECTION", Types.ARRAY),
                                        1111))),
                "TYPE_NAME", sqlColumn("data_type"),
                "COLUMN_SIZE", sqlDecodeNull(sqlColumn("data_precision"),
                        sqlDecode(sqlColumn("data_type"), ImmutableMap.of(
                                        "NUMBER", sqlDecodeNull(sqlColumn("data_scale"),
                                                j2ee13Compliant ? "38" : "0", "38")),
                                sqlDecode(sqlColumn("data_type"), ImmutableMap.of(
                                                "CHAR", sqlColumn("char_length"),
                                                "VARCHAR", sqlColumn("char_length"),
                                                "VARCHAR2", sqlColumn("char_length"),
                                                "NVARCHAR2", sqlColumn("char_length"),
                                                "NCHAR", sqlColumn("char_length"),
                                                "NUMBER", "0"),
                                        sqlColumn("data_length"))),
                        sqlColumn("data_precision")),
                "DECIMAL_DIGITS", sqlDecode(sqlColumn("data_type"), ImmutableMap.of(
                                "NUMBER", sqlDecodeNull(sqlColumn("data_precision"),
                                        sqlDecodeNull(sqlColumn("data_scale"),
                                                j2ee13Compliant ? "0" : "-127",
                                                sqlColumn("data_scale")),
                                        sqlColumn("data_scale"))),
                        sqlColumn("data_scale")),
                "NULLABLE", sqlDecode(sqlColumn("nullable"), ImmutableMap.of("N", 0), 1),
                "ORDINAL_POSITION", sqlColumn("column_id"));

        String queryHint = (includeSynonyms && versionNumber >= 10200 && versionNumber < 11100) ? "/*+ CHOOSE */" : "";
        String allColumnsTable = versionNumber >= 12000 ? "all_tab_cols" : "all_tab_columns";
        String userGeneratedFilter = versionNumber >= 12000 ? " AND " + sqlColumn("user_generated") + " = 'YES'" : "";

        return sqlSelectFromWhere(queryHint, concat(ImmutableMap.of(
                                "TABLE_CAT", "NULL",
                                "TABLE_SCHEM", sqlColumn("owner"),
                                "TABLE_NAME", sqlColumn("table_name")),
                        otherColumns),
                allColumnsTable + " t",
                sqlColumn("owner") + " = ? AND " + sqlColumn("table_name") + " = ?" +
                        userGeneratedFilter)

                + (includeSynonyms
                ? "\nUNION ALL\n"

                + sqlSelectFromWhere(queryHint, concat(ImmutableMap.of(
                                "TABLE_CAT", "NULL",
                                "TABLE_SCHEM", "REGEXP_SUBSTR(LTRIM(s.owner, '/'), '[^/]+')",
                                "TABLE_NAME", "REGEXP_SUBSTR(LTRIM(s.synonym_name, '/'), '[^/]+')"),
                        otherColumns),
                allColumnsTable + " t,\n" +
                        "(SELECT SYS_CONNECT_BY_PATH(owner, '/') owner, " +
                        "SYS_CONNECT_BY_PATH(synonym_name, '/') synonym_name, " +
                        "table_owner, table_name\n" +
                        "FROM all_synonyms\n" +
                        "WHERE CONNECT_BY_ISLEAF = 1\n" +
                        "AND db_link is NULL\n" +
                        "START WITH owner = ? AND synonym_name = ?\n" +
                        "CONNECT BY PRIOR table_name = synonym_name\n" +
                        "AND PRIOR table_owner = owner) s",
                sqlColumn("owner") + " = s.table_owner AND " + sqlColumn("table_name") + " = s.table_name " +
                        userGeneratedFilter)
                : "")

                + "\n" + "ORDER BY " + String.join(", ", ImmutableList.of("TABLE_SCHEM", "TABLE_NAME", "ORDINAL_POSITION"));
    }

    private static ImmutableMap<String, String> concat(ImmutableMap<String, String> m1, ImmutableMap<String, String> m2) {
        return Stream.concat(m1.entrySet().stream(), m2.entrySet().stream()).collect(ImmutableCollectors.toMap());
    }

    private static String sqlSubstring(String expression, int start, int length) {
        return "substr(" + expression + ", " + start + ", " + length + ")";
    }

    private static String sqlColumn(String column) {
        return "t." + column;
    }

    private static String sqlSelectFromWhere(String hint, ImmutableMap<String, String> columns, String from, String where) {
        return "SELECT " + hint + " " +
                columns.entrySet().stream()
                        .map(e -> e.getValue() + " AS " + e.getKey())
                        .collect(Collectors.joining(",\n")) +
                "\nFROM " + from +
                "\nWHERE " + where;
    }

    private static String sqlDecodeNull(String expression, Object nullValue, Object otherValue) {
        return "DECODE(" + expression + ", NULL, " + nullValue + ", " + otherValue +  ")";
    }


    private static String sqlDecode(String expression, Map<String, ?> cases, Object defaultValue) {
        return "DECODE(" + expression + ", " + cases.entrySet().stream()
                .map(e -> "'" + e.getKey() + "', " + e.getValue())
                .collect(Collectors.joining(", "))
                +  ", " + defaultValue +  ")";
    }

    private static String sqlDecode(String expression, Map<String, ?> cases) {
        return "DECODE(" + expression + ", " + cases.entrySet().stream()
                .map(e -> "'" + e.getKey() + "', " + e.getValue())
                .collect(Collectors.joining(", "))
                +  ")";
    }

    private Map<String, Integer> getSupportedSimpleTypes() {
        return ImmutableMap.ofEntries(
                Map.entry("BINARY_DOUBLE", 101),
                Map.entry("BINARY_FLOAT", 100),
                Map.entry("BFILE", -13),
                Map.entry("BLOB", Types.BLOB),
                Map.entry("BOOLEAN", Types.BOOLEAN),
                Map.entry("CHAR", Types.CHAR),
                Map.entry("CLOB", Types.CLOB),
                Map.entry("COLLECTION", Types.ARRAY),
                Map.entry("DATE", (mapDateToTimestamp ? Types.TIMESTAMP : Types.DATE)),
                Map.entry("FLOAT", Types.FLOAT),
                Map.entry("JSON", 2016),
                Map.entry("LONG", Types.LONGVARCHAR),
                Map.entry("LONG RAW", Types.LONGVARBINARY),
                Map.entry("NCHAR", Types.NCHAR),
                Map.entry("NCLOB", Types.NCLOB),
                Map.entry("NUMBER", Types.NUMERIC),
                Map.entry("NVARCHAR", Types.NVARCHAR),
                Map.entry("NVARCHAR2", Types.NVARCHAR),
                Map.entry("OBJECT", Types.STRUCT),
                Map.entry("OPAQUE/XMLTYPE", Types.SQLXML),
                Map.entry("RAW", Types.VARBINARY),
                Map.entry("REF", Types.REF),
                Map.entry("ROWID", Types.ROWID),
                Map.entry("SQLXML", Types.SQLXML),
                Map.entry("UROWID" , Types.ROWID),
                Map.entry("VARCHAR2", Types.VARCHAR),
                Map.entry("VARRAY", Types.ARRAY),
                Map.entry("VECTOR", -105),
                Map.entry("XMLTYPE", Types.SQLXML));
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

    private static final ImmutableSet<String> IGNORED_SYSTEM_VIEWS = ImmutableSet.of("SCHEDULER_PROGRAM_ARGS",
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

