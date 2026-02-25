package it.unibz.inf.ontop.model.term.functionsymbol.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.AbstractTypedDBFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;

import java.util.function.Function;

/**
 * For internal usage only!
 * Not made available to SPARQL users, as it would leak blank node labels (which may contain sensitive identifiers)
 */
public class BNodeTolerantStrSPARQLFunctionSymbolImpl extends AbstractStrSPARQLFunctionSymbolImpl {

    private static final String NAME = "INTERNAL_SP_BNODE_TOLERANT_STR";
    // Lazy
    private DBFunctionSymbol convertBNodeStringTemplateFunctionSymbol;

    protected BNodeTolerantStrSPARQLFunctionSymbolImpl(RDFTermType abstractRDFType, RDFDatatype xsdStringType) {
        super(NAME, NAME, abstractRDFType, xsdStringType);
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                               ImmutableTerm returnedTypeTerm) {
        return termFactory.getImmutableFunctionalTerm(
                getBNodeStringTemplateFunctionSymbol(termFactory),
                subLexicalTerms.get(0));
    }

    private synchronized DBFunctionSymbol getBNodeStringTemplateFunctionSymbol(TermFactory termFactory) {
        if (convertBNodeStringTemplateFunctionSymbol == null) {
            convertBNodeStringTemplateFunctionSymbol = new ConvertBnodeTemplateIntoIRITemplateFunctionSymbol(
                    termFactory.getTypeFactory().getDBTypeFactory().getDBStringType());
        }
        return convertBNodeStringTemplateFunctionSymbol;
    }


    /**
     * Hack: does produce valid IRIs. But good enough for providing the string template
     */
    protected static class ConvertBnodeTemplateIntoIRITemplateFunctionSymbol extends AbstractTypedDBFunctionSymbol {

        protected ConvertBnodeTemplateIntoIRITemplateFunctionSymbol(DBTermType dbStringType) {
            super("CONVERT_INTO_IRI_TEMPLATE", ImmutableList.of(dbStringType), dbStringType);
        }

        @Override
        public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
            return true;
        }

        @Override
        public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
            return true;
        }

        @Override
        public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
            return termConverter.apply(terms.get(0));
        }

        @Override
        protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                         TermFactory termFactory, VariableNullability variableNullability) {
            var term = newTerms.get(0);
            if (term instanceof DBConstant)
                return term;
            if (term instanceof ImmutableFunctionalTerm) {
                var functionalTerm = (ImmutableFunctionalTerm) term;
                var functionSymbol = functionalTerm.getFunctionSymbol();
                if (functionSymbol instanceof BnodeStringTemplateFunctionSymbol) {
                    // Hack! Not producing valid IRIs (but good enough for the current use case)
                    var iriTemplate = termFactory.getDBFunctionSymbolFactory()
                            .getIRIStringTemplateFunctionSymbol(
                                    ((BnodeStringTemplateFunctionSymbol) functionSymbol).getTemplateComponents());
                    return termFactory.getImmutableFunctionalTerm(iriTemplate, functionalTerm.getTerms());
                }
                else if ((functionSymbol instanceof IRIStringTemplateFunctionSymbol)
                        || (functionSymbol instanceof DBConcatFunctionSymbol)) {
                    return functionalTerm;
                }
            }
            return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
        }

    }
}
