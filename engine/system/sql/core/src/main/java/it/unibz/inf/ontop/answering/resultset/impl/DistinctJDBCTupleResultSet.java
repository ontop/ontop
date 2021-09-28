package it.unibz.inf.ontop.answering.resultset.impl;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Class to handle distinct in SPARQL query. Avoid returning duplicate rows.
 * See test case DistinctResultSetTest
 */

@Deprecated
public class DistinctJDBCTupleResultSet extends JDBCTupleResultSet implements TupleResultSet {

    private final Set<List<Object>> rowKeys = new HashSet<>();
    private final ImmutableSortedSet<Variable> sqlSignature;

    public DistinctJDBCTupleResultSet(ResultSet rs, ImmutableSortedSet<Variable> sqlSignature, ImmutableMap<Variable, DBTermType> sqlTypes,
                                      ConstructionNode constructionNode,
                                      DistinctVariableOnlyDataAtom answerAtom, QueryLogger queryLogger,
                                      @Nullable OntopConnectionCloseable statementClosingCB, TermFactory termFactory,
                                      SubstitutionFactory substitutionFactory) {

        super(rs, sqlSignature, sqlTypes, constructionNode, answerAtom, queryLogger, statementClosingCB, termFactory, substitutionFactory);
        this.sqlSignature = sqlSignature;
    }

    /**
     * Moves cursor until we get a fresh row
     */
    @Override
    protected boolean moveCursor() throws SQLException, OntopConnectionException {
        boolean foundFreshTuple;
        List<Object> currentKey;
        do{
           foundFreshTuple = rs.next();
           // Cannot use this in the while condition: limit case where the last row was a duplicate
           if(!foundFreshTuple) {
               break;
           }
           currentKey = computeTupleKey();
        } while(!rowKeys.add(currentKey));

        return foundFreshTuple;
    }

    private List<Object> computeTupleKey() throws OntopConnectionException {
        try {
            ArrayList<Object> rowKey = new ArrayList<>(sqlSignature.size());
            for (int i = 1; i <= sqlSignature.size(); i++) {
                rowKey.add(rs.getObject(i)); //value
            }
            return rowKey;
        }
        catch (Exception e) {
            throw buildConnectionException(e);
        }
    }

}
