package org.semanticweb.ontop.owlrefplatform.core;

import org.semanticweb.ontop.model.OBDAConnection;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.ResultSet;
import org.semanticweb.ontop.model.TupleResultSet;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.owlrefplatform.core.execution.TargetQueryExecutionException;
import org.semanticweb.ontop.owlrefplatform.core.resultset.BooleanOWLOBDARefResultSet;
import org.semanticweb.ontop.owlrefplatform.core.resultset.EmptyQueryResultSet;
import org.semanticweb.ontop.owlrefplatform.core.resultset.QuestGraphResultSet;
import org.semanticweb.ontop.owlrefplatform.core.resultset.QuestResultset;

import java.sql.SQLException;
import java.sql.Statement;
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

        String unf = unfoldAndGenerateTargetQuery(query).getNativeQueryString();
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

    protected void cancelTargetQueryStatement() throws TargetQueryExecutionException {
        try {
            sqlStatement.cancel();
        } catch (SQLException e) {
            throw new TargetQueryExecutionException(e.getMessage());
        }
    }

    @Override
    protected ResultSet executeBooleanQuery(TargetQuery targetQuery) throws TargetQueryExecutionException {
        String sqlQuery = targetQuery.getNativeQueryString();
        if (sqlQuery.equals("")) {
            return new BooleanOWLOBDARefResultSet(false, this);
        }

        try {
            java.sql.ResultSet set = sqlStatement.executeQuery(sqlQuery);
            return new BooleanOWLOBDARefResultSet(set, this);
        } catch (SQLException e) {
            throw new TargetQueryExecutionException(e.getMessage());
        }
    }

    @Override
    protected ResultSet executeSelectQuery(TargetQuery targetQuery) throws OBDAException {
        String sqlQuery = targetQuery.getNativeQueryString();
        if (sqlQuery.equals("") ) {
            return new EmptyQueryResultSet(targetQuery.getSignature(), this);
        }
        try {
            java.sql.ResultSet set = sqlStatement.executeQuery(sqlQuery);
            return new QuestResultset(set, targetQuery.getSignature(), this);
        } catch (SQLException e) {
            throw new TargetQueryExecutionException(e.getMessage());
        }
    }

    @Override
    protected ResultSet executeGraphQuery(TargetQuery targetQuery, boolean collectResults) throws  OBDAException {
        String sqlQuery = targetQuery.getNativeQueryString();
        if (sqlQuery.equals("") ) {
            return new EmptyQueryResultSet(targetQuery.getSignature(), this);
        }
        try {
            java.sql.ResultSet set = sqlStatement.executeQuery(sqlQuery);
            TupleResultSet tuples = new QuestResultset(set, targetQuery.getSignature(), this);
            return new QuestGraphResultSet(tuples, targetQuery.getConstructTemplate(), collectResults);
        } catch (SQLException e) {
            throw new TargetQueryExecutionException(e.getMessage());
        }
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

    /**
     * Not implemented by default (in the virtual mode)
     */
    @Override
    public int insertData(Iterator<Assertion> data, boolean useFile, int commit, int batch) throws OBDAException {
        throw new OBDAException("Data insertion not supported by default.");
    }
}
