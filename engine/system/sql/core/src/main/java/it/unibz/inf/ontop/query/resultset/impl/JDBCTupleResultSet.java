package it.unibz.inf.ontop.query.resultset.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.query.resultset.OntopBinding;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class JDBCTupleResultSet extends AbstractTupleResultSet {

    private final ImmutableSortedSet<Variable> sqlSignature;
    private final ImmutableMap<Variable, DBTermType> sqlTypeMap;
    private final ImmutableSubstitution<ImmutableTerm> sparqlVar2Term;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    public JDBCTupleResultSet(ResultSet rs,
                              ImmutableSortedSet<Variable> sqlSignature,
                              ImmutableMap<Variable, DBTermType> sqlTypeMap,
                              ConstructionNode constructionNode,
                              DistinctVariableOnlyDataAtom answerAtom, QueryLogger queryLogger,
                              @Nullable OntopConnectionCloseable statementClosingCB,
                              TermFactory termFactory,
                              SubstitutionFactory substitutionFactory) {
        super(rs, answerAtom.getArguments(), queryLogger, statementClosingCB);
        this.sqlSignature = sqlSignature;
        this.sqlTypeMap = sqlTypeMap;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.sparqlVar2Term = constructionNode.getSubstitution();
    }


    @Override
    protected SQLOntopBindingSet readCurrentRow() throws OntopConnectionException, OntopResultConversionException {
        //builder (+loop) in order to throw checked exception
        final ImmutableMap.Builder<Variable, Constant> builder = ImmutableMap.builder();
        try {
            int index = 1;
            for (Variable var : sqlSignature) {
                builder.put(
                        var,
                        convertToConstant(
                            rs.getString(index),
                            sqlTypeMap.get(var)
                        ));
                index++;
            }
        } catch (SQLException e) {
            throw buildConnectionException(e);
        }
        try {
            return new SQLOntopBindingSet(computeBindingMap(substitutionFactory.getSubstitution(builder.build())));
        } catch (Exception e) {
            throw new OntopResultConversionException(e);
        }
    }

    private Constant convertToConstant(@Nullable String jdbcValue, DBTermType termType) {
        if (jdbcValue == null)
            return termFactory.getNullConstant();
        return termFactory.getDBConstant(jdbcValue, termType);
    }

    private OntopBinding[] computeBindingMap(ImmutableSubstitution<Constant> sqlVar2Constant) {
        //this can be improved and simplified
        return signature.stream()
                       .map(v -> getBinding(v, sqlVar2Constant))
                       .filter(Optional::isPresent)
                       .map(Optional::get)
                       .toArray(OntopBinding[]::new);
    }

    private Optional<OntopBinding> getBinding(Variable v, ImmutableSubstitution<Constant> sqlVar2Constant) {
        ImmutableTerm term = sparqlVar2Term.applyToVariable(v);
        ImmutableTerm constantTerm = sqlVar2Constant.apply(term);
        Optional<RDFConstant> constant = evaluate(constantTerm);
        return constant.map(rdfConstant -> new OntopBindingImpl(v, rdfConstant));
    }

    private Optional<RDFConstant> evaluate(ImmutableTerm term) {
        ImmutableTerm simplifiedTerm = term.simplify();
        if (simplifiedTerm instanceof Constant) {
            if (simplifiedTerm instanceof RDFConstant) {
                return Optional.of((RDFConstant) simplifiedTerm);
            }
            Constant constant = (Constant) simplifiedTerm;
            if (constant.isNull()) {
                return Optional.empty();
            }
            if (constant instanceof DBConstant) {
                throw new SQLOntopBindingSet.InvalidConstantTypeInResultException(
                        constant + " is a DB constant. But a binding cannot have a DB constant as value");
            }
            throw new SQLOntopBindingSet.InvalidConstantTypeInResultException("Unexpected constant type for " + constant);
        }
        throw new SQLOntopBindingSet.InvalidTermAsResultException(simplifiedTerm);
    }
}
