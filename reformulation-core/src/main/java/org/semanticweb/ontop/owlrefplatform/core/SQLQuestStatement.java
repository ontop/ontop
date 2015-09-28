package org.semanticweb.ontop.owlrefplatform.core;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.owlrefplatform.core.execution.NativeQueryExecutionException;
import org.semanticweb.ontop.owlrefplatform.core.resultset.BooleanOWLOBDARefResultSet;
import org.semanticweb.ontop.owlrefplatform.core.resultset.EmptyQueryResultSet;
import org.semanticweb.ontop.owlrefplatform.core.resultset.QuestGraphResultSet;
import org.semanticweb.ontop.owlrefplatform.core.resultset.QuestResultset;
import org.semanticweb.ontop.owlrefplatform.core.translator.SesameConstructTemplate;

import java.sql.*;
import java.sql.ResultSet;
import java.util.Iterator;

/**
 * SQL-specific implementation of OBDAStatement.
 * Derived from QuestStatement.
 */
public class SQLQuestStatement extends QuestStatement {

    private final Statement sqlStatement;

    public SQLQuestStatement(IQuest questInstance, OBDAConnection obdaConnection, Statement sqlStatement) {
        super(questInstance, obdaConnection);
        this.sqlStatement = sqlStatement;
    }

    @Override
    public int getFetchSize() throws OBDAException {
        try {
            return sqlStatement.getFetchSize();
        } catch (Exception e) {
            throw new OBDAException(e);
        }

    }

    @Override
    public int getMaxRows() throws OBDAException {
        try {
            return sqlStatement.getMaxRows();
        } catch (Exception e) {
            throw new OBDAException(e);
        }

    }

    @Override
    public void getMoreResults() throws OBDAException {
        try {
            sqlStatement.getMoreResults();
        } catch (Exception e) {
            throw new OBDAException(e);
        }

    }

    @Override
    public void setFetchSize(int rows) throws OBDAException {
        try {
            sqlStatement.setFetchSize(rows);
        } catch (Exception e) {
            throw new OBDAException(e);
        }

    }

    @Override
    public void setMaxRows(int max) throws OBDAException {
        try {
            sqlStatement.setMaxRows(max);
        } catch (Exception e) {
            throw new OBDAException(e);
        }

    }

    @Override
    public void setQueryTimeout(int seconds) throws OBDAException {
        try {
            sqlStatement.setQueryTimeout(seconds);
        } catch (Exception e) {
            throw new OBDAException(e);
        }
    }

    @Override
    public int getQueryTimeout() throws OBDAException {
        try {
            return sqlStatement.getQueryTimeout();
        } catch (Exception e) {
            throw new OBDAException(e);
        }
    }

    @Override
    public boolean isClosed() throws OBDAException {
        try {
            return sqlStatement.isClosed();
        } catch (Exception e) {
            throw new OBDAException(e);
        }
    }

    /**
     * Returns the number of tuples returned by the query
     */
    @Override
    public int getTupleCount(String query) throws OBDAException {


        SQLExecutableQuery targetQuery = checkAndConvertTargetQuery(unfoldAndGenerateTargetQuery(query));
        String unf = targetQuery.getSQL();
        String newsql = "SELECT count(*) FROM (" + unf + ") t1";
        if (!isCanceled()) {
            try {

                java.sql.ResultSet set = sqlStatement.executeQuery(newsql);
                if (set.next()) {
                    return set.getInt(1);
                } else {
                    throw new OBDAException("Tuple count failed due to empty result set.");
                }
            } catch (SQLException e) {
                throw new OBDAException(e.getMessage());
            }
        }
        else {
            throw new OBDAException("Action canceled.");
        }
    }

    @Override
    public void close() throws OBDAException {
        try {
            if (sqlStatement != null)
                sqlStatement.close();
        } catch (Exception e) {
            throw new OBDAException(e);
        }
    }

    protected void cancelTargetQueryStatement() throws NativeQueryExecutionException {
        try {
            sqlStatement.cancel();
        } catch (SQLException e) {
            throw new NativeQueryExecutionException(e.getMessage());
        }
    }

    @Override
    protected TupleResultSet executeBooleanQuery(ExecutableQuery executableQuery) throws NativeQueryExecutionException {
        SQLExecutableQuery sqlTargetQuery = checkAndConvertTargetQuery(executableQuery);
        String sqlQuery = sqlTargetQuery.getSQL();
        if (sqlQuery.equals("")) {
            return new BooleanOWLOBDARefResultSet(false, this);
        }

        try {
            java.sql.ResultSet set = sqlStatement.executeQuery(sqlQuery);
            return new BooleanOWLOBDARefResultSet(set, this);
        } catch (SQLException e) {
            throw new NativeQueryExecutionException(e.getMessage());
        }
    }

    @Override
    protected TupleResultSet executeSelectQuery(ExecutableQuery executableQuery) throws OBDAException {
        SQLExecutableQuery sqlTargetQuery = checkAndConvertTargetQuery(executableQuery);
        String sqlQuery = sqlTargetQuery.getSQL();

        if (sqlQuery.equals("") ) {
            return new EmptyQueryResultSet(executableQuery.getSignature(), this);
        }
        try {
            java.sql.ResultSet set = sqlStatement.executeQuery(sqlQuery);
            return new QuestResultset(set, executableQuery.getSignature(), this);
        } catch (SQLException e) {
            throw new NativeQueryExecutionException(e.getMessage());
        }
    }

    @Override
    protected GraphResultSet executeGraphQuery(ExecutableQuery executableQuery, boolean collectResults) throws  OBDAException {
        SQLExecutableQuery sqlTargetQuery = checkAndConvertTargetQuery(executableQuery);

        String sqlQuery = sqlTargetQuery.getSQL();
        Optional<SesameConstructTemplate> optionalTemplate = sqlTargetQuery.getOptionalConstructTemplate();

        if (!optionalTemplate.isPresent()) {
            throw new IllegalArgumentException("A CONSTRUCT template is required for executing a graph query");
        }

        TupleResultSet tuples;

        if (sqlQuery.equals("") ) {
            tuples = new EmptyQueryResultSet(executableQuery.getSignature(), this);
        }
        else {
            try {
                ResultSet set = sqlStatement.executeQuery(sqlQuery);
                tuples = new QuestResultset(set, executableQuery.getSignature(), this);
            } catch (SQLException e) {
                throw new NativeQueryExecutionException(e.getMessage());
            }
        }
        return new QuestGraphResultSet(tuples, optionalTemplate.get(), collectResults);
    }

    private SQLExecutableQuery checkAndConvertTargetQuery(ExecutableQuery executableQuery) {
        if (! (executableQuery instanceof SQLExecutableQuery)) {
            throw new IllegalArgumentException("A SQLQuestStatement only accepts SQLTargetQuery instances");
        }
        return (SQLExecutableQuery) executableQuery;
    }

    protected Statement getSQLStatement() {
        return sqlStatement;
    }

    /**
     * Not implemented by default (in the virtual mode)
     */
    @Override
    public int insertData(Iterator<Assertion> data, int commit, int batch) throws OBDAException {
        throw new OBDAException("Data insertion not supported by default.");
    }
}
