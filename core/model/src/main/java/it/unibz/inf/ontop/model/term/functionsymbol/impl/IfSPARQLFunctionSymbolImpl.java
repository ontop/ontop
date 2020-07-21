package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

public class IfSPARQLFunctionSymbolImpl extends SPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdBooleanType;

    protected IfSPARQLFunctionSymbolImpl(RDFDatatype xsdBooleanType, RDFTermType rootRDFType) {
        super("SP_IF", SPARQL.IF, ImmutableList.of(xsdBooleanType, rootRDFType, rootRDFType));
        this.xsdBooleanType = xsdBooleanType;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {
        if (newTerms.stream()
                .skip(1)
                .allMatch(t -> (t instanceof Constant) && t.isNull()))
            return termFactory.getNullConstant();

        if (newTerms.stream()
                .allMatch(t -> isRDFFunctionalTerm(t) || (t instanceof Constant))) {
            ImmutableList<ImmutableTerm> typeTerms = newTerms.stream()
                    .map(t -> extractRDFTermTypeTerm(t, termFactory))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<ImmutableTerm> subLexicalTerms = newTerms.stream()
                    .map(t -> extractLexicalTerm(t, termFactory))
                    .collect(ImmutableCollectors.toList());

            DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();
            ImmutableExpression condition = (ImmutableExpression) termFactory.getConversionFromRDFLexical2DB(
                    dbTypeFactory.getDBBooleanType(),
                    subLexicalTerms.get(0),
                    xsdBooleanType);

            ImmutableTerm typeTerm = computeTypeTerm(condition, typeTerms, termFactory, variableNullability);
            ImmutableTerm lexicalTerm = computeLexicalTerm(condition, subLexicalTerms.get(1), subLexicalTerms.get(2),
                    termFactory, variableNullability);

            ImmutableExpression noConditionError = termFactory.getDBIsNotNull(condition);

            return termFactory.getRDFFunctionalTerm(
                    termFactory.getIfElseNull(noConditionError, lexicalTerm),
                    termFactory.getIfElseNull(noConditionError, typeTerm))
                    .simplify(variableNullability);
        }
        else
            return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    protected ImmutableTerm computeLexicalTerm(ImmutableExpression condition, ImmutableTerm thenLexicalTerm,
                                               ImmutableTerm elseLexicalTerm,
                                               TermFactory termFactory, VariableNullability variableNullability) {
        return termFactory.getIfThenElse(condition, thenLexicalTerm, elseLexicalTerm)
                .simplify(variableNullability);
    }

    protected ImmutableTerm computeTypeTerm(ImmutableExpression condition, ImmutableList<ImmutableTerm> typeTerms,
                                            TermFactory termFactory, VariableNullability variableNullability) {
        return termFactory.getIfThenElse(condition, typeTerms.get(1), typeTerms.get(2))
                .simplify(variableNullability);
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.empty();
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
