package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLTupleResultSet extends AbstractSQLTupleResultSet implements TupleResultSet {

    private final SQLConstantRetriever constantRetriever;
    private final ImmutableMap<Variable, Integer> var2SQLIndexMap;
    private final ImmutableSubstitution substitution;

    public SQLTupleResultSet(ResultSet rs, ImmutableList<Variable> signature,
                             ConstructionNode constructionNode,
                             TermFactory termFactory,
                             SubstitutionFactory substitutionFactory) {
        super(rs, signature);
        ImmutableSubstitution inputSubstitution = constructionNode.getSubstitution();
        var2SQLIndexMap = computeVar2SQLIndexMap(inputSubstitution);
        substitution = normalizeSubstitution(inputSubstitution, var2SQLIndexMap);
        constantRetriever = new SQLConstantRetriever(substitution, var2SQLIndexMap, termFactory, substitutionFactory);
    }

    @Override
    protected SQLOntopBindingSet readCurrentRow() throws OntopConnectionException {

        //builder (+loop) in order to throw checked exception
        final ImmutableList.Builder<String> builder = ImmutableList.builder();
        try {
            for (int i = 1; i <= getColumnCount(); i++) {
                builder.add(String.valueOf(rs.getObject(i)));
            }
        } catch (SQLException e) {
            throw new OntopConnectionException(e);
        }
        return new SQLOntopBindingSet(builder.build(), signature, constantRetriever, substitution);
    }

    private ImmutableSubstitution normalizeSubstitution(ImmutableSubstitution<ImmutableTerm> substitution,
                                                        ImmutableMap<Variable, Integer> var2SQLIndexMap) {

    }

    private ImmutableMap<Variable,Integer> computeVar2SQLIndexMap(ImmutableSubstitution inputSubstitution) {
    }

}
