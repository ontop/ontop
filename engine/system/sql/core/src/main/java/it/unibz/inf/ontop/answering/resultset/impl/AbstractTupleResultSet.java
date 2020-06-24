package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;


public abstract class AbstractTupleResultSet implements TupleResultSet {

    protected final ResultSet rs;
    protected final ImmutableList<Variable> signature;
    private final QueryLogger queryLogger;

    /**
     * Flag used to emulate the expected behavior of next() and hasNext()
     * This workaround is due to the fact that java.sql.ResultSet does not have a hasNext() method.
     * Keeps track of whether next() or hasNext() has been called last.
     */
    private boolean lastCalledIsHasNext = false;

    /* Set to false iff the moveCursor() method returned false (at least once) */
    private boolean foundNextElement = true;

    private long rowCount = 0;

    AbstractTupleResultSet(ResultSet rs, ImmutableList<Variable> signature, QueryLogger queryLogger){
        this.rs = rs;
        this.signature = signature;
        this.queryLogger = queryLogger;
    }

    @Override
    public int getColumnCount() {
        return signature.size();
    }

    @Override
    public int getFetchSize() throws OntopConnectionException {
        try {
            return rs.getFetchSize();
        } catch (Exception e) {
            throw new OntopConnectionException(e.getMessage());
        }
    }

    @Override
    public ImmutableList<String> getSignature() {
        return signature.stream()
                .map(Variable::getName)
                .collect(ImmutableCollectors.toList());
    }


    @Override
    public OntopBindingSet next() throws OntopConnectionException {

        if (!lastCalledIsHasNext) {
            try {
                // Moves cursor one result ahead
                foundNextElement = moveCursor();
            } catch (Exception e) {
                throw new OntopConnectionException(e);
            }
        }
        lastCalledIsHasNext = false;
        if (!foundNextElement) {
            throw new NoSuchElementException("No next OntopBindingSet in this TupleResultSet");
        }
        return readCurrentRow();
    }

    @Override
    public boolean hasNext() throws OntopConnectionException {
        if (!lastCalledIsHasNext) {
            lastCalledIsHasNext = true;
            try {
                // Moves cursor one result ahead
                foundNextElement = moveCursor();
                if (foundNextElement)
                    rowCount++;
            } catch (Exception e) {
                throw new OntopConnectionException(e);
            }
        }
        if (!foundNextElement)
            queryLogger.declareLastResultRetrievedAndSerialize(rowCount);
        return foundNextElement;
    }

    /* This method can be overwritten to ensure distinct rows */
    protected boolean moveCursor() throws SQLException, OntopConnectionException {
        return rs.next();
    }

    @Override
    public void close() throws OntopConnectionException {
        try {
            rs.close();
        } catch (Exception e) {
            throw new OntopConnectionException(e);
        }
    }

    protected abstract OntopBindingSet readCurrentRow() throws OntopConnectionException;
}
