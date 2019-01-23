package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;

import java.util.Optional;
import java.util.function.Function;

public interface FunctionSymbolFactory {

    RDFTermFunctionSymbol getRDFTermFunctionSymbol();


    DBFunctionSymbolFactory getDBFunctionSymbolFactory();

    BooleanFunctionSymbol getIsARDFTermTypeFunctionSymbol(RDFTermType rdfTermType);

    /**
     * See https://www.w3.org/TR/sparql11-query/#func-arg-compatibility
     */
    BooleanFunctionSymbol getAreCompatibleRDFStringFunctionSymbol();

    /**
     * Used for wrapping SPARQL boolean functional terms to make them becoming ImmutableExpressions
     *
     * Such a wrapping consists in converting XSD booleans into DB booleans.
     */
    BooleanFunctionSymbol getRDF2DBBooleanFunctionSymbol();

    RDFTermTypeFunctionSymbol getRDFTermTypeFunctionSymbol(TypeConstantDictionary dictionary,
            ImmutableSet<RDFTermTypeConstant> possibleConstants);

    // SPARQL functions

    Optional<SPARQLFunctionSymbol> getSPARQLFunctionSymbol(String officialName, int arity);

    FunctionSymbol getCommonDenominatorFunctionSymbol(int arity);

    /**
     * Binary
     */
    FunctionSymbol getCommonPropagatedOrSubstitutedNumericTypeFunctionSymbol();

    /**
     * Do NOT confuse it with the LANG SPARQL function
     *
     * This function symbol takes a RDF type term as input.
     * and returns
     *   * NULL if it is not a literal
     *   * "" if the literal type does not have a language tag
     *   * the language tag if available
     */
    FunctionSymbol getLangTagFunctionSymbol();

    /**
     * Do NOT confuse it with the LANG DATATYPE function
     *
     * This function symbol takes a RDF type term as input.
     * and returns
     *   * NULL if it is not a literal
     *   * the string of the datatype IRI
     */
    FunctionSymbol getRDFDatatypeStringFunctionSymbol();

    /**
     * Do NOT confuse it with the langMatches SPARQL function
     *
     * Not a DBFunctionSymbol as it is not delegated to the DB (too complex logic)
     */
    BooleanFunctionSymbol getLexicalLangMatches();

    FunctionSymbol getBinaryNumericLexicalFunctionSymbol(String dbNumericOperationName);
}
