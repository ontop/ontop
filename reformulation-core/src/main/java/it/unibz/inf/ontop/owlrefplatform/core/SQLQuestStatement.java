package it.unibz.inf.ontop.owlrefplatform.core;

import java.util.Optional;

import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.owlrefplatform.core.execution.NativeQueryExecutionException;
import it.unibz.inf.ontop.owlrefplatform.core.resultset.*;
import it.unibz.inf.ontop.owlrefplatform.core.translator.SesameConstructTemplate;

import it.unibz.inf.ontop.ontology.Assertion;
import it.unibz.inf.ontop.reformulation.IRIDictionary;
import it.unibz.inf.ontop.reformulation.OBDAQueryProcessor;

import java.sql.*;
import java.sql.ResultSet;
import java.util.Iterator;

/**
 * SQL-specific implementation of OBDAStatement.
 * Derived from QuestStatement.
 */
public class SQLQuestStatement extends QuestStatement {

    private final Statement sqlStatement;
    private final DBMetadata dbMetadata;
    private final Optional<IRIDictionary> iriDictionary;

    public SQLQuestStatement(OBDAQueryProcessor queryProcessor, OBDAConnection obdaConnection, Statement sqlStatement,
                             Optional<IRIDictionary> iriDictionary) {
        super(queryProcessor, obdaConnection);
        this.sqlStatement = sqlStatement;
        this.dbMetadata = queryProcessor.getDBMetadata();
        this.iriDictionary = iriDictionary;
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
    public int getTupleCount(String sparqlQuery) throws OBDAException {
        SQLExecutableQuery targetQuery = checkAndConvertTargetQuery(getExecutableQuery(sparqlQuery));
        String sql = targetQuery.getSQL();
        String newsql = "SELECT count(*) FROM (" + sql + ") t1";
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

    protected void cancelExecution() throws NativeQueryExecutionException {
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
            return new BooleanResultSet(false, this);
        }

        try {
            java.sql.ResultSet set = sqlStatement.executeQuery(sqlQuery);
            return new BooleanResultSet(set, this);
        } catch (SQLException e) {
            throw new NativeQueryExecutionException(e.getMessage());
        }
    }

    @Override
    protected TupleResultSet executeSelectQuery(ExecutableQuery executableQuery, boolean doDistinctPostProcessing) throws OBDAException {
        SQLExecutableQuery sqlTargetQuery = checkAndConvertTargetQuery(executableQuery);
        String sqlQuery = sqlTargetQuery.getSQL();

        if (sqlQuery.equals("") ) {
            return new EmptyTupleResultSet(executableQuery.getSignature(), this);
        }
        try {
            java.sql.ResultSet set = sqlStatement.executeQuery(sqlQuery);
            return doDistinctPostProcessing
                    ? new QuestDistinctTupleResultSet(set, executableQuery.getSignature(), this, dbMetadata, iriDictionary)
                    : new QuestTupleResultSet(set, executableQuery.getSignature(), this, dbMetadata, iriDictionary);
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
            tuples = new EmptyTupleResultSet(executableQuery.getSignature(), this);
        }
        else {
            try {
                ResultSet set = sqlStatement.executeQuery(sqlQuery);
                tuples = new QuestTupleResultSet(set, executableQuery.getSignature(), this, dbMetadata,
                        iriDictionary);
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
