package it.unibz.krdb.obda.owlrefplatform.core.resultset;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.TupleResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by elem on 7/15/2015.
 */
public class FourDResultSet extends QuestResultset {
    /***
     * Constructs an OBDA statement from an SQL statement, a signature described
     * by terms and a statement. The statement is maintained only as a reference
     * for closing operations.
     *
     * @param set
     * @param signature A list of terms that determines the type of the columns of
     *                  this results set.
     * @param st
     * @throws OBDAException
     */
    private int current = 0;
    private List<ResultSet> setList;

    public FourDResultSet(List <ResultSet> setList, List<String> signature, QuestStatement st) throws OBDAException {
        super(setList.get(0), signature, st);
        this.setList = setList;
        current++;
    }

    @Override
    public boolean nextRow() throws OBDAException {
        try {
            if(!set.next()){
                if(current == setList.size())
                    return false;
                set = setList.get(current++);
            }
            return set.next();
        } catch (SQLException e) {
            throw new OBDAException(e);
        }
    }

    @Override
    public int getFetchSize() throws OBDAException {
        try {
            int fetchSize = 0;
            for (ResultSet rs : setList) {
                fetchSize += rs.getFetchSize();
            }
            return fetchSize;
        } catch (SQLException e) {
            throw new OBDAException(e.getMessage());

        }
    }

    @Override
    public void close() throws OBDAException {
        try {
            for(ResultSet rs:setList) {
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
