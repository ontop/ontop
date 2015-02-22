package org.semanticweb.ontop.owlrefplatform.core;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.RDBMSourceParameterConstants;
import org.semanticweb.ontop.nativeql.DBMetadataException;
import org.semanticweb.ontop.nativeql.DBMetadataExtractor;
import org.semanticweb.ontop.owlrefplatform.core.queryevaluation.SQLAdapterFactory;
import org.semanticweb.ontop.owlrefplatform.core.queryevaluation.SQLDialectAdapter;
import org.semanticweb.ontop.owlrefplatform.core.queryevaluation.SQLServerSQLDialectAdapter;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.ImplicitDBConstraints;
import org.semanticweb.ontop.utils.MetaMappingExpander;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.URI;
import java.sql.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * For RDBMS having a JDBC driver.
 */
public class JDBCConnector implements DBConnector {

    private final IQuest questInstance;
    private final QuestPreferences questPreferences;

    /* The active connection used to get metadata from the DBMS */
    private transient Connection localConnection;
    private final OBDADataSource obdaSource;

    private final Logger log = LoggerFactory.getLogger(JDBCConnector.class);
    private PoolProperties poolProperties;
    private DataSource tomcatPool;

    // Tomcat pool default properties
    // These can be changed in the properties file
    private final int maxPoolSize;
    private final int startPoolSize;
    private final boolean removeAbandoned ;
    private final boolean logAbandoned = false;
    private final int abandonedTimeout;
    private final boolean keepAlive;

    private final NativeQueryLanguageComponentFactory nativeQLFactory;

    /**
     * TODO: see if we can ignore the questInstance
     */
    @Inject
    private JDBCConnector(@Assisted OBDADataSource obdaDataSource, @Assisted IQuest questInstance,
                          NativeQueryLanguageComponentFactory nativeQLFactory,
                          QuestPreferences preferences) {
        this.questPreferences = preferences;
        this.obdaSource = obdaDataSource;
        this.questInstance = questInstance;
        this.nativeQLFactory = nativeQLFactory;
        keepAlive = Boolean.valueOf((String) preferences.get(QuestPreferences.KEEP_ALIVE));
        removeAbandoned = Boolean.valueOf((String) preferences.get(QuestPreferences.REMOVE_ABANDONED));
        abandonedTimeout = Integer.valueOf((String) preferences.get(QuestPreferences.ABANDONED_TIMEOUT));
        startPoolSize = Integer.valueOf((String) preferences.get(QuestPreferences.INIT_POOL_SIZE));
        maxPoolSize = Integer.valueOf((String) preferences.get(QuestPreferences.MAX_POOL_SIZE));

        setupConnectionPool();
    }

    /***
     * Starts the local connection that Quest maintains to the DBMS. This
     * connection belongs only to Quest and is used to get information from the
     * DBMS. At the moment this connection is mainly used during initialization,
     * to get metadata about the DBMS or to create repositories in classic mode.
     *
     * @return
     * @throws SQLException
     */
    public boolean connect() throws SQLException {
        if (localConnection != null && !localConnection.isClosed()) {
            return true;
        }
        String url = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
        String username = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
        String password = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
        String driver = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e1) {
            // Does nothing because the SQLException handles this problem also.
        }
        localConnection = DriverManager.getConnection(url, username, password);

        if (localConnection != null) {
            return true;
        }
        return false;
    }

    @Override
    public void disconnect() throws SQLException {
        try {
            localConnection.close();
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
    }

    @Override
    public void dispose() {
/*		try {
			if (evaluationEngine != null)
				this.evaluationEngine.dispose();
		} catch (Exception e) {
			log.debug("Error during disconnect: " + e.getMessage());
		}
*/
        try {
            if (localConnection != null && !localConnection.isClosed())
                disconnect();
        } catch (Exception e) {
            log.debug("Error during disconnect: " + e.getMessage());
        }
    }

    @Override
    public DBMetadata extractDBMetadata(OBDAModel obdaModel, @Nullable ImplicitDBConstraints userConstraints) throws DBMetadataException {
        DBMetadataExtractor dbMetadataExtractor = nativeQLFactory.create();
        return dbMetadataExtractor.extract(obdaSource, localConnection, obdaModel, userConstraints);
    }

    @Override
    public OBDAModel expandMetaMappings(OBDAModel unfoldingOBDAModel, URI sourceId) throws Exception {
        MetaMappingExpander metaMappingExpander = new MetaMappingExpander(localConnection);
        return metaMappingExpander.expand(unfoldingOBDAModel, sourceId);
    }

    @Override
    public void preprocessProjection(ImmutableList<OBDAMappingAxiom> mappings) throws SQLException {
        String parameter = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);
        SQLDialectAdapter sqladapter = SQLAdapterFactory.getSQLDialectAdapter(parameter, questPreferences);
        preprocessProjection(localConnection, mappings, OBDADataFactoryImpl.getInstance(), sqladapter);
    }

    private void setupConnectionPool() {
        String url = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
        String username = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
        String password = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
        String driver = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

        poolProperties = new PoolProperties();
        poolProperties.setUrl(url);
        poolProperties.setDriverClassName(driver);
        poolProperties.setUsername(username);
        poolProperties.setPassword(password);
        poolProperties.setJmxEnabled(true);

        // TEST connection before using it
        poolProperties.setTestOnBorrow(keepAlive);
        if (keepAlive) {
            if (driver.contains("oracle"))
                poolProperties.setValidationQuery("select 1 from dual");
            else if (driver.contains("db2"))
                poolProperties.setValidationQuery("select 1 from sysibm.sysdummy1");
            else
                poolProperties.setValidationQuery("select 1");
        }

        poolProperties.setTestOnReturn(false);
        poolProperties.setMaxActive(maxPoolSize);
        poolProperties.setMaxIdle(maxPoolSize);
        poolProperties.setInitialSize(startPoolSize);
        poolProperties.setMaxWait(30000);
        poolProperties.setRemoveAbandonedTimeout(abandonedTimeout);
        poolProperties.setMinEvictableIdleTimeMillis(30000);
        poolProperties.setLogAbandoned(logAbandoned);
        poolProperties.setRemoveAbandoned(removeAbandoned);
        poolProperties.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
                + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        tomcatPool = new DataSource();
        tomcatPool.setPoolProperties(poolProperties);

        log.debug("Connection Pool Properties:");
        log.debug("Start size: " + startPoolSize);
        log.debug("Max size: " + maxPoolSize);
        log.debug("Remove abandoned connections: " + removeAbandoned);
    }

    @Override
    public void close() {
        tomcatPool.close();
    }

    public void releaseSQLPoolConnection(Connection co) {
        try {
            co.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized Connection getSQLPoolConnection() throws OBDAException {
        Connection conn = null;
        try {
            conn = tomcatPool.getConnection();
        } catch (SQLException e) {
            throw new OBDAException(e);
        }
        return conn;
    }

    /***
     * Establishes a new connection to the data source. This is a normal JDBC
     * connection. Used only internally to get metadata at the moment.
     *
     * TODO: update comment
     *
     * @return
     * @throws OBDAException
     */
    protected Connection getSQLConnection() throws OBDAException {
        Connection conn;

        String url = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
        String username = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
        String password = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
        String driver = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

        // if (driver.contains("mysql")) {
        // url = url + "?relaxAutoCommit=true";
        // }
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e1) {
            log.debug(e1.getMessage());
        }
        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new OBDAException(e.getMessage());
        } catch (Exception e) {
            throw new OBDAException(e.getMessage());
        }
        return conn;
    }

    // get a real (non pool) connection - used for protege plugin
    @Override
    public IQuestConnection getNonPoolConnection() throws OBDAException {

        return new QuestConnection(questInstance, getSQLConnection());
    }

    /***
     * Returns a QuestConnection, the main object that a client should use to
     * access the query answering services of Quest. With the QuestConnection
     * you can get a QuestStatement to execute queries.
     *
     * <p>
     * Note, the QuestConnection is not a normal JDBC connection. It is a
     * wrapper of one of the N JDBC connections that quest's connection pool
     * starts on initialization. Calling .close() will not actually close the
     * connection, with will just release it back to the pool.
     * <p>
     * to close all connections you must call Quest.close().
     *
     * @return
     * @throws OBDAException
     */
    @Override
    public IQuestConnection getConnection() throws OBDAException {

        return new QuestConnection(questInstance, getSQLPoolConnection());
    }

    /***
     * Expands a SELECT * into a SELECT with all columns implicit in the *
     *
     * @param mappings
     * @param factory
     * @param adapter
     * @throws SQLException
     */
    private static void preprocessProjection(Connection localConnection, List<OBDAMappingAxiom> mappings,
                                             OBDADataFactory factory, SQLDialectAdapter adapter)
            throws SQLException {

        // TODO this code seems buggy, it will probably break easily (check the
        // part with
        // parenthesis in the beginning of the for loop.

        Statement st = null;
        try {
            st = localConnection.createStatement();
            for (OBDAMappingAxiom axiom : mappings) {
                String sourceString = axiom.getSourceQuery().toString();

				/*
				 * Check if the projection contains select all keyword, i.e.,
				 * 'SELECT * [...]'.
				 */
                if (containSelectAll(sourceString)) {
                    StringBuilder sb = new StringBuilder();

                    // If the SQL string has sub-queries in its statement
                    if (containChildParentSubQueries(sourceString)) {
                        int childquery1 = sourceString.indexOf("(");
                        int childquery2 = sourceString.indexOf(") AS child");
                        String childquery = sourceString.substring(childquery1 + 1, childquery2);

                        String copySourceQuery = createDummyQueryToFetchColumns(childquery, adapter);
                        if (st.execute(copySourceQuery)) {
                            ResultSetMetaData rsm = st.getResultSet().getMetaData();
                            boolean needComma = false;
                            for (int pos = 1; pos <= rsm.getColumnCount(); pos++) {
                                if (needComma) {
                                    sb.append(", ");
                                }
                                String col = rsm.getColumnName(pos);
                                //sb.append("CHILD." + col );
                                sb.append("child.\"" + col + "\" AS \"child_" + (col)+"\"");
                                needComma = true;
                            }
                        }
                        sb.append(", ");

                        int parentquery1 = sourceString.indexOf(", (", childquery2);
                        int parentquery2 = sourceString.indexOf(") AS parent");
                        String parentquery = sourceString.substring(parentquery1 + 3, parentquery2);

                        copySourceQuery = createDummyQueryToFetchColumns(parentquery, adapter);
                        if (st.execute(copySourceQuery)) {
                            ResultSetMetaData rsm = st.getResultSet().getMetaData();
                            boolean needComma = false;
                            for (int pos = 1; pos <= rsm.getColumnCount(); pos++) {
                                if (needComma) {
                                    sb.append(", ");
                                }
                                String col = rsm.getColumnName(pos);
                                //sb.append("PARENT." + col);
                                sb.append("parent.\"" + col + "\" AS \"parent_" + (col)+"\"");
                                needComma = true;
                            }
                        }

                        //If the SQL string doesn't have sub-queries
                    } else

                    {
                        String copySourceQuery = createDummyQueryToFetchColumns(sourceString, adapter);
                        boolean execute = st.execute(copySourceQuery);
                        if (execute) {
                            ResultSetMetaData rsm = st.getResultSet().getMetaData();
                            boolean needComma = false;
                            for (int pos = 1; pos <= rsm.getColumnCount(); pos++) {
                                if (needComma) {
                                    sb.append(", ");
                                }
                                sb.append("\"" + rsm.getColumnName(pos) + "\"");
                                needComma = true;
                            }
                        }
                    }

					/*
					 * Replace the asterisk with the proper column names
					 */
                    String columnProjection = sb.toString();
                    String tmp = axiom.getSourceQuery().toString();
                    int fromPosition = tmp.toLowerCase().indexOf("from");
                    int asteriskPosition = tmp.indexOf('*');
                    if (asteriskPosition != -1 && asteriskPosition < fromPosition) {
                        String str = sourceString.replaceFirst("\\*", columnProjection);
                        axiom.setSourceQuery(factory.getSQLQuery(str));
                    }
                }
            }
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }

    private static final String selectAllPattern = "(S|s)(E|e)(L|l)(E|e)(C|c)(T|t)\\s+\\*";
    private static final String subQueriesPattern = "\\(.*\\)\\s+(A|a)(S|s)\\s+(C|c)(H|h)(I|i)(L|l)(D|d),\\s+\\(.*\\)\\s+(A|a)(S|s)\\s+(P|p)(A|a)(R|r)(E|e)(N|n)(T|t)";


    private static boolean containSelectAll(String sql) {
        final Pattern pattern = Pattern.compile(selectAllPattern);
        return pattern.matcher(sql).find();
    }

    private static boolean containChildParentSubQueries(String sql) {
        final Pattern pattern = Pattern.compile(subQueriesPattern);
        return pattern.matcher(sql).find();
    }

    private static String createDummyQueryToFetchColumns(String originalQuery, SQLDialectAdapter adapter) {
        String toReturn = String.format("select * from (%s) view20130219 ", originalQuery);
        if (adapter instanceof SQLServerSQLDialectAdapter) {
            SQLServerSQLDialectAdapter sqlServerAdapter = (SQLServerSQLDialectAdapter) adapter;
            toReturn = sqlServerAdapter.sqlLimit(toReturn, 1);
        } else {
            toReturn += adapter.sqlSlice(0, Long.MIN_VALUE);
        }
        return toReturn;
    }
}
