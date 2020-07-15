package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class JDBCTupleResultSet extends AbstractTupleResultSet implements TupleResultSet {

    private final ImmutableSortedSet<Variable> sqlSignature;
    private final ImmutableMap<Variable, DBTermType> sqlTypeMap;
    private final ImmutableSubstitution<ImmutableTerm> sparqlVar2Term;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    public JDBCTupleResultSet(ResultSet rs,
                              ImmutableSortedSet<Variable> sqlSignature,
                              ImmutableMap<Variable, DBTermType> sqlTypeMap,
                              ConstructionNode constructionNode,
                              DistinctVariableOnlyDataAtom answerAtom,
                              QueryLogger queryLogger, TermFactory termFactory,
                              SubstitutionFactory substitutionFactory) {
        super(rs, answerAtom.getArguments(),queryLogger);
        this.sqlSignature = sqlSignature;
        this.sqlTypeMap = sqlTypeMap;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.sparqlVar2Term = constructionNode.getSubstitution();
    }


    @Override
    protected SQLOntopBindingSet readCurrentRow() throws OntopConnectionException {

        //builder (+loop) in order to throw checked exception
        final ImmutableMap.Builder<Variable, Constant> builder = ImmutableMap.builder();
        Iterator<Variable> it = sqlSignature.iterator();
        try {
            for (int i = 1; i <= sqlSignature.size(); i++) {
                Variable var = it.next();
                builder.put(
                        var,
                        convertToConstant(
                            rs.getString(i),
                            sqlTypeMap.get(var)
                        ));
            }
        } catch (SQLException e) {
            throw buildConnectionException(e);
        }
        return new SQLOntopBindingSet(
                signature,
                substitutionFactory.getSubstitution(builder.build()),
                sparqlVar2Term
        );
    }

    private Constant convertToConstant(@Nullable String jdbcValue, DBTermType termType) {
        if (jdbcValue == null)
            return termFactory.getNullConstant();
        return termFactory.getDBConstant(jdbcValue, termType);
    }
}
