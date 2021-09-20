package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfElseNullFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.DefaultDBCoalesceFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

/**
 * Takes a RDF type term as input.
 * Returns
 *   * The special value defined by the sub-class if it is not a literal
 *   * "" if the literal type does not have a language tag
 *   * the language tag if available
 */
public abstract class AbstractLangTagLikeFunctionSymbol extends FunctionSymbolImpl {

    private final DBTermType dbStringType;

    protected AbstractLangTagLikeFunctionSymbol(String name, MetaRDFTermType metaRDFTermType, DBTermType dbStringType) {
        super(name, ImmutableList.of(metaRDFTermType));
        this.dbStringType = dbStringType;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(dbStringType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableTerm newTerm = newTerms.get(0);
        if (newTerm instanceof RDFTermTypeConstant) {
            RDFTermType termType = ((RDFTermTypeConstant) newTerm).getRDFTermType();
            return Optional.of(termType)
                    .filter(t -> t instanceof RDFDatatype)
                    .map(t -> ((RDFDatatype) t).getLanguageTag()
                            .map(LanguageTag::getFullString)
                            .orElse(""))
                    .map(s -> (Constant) termFactory.getDBStringConstant(s))
                    .orElseGet(() -> defaultValueForNonLiteral(termFactory));
        }
        else if (newTerm instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) newTerm;
            FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();

            if (functionSymbol instanceof RDFTermTypeFunctionSymbol){
                RDFTermTypeFunctionSymbol rdfTermTypeFunctionSymbol = (RDFTermTypeFunctionSymbol) functionSymbol;

                return rdfTermTypeFunctionSymbol.lift(
                        functionalTerm.getTerms(),
                        c -> Optional.of(c)
                                .map(RDFTermTypeConstant::getRDFTermType)
                                .filter(t -> t instanceof RDFDatatype)
                                .map(t -> ((RDFDatatype) t).getLanguageTag()
                                        .map(LanguageTag::getFullString)
                                        .orElse(""))
                                .map(s -> (ImmutableTerm) termFactory.getDBStringConstant(s))
                                .orElseGet(termFactory::getNullConstant),
                        termFactory);
            }
        }
        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }

    protected abstract Constant defaultValueForNonLiteral(TermFactory termFactory);

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

}
