package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBCoalesceFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfElseNullFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

/**
 * Takes an RDF type term as input.
 * Returns
 * <ul>
 *   <li> NULL if it is not a literal </li>
 *   <li> "" if the literal type does not have a language tag </li>
 *   <li> the language tag if available </li>
 * </ul>  
 */
public class LangTagFunctionSymbolImpl extends AbstractLangTagLikeFunctionSymbol {

    private final LangTagWithPlaceholderFunctionSymbol langTagWithPlaceholder;

    protected LangTagFunctionSymbolImpl(MetaRDFTermType metaRDFTermType, DBTermType dbStringType) {
        super("LANG_TAG", metaRDFTermType, dbStringType);
        this.langTagWithPlaceholder = new LangTagWithPlaceholderFunctionSymbol(metaRDFTermType, dbStringType);
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return true;
    }

    /**
     * Because it can produce nulls without null arguments, it needs a special logic for handling coalesce.
     */
    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableTerm newTerm = newTerms.get(0);
        if (newTerm instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) newTerm;
            FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();

            if (functionSymbol instanceof DBCoalesceFunctionSymbol) {
                ImmutableFunctionalTerm newCoalesceFunctionalTerm = termFactory.getDBCoalesce(
                        functionalTerm.getTerms().stream()
                                .map(t -> termFactory.getImmutableFunctionalTerm(langTagWithPlaceholder, t))
                                .collect(ImmutableCollectors.toList()));

                return termFactory.getIfElseNull(
                        termFactory.getDBNot(
                                termFactory.getStrictEquality(
                                            newCoalesceFunctionalTerm,
                                            termFactory.getDBStringConstant(LangTagWithPlaceholderFunctionSymbol.PLACEHOLDER))),
                                newCoalesceFunctionalTerm)
                        .simplify(variableNullability);
            }
            else if (functionSymbol instanceof DBIfElseNullFunctionSymbol) {
                ImmutableList<? extends ImmutableTerm> subTerms = functionalTerm.getTerms();

                return termFactory.getImmutableFunctionalTerm(functionSymbol,
                        subTerms.get(0),
                        termFactory.getImmutableFunctionalTerm(this, subTerms.get(1)))
                        .simplify(variableNullability);
            }
        }
        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }

    @Override
    protected Constant defaultValueForNonLiteral(TermFactory termFactory) {
        return termFactory.getNullConstant();
    }

}
