package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;
import java.util.function.Function;


public class UnaryNumericSPARQLFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct;

    protected UnaryNumericSPARQLFunctionSymbolImpl(String officialName,
                                                   RDFDatatype abstractNumericType,
                                                   Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct) {
        super("SP_" + officialName, officialName, ImmutableList.of(abstractNumericType));
        this.dbFunctionSymbolFct = dbFunctionSymbolFct;
    }

    protected UnaryNumericSPARQLFunctionSymbolImpl(String functionSymbolName, IRI functionIRI,
                                                   RDFDatatype abstractNumericType,
                                                   Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct) {
        super(functionSymbolName, functionIRI, ImmutableList.of(abstractNumericType));
        this.dbFunctionSymbolFct = dbFunctionSymbolFct;
    }

    /**
     * Makes sure sub-types of xsd:integer are replaced by xsd:integer
     */
    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms,
                                            ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        ImmutableTerm typeTerm = typeTerms.get(0);
        return termFactory.getCommonPropagatedOrSubstitutedNumericType(typeTerm, typeTerm)
                .simplify(variableNullability);
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                               ImmutableTerm returnedRDFTypeTerm) {
        return termFactory.getUnaryLexicalFunctionalTerm(subLexicalTerms.get(0), returnedRDFTypeTerm,
                dbFunctionSymbolFct);
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    /**
     * Too complex logic so not infer at this level (but after simplification into DB functional terms)
     */
    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.empty();
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
