package it.unibz.inf.ontop.owlrefplatform.core;

import java.util.Optional;

import it.unibz.inf.ontop.answering.input.*;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.OntopSystemSQLSettings;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.owlrefplatform.core.resultset.*;

import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.answering.reformulation.QueryTranslator;

import java.sql.*;
import java.sql.ResultSet;

/**
 * SQL-specific implementation of OBDAStatement.
 * Derived from QuestStatement.
 */
public class SQLQuestStatement extends QuestStatement {

    private final Statement sqlStatement;
    private final DBMetadata dbMetadata;
    private final Optional<IRIDictionary> iriDictionary;
    private final OntopSystemSQLSettings settings;

    public SQLQuestStatement(QueryTranslator queryProcessor, Statement sqlStatement,
                             Optional<IRIDictionary> iriDictionary, DBMetadata dbMetadata,
                             InputQueryFactory inputQueryFactory,
                             OntopSystemSQLSettings settings) {
        super(queryProcessor, inputQueryFactory);
        this.sqlStatement = sqlStatement;
        this.dbMetadata = dbMetadata;
        this.iriDictionary = iriDictionary;
        this.settings = settings;
    }

    @Override
    public int getFetchSize() throws OntopConnectionException {
        try {
            return sqlStatement.getFetchSize();
        } catch (SQLException e) {
            throw new OntopConnectionException(e);
        }

    }

    @Override
    public int getMaxRows() throws OntopConnectionException {
        try {
            return sqlStatement.getMaxRows();
        } catch (SQLException e) {
            throw new OntopConnectionException(e);
        }

    }

    @Override
    public void getMoreResults() throws OntopConnectionException {
        try {
            sqlStatement.getMoreResults();
        } catch (SQLException e) {
            throw new OntopConnectionException(e);
        }

    }

    @Override
    public void setFetchSize(int rows) throws OntopConnectionException {
        try {
            sqlStatement.setFetchSize(rows);
        } catch (SQLException e) {
            throw new OntopConnectionException(e);
        }

    }

    @Override
    public void setMaxRows(int max) throws OntopConnectionException {
        try {
            sqlStatement.setMaxRows(max);
        } catch (SQLException e) {
            throw new OntopConnectionException(e);
        }

    }

    @Override
    public void setQueryTimeout(int seconds) throws OntopConnectionException {
        try {
            sqlStatement.setQueryTimeout(seconds);
        } catch (SQLException e) {
            throw new OntopConnectionException(e);
        }
    }

    @Override
    public int getQueryTimeout() throws OntopConnectionException {
        try {
            return sqlStatement.getQueryTimeout();
        } catch (SQLException e) {
            throw new OntopConnectionException(e);
        }
    }

    @Override
    public boolean isClosed() throws OntopConnectionException {
        try {
            return sqlStatement.isClosed();
        } catch (SQLException e) {
            throw new OntopConnectionException(e);
        }
    }

    /**
     * Returns the number of tuples returned by the query
     */
    @Override
    public int getTupleCount(InputQuery inputQuery) throws OntopTranslationException, OntopQueryEvaluationException {
        SQLExecutableQuery targetQuery = checkAndConvertTargetQuery(getExecutableQuery(inputQuery));
        String sql = targetQuery.getSQL();
        String newsql = "SELECT count(*) FROM (" + sql + ") t1";
        if (!isCanceled()) {
            try {

                java.sql.ResultSet set = sqlStatement.executeQuery(newsql);
                if (set.next()) {
                    return set.getInt(1);
                } else {
                    //throw new OBDAException("Tuple count failed due to empty result set.");
                    return 0;
                }
            } catch (SQLException e) {
                throw new OntopQueryEvaluationException(e);
            }
        }
        else {
            throw new OntopQueryEvaluationException("Action canceled.");
        }
    }

    @Override
    public void close() throws OntopConnectionException {
        try {
            if (sqlStatement != null)
                sqlStatement.close();
        } catch (SQLException e) {
            throw new OntopConnectionException(e);
        }
    }

    protected void cancelExecution() throws OntopQueryEvaluationException {
        try {
            sqlStatement.cancel();
        } catch (SQLException e) {
            throw new OntopQueryEvaluationException(e);
        }
    }

    @Override
    protected BooleanResultSet executeBooleanQuery(ExecutableQuery executableQuery)
            throws OntopQueryEvaluationException {
        SQLExecutableQuery sqlTargetQuery = checkAndConvertTargetQuery(executableQuery);
        String sqlQuery = sqlTargetQuery.getSQL();
        if (sqlQuery.equals("")) {
            return new SQLBooleanResultSet(false);
        }

        try {
            java.sql.ResultSet set = sqlStatement.executeQuery(sqlQuery);
            return new SQLBooleanResultSet(set);
        } catch (SQLException e) {
            throw new OntopQueryEvaluationException(e.getMessage());
        }
    }

    @Override
    protected TupleResultSet executeSelectQuery(ExecutableQuery executableQuery)
            throws OntopQueryEvaluationException {
        SQLExecutableQuery sqlTargetQuery = checkAndConvertTargetQuery(executableQuery);
        String sqlQuery = sqlTargetQuery.getSQL();

        if (sqlQuery.equals("") ) {
            return new EmptyTupleResultSet(executableQuery.getSignature());
        }
        try {
            java.sql.ResultSet set = sqlStatement.executeQuery(sqlQuery);
            return settings.isDistinctPostProcessingEnabled()
                    ? new QuestDistinctTupleResultSet(set, executableQuery.getSignature(), dbMetadata, iriDictionary)
                    : new QuestTupleResultSet(set, executableQuery.getSignature(), dbMetadata, iriDictionary);
        } catch (SQLException e) {
            throw new OntopQueryEvaluationException(e);
        }
    }

    @Override
    protected GraphResultSet executeGraphQuery(ConstructQuery inputQuery, ExecutableQuery executableQuery,
                                               boolean collectResults)
            throws OntopQueryEvaluationException, OntopResultConversionException, OntopConnectionException {
        SQLExecutableQuery sqlTargetQuery = checkAndConvertTargetQuery(executableQuery);

        String sqlQuery = sqlTargetQuery.getSQL();

        TupleResultSet tuples;

        if (sqlQuery.equals("") ) {
            tuples = new EmptyTupleResultSet(executableQuery.getSignature());
        }
        else {
            try {
                ResultSet set = sqlStatement.executeQuery(sqlQuery);
                tuples = new QuestTupleResultSet(set, executableQuery.getSignature(), dbMetadata,
                        iriDictionary);
            } catch (SQLException e) {
                throw new OntopQueryEvaluationException(e.getMessage());
            }
        }
        return new QuestGraphResultSet(tuples, inputQuery.getConstructTemplate(), collectResults);
    }

    private SQLExecutableQuery checkAndConvertTargetQuery(ExecutableQuery executableQuery) {
        if (! (executableQuery instanceof SQLExecutableQuery)) {
            throw new IllegalArgumentException("A SQLQuestStatement only accepts SQLTargetQuery instances");
        }
        return (SQLExecutableQuery) executableQuery;
    }
}
