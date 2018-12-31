package it.unibz.inf.ontop.answering.connection.impl;

import java.util.Optional;

import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;
import it.unibz.inf.ontop.answering.reformulation.impl.SQLExecutableQuery;
import it.unibz.inf.ontop.answering.reformulation.input.*;
import it.unibz.inf.ontop.answering.resultset.impl.*;
import it.unibz.inf.ontop.answering.resultset.BooleanResultSet;
import it.unibz.inf.ontop.answering.resultset.SimpleGraphResultSet;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.OntopSystemSQLSettings;
import it.unibz.inf.ontop.answering.resultset.impl.PredefinedBooleanResultSet;

import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import org.apache.commons.rdf.api.RDF;

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
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final RDF rdfFactory;
    private final OntopSystemSQLSettings settings;

    public SQLQuestStatement(QueryReformulator queryProcessor, Statement sqlStatement,
                             Optional<IRIDictionary> iriDictionary, DBMetadata dbMetadata,
                             InputQueryFactory inputQueryFactory,
                             TermFactory termFactory, TypeFactory typeFactory,
                             RDF rdfFactory, OntopSystemSQLSettings settings) {
        super(queryProcessor, inputQueryFactory);
        this.sqlStatement = sqlStatement;
        this.dbMetadata = dbMetadata;
        this.iriDictionary = iriDictionary;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.rdfFactory = rdfFactory;
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
    public int getTupleCount(InputQuery inputQuery) throws OntopReformulationException, OntopQueryEvaluationException {
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
            return new PredefinedBooleanResultSet(false);
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
                    ? new SQLDistinctTupleResultSet(set, executableQuery.getSignature(), dbMetadata, iriDictionary,
                    termFactory, typeFactory, rdfFactory)
                    : new DelegatedIriSQLTupleResultSet(set, executableQuery.getSignature(), dbMetadata, iriDictionary,
                    termFactory, typeFactory, rdfFactory);
        } catch (SQLException e) {
            throw new OntopQueryEvaluationException(e);
        }
    }

    @Override
    protected SimpleGraphResultSet executeGraphQuery(ConstructQuery inputQuery, ExecutableQuery executableQuery,
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
                tuples = new DelegatedIriSQLTupleResultSet(set, executableQuery.getSignature(), dbMetadata,
                        iriDictionary, termFactory, typeFactory, rdfFactory);
            } catch (SQLException e) {
                throw new OntopQueryEvaluationException(e.getMessage());
            }
        }
        return new DefaultSimpleGraphResultSet(tuples, inputQuery.getConstructTemplate(), collectResults, termFactory, rdfFactory);
    }

    private SQLExecutableQuery checkAndConvertTargetQuery(ExecutableQuery executableQuery) {
        if (! (executableQuery instanceof SQLExecutableQuery)) {
            throw new IllegalArgumentException("A SQLQuestStatement only accepts SQLTargetQuery instances");
        }
        return (SQLExecutableQuery) executableQuery;
    }
}
