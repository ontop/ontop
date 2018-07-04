package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCSolutionMappingSet extends AbstractTupleResultSet implements TupleResultSet {

    private final ImmutableList<Variable> SQLSignature;
    private final SQLConstantRetriever constantRetriever;
    private final ImmutableSubstitution<ImmutableFunctionalTerm> substitution;

    public JDBCSolutionMappingSet(ResultSet rs, ImmutableList<Variable> SQLSignature,
                                  ConstructionNode constructionNode,
                                  TermFactory termFactory,
                                  SubstitutionFactory substitutionFactory) {
        super(rs, SQLSignature);
        ImmutableSubstitution inputSubstitution = constructionNode.getSubstitution();
        this.SQLSignature = SQLSignature;
        this.substitution = normalizeSubstitution(inputSubstitution);
        constantRetriever = new SQLConstantRetriever(substitution, SQLSignature, termFactory, substitutionFactory);
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

    /* Renames variables appearing in multiple terms of the substitution */
    private ImmutableSubstitution normalizeSubstitution(ImmutableSubstitution<ImmutableTerm> substitution){

    }

    private ImmutableMap<Variable,Integer> computeVar2SQLIndexMap(ImmutableSubstitution inputSubstitution) {

    }

}
