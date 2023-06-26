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
import it.unibz.inf.ontop.substitution.*;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class JDBCTupleResultSet extends AbstractTupleResultSet {

    private final ImmutableMap<Integer, Variable> indexedSqlSignature;
    private final ImmutableMap<Variable, DBTermType> sqlTypeMap;
    private final Substitution<ImmutableTerm> sparqlVar2Term;
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
        ImmutableMap.Builder<Integer, Variable> indexedSqlSignatureBuilder = ImmutableMap.builder();
        int index = 1;
        for (Variable v : sqlSignature) {
            indexedSqlSignatureBuilder.put(index, v);
            index++;
        }
        this.indexedSqlSignature = indexedSqlSignatureBuilder.build();
        this.sqlTypeMap = sqlTypeMap;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.sparqlVar2Term = constructionNode.getSubstitution();
    }


    @Override
    protected SQLOntopBindingSet readCurrentRow() throws OntopConnectionException, OntopResultConversionException {
        Substitution<Constant> substitution;
        try {
            substitution = substitutionFactory.getSubstitutionThrowsExceptions(
                    indexedSqlSignature.entrySet(),
                    Map.Entry::getValue,
                    e -> convertToConstant(rs.getString(e.getKey()), sqlTypeMap.get(e.getValue())));
        }
        catch (SQLException e) {
            throw buildConnectionException(e);
        }
        try {
            return new SQLOntopBindingSet(computeBindingMap(substitution));
        }
        catch (Exception e) {
            throw new OntopResultConversionException(e);
        }
    }

    private Constant convertToConstant(@Nullable String jdbcValue, DBTermType termType) {
        if (jdbcValue == null)
            return termFactory.getNullConstant();
        return termFactory.getDBConstant(jdbcValue, termType);
    }

    private OntopBinding[] computeBindingMap(Substitution<Constant> sqlVar2Constant) {
        //this can be improved and simplified
        return signature.stream()
                       .map(v -> getBinding(v, sqlVar2Constant))
                       .filter(Optional::isPresent)
                       .map(Optional::get)
                       .toArray(OntopBinding[]::new);
    }

    private Optional<OntopBinding> getBinding(Variable v, Substitution<Constant> sqlVar2Constant) {
        ImmutableTerm term = sparqlVar2Term.apply(v);
        ImmutableTerm constantTerm = sqlVar2Constant.applyToTerm(term);
        Optional<RDFConstant> constant = evaluate(constantTerm);
        return constant.map(rdfConstant -> new OntopBindingImpl(v, rdfConstant));
    }


    private Optional<RDFConstant> evaluate(ImmutableTerm term) {
        ImmutableTerm simplifiedTerm = term.simplify();
        if (simplifiedTerm instanceof RDFConstant) {
            return Optional.of((RDFConstant) simplifiedTerm);
        }
        else if (simplifiedTerm.isNull()) {
            return Optional.empty();
        }
        else if (simplifiedTerm instanceof DBConstant) {
            throw new SQLOntopBindingSet.InvalidConstantTypeInResultException(
                    simplifiedTerm + " is a DB constant. But a binding cannot have a DB constant as value");
        }
        else if (simplifiedTerm instanceof Constant) {
            throw new SQLOntopBindingSet.InvalidConstantTypeInResultException("Unexpected constant type for " + simplifiedTerm);
        }
        throw new SQLOntopBindingSet.InvalidTermAsResultException(simplifiedTerm);
    }
}
