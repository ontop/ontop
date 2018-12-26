package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.RDFTermType;

import java.util.Optional;

public interface FunctionSymbolFactory {

    RDFTermFunctionSymbol getRDFTermFunctionSymbol();


    DBFunctionSymbolFactory getDBFunctionSymbolFactory();

    BooleanFunctionSymbol getIsARDFTermTypeFunctionSymbol(RDFTermType rdfTermType);

    /**
     * See https://www.w3.org/TR/sparql11-query/#func-arg-compatibility
     */
    BooleanFunctionSymbol getAreCompatibleRDFStringFunctionSymbol();

    RDFTermTypeFunctionSymbol getRDFTermTypeFunctionSymbol(TypeConstantDictionary dictionary,
            ImmutableSet<RDFTermTypeConstant> possibleConstants);

    // SPARQL functions

    Optional<SPARQLFunctionSymbol> getSPARQLFunctionSymbol(String officialName, int arity);

    FunctionSymbol getCommonDenominatorFunctionSymbol(int arity);
}
