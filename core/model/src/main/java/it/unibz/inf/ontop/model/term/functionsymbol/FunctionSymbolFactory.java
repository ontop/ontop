package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;
import java.util.function.Function;

/**
 * Accessible through Guice (recommended) or through CoreSingletons.
 */
public interface FunctionSymbolFactory {

    RDFTermFunctionSymbol getRDFTermFunctionSymbol();

    DBFunctionSymbolFactory getDBFunctionSymbolFactory();

    BooleanFunctionSymbol getIsARDFTermTypeFunctionSymbol(RDFTermType rdfTermType);

    /**
     * See https://www.w3.org/TR/sparql11-query/#func-arg-compatibility
     */
    BooleanFunctionSymbol getAreCompatibleRDFStringFunctionSymbol();

    BooleanFunctionSymbol getLexicalNonStrictEqualityFunctionSymbol();

    /**
     * To be used when parsing the mapping and when an equality is found.
     * Is expected to replaced later by a proper equality (may be strict or not)
     */
    NotYetTypedEqualityFunctionSymbol getNotYetTypedEquality();

    BooleanFunctionSymbol getLexicalInequalityFunctionSymbol(InequalityLabel inequalityLabel);

    BooleanFunctionSymbol getLexicalEBVFunctionSymbol();

    /**
     * Used for wrapping SPARQL boolean functional terms to make them becoming ImmutableExpressions
     *
     * Such a wrapping consists in converting XSD booleans into DB booleans.
     */
    BooleanFunctionSymbol getRDF2DBBooleanFunctionSymbol();

    RDFTermTypeFunctionSymbol getRDFTermTypeFunctionSymbol(TypeConstantDictionary dictionary,
                                                           ImmutableSet<RDFTermTypeConstant> possibleConstants,
                                                           boolean isSimplifiable);

    // SPARQL functions

    Optional<SPARQLFunctionSymbol> getSPARQLFunctionSymbol(String officialName, int arity);

    default SPARQLFunctionSymbol getRequiredSPARQLFunctionSymbol(String officialName, int arity) {
        return getSPARQLFunctionSymbol(officialName, arity)
                .orElseThrow(() -> new IllegalArgumentException("The SPARQL function " + officialName
                        + " is not available for the arity " + arity));
    }

    Optional<SPARQLFunctionSymbol> getSPARQLDistinctAggregateFunctionSymbol(String officialName, int arity);

    default SPARQLFunctionSymbol getRequiredSPARQLDistinctAggregateFunctionSymbol(String officialName, int arity){
        return getSPARQLDistinctAggregateFunctionSymbol(officialName, arity)
                .orElseThrow(() -> new IllegalArgumentException("The SPARQL distinct aggregate function " + officialName
                        + " with arity " + arity + " is not available"));
    }

    SPARQLAggregationFunctionSymbol getSPARQLGroupConcatFunctionSymbol(String separator, boolean isDistinct);

    SPARQLFunctionSymbol getIRIFunctionSymbol(IRI baseIRI);
    SPARQLFunctionSymbol getIRIFunctionSymbol();

    /**
     * Special function capturing the EBV logic
     * https://www.w3.org/TR/sparql11-query/#ebv
     *
     * Returns an XSD.BOOLEAN
     */
    FunctionSymbol getSPARQLEffectiveBooleanValueFunctionSymbol();

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
     * This function symbol takes an RDF type term as input.
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

    FunctionSymbol getUnaryLatelyTypedFunctionSymbol(Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct,
                                                     DBTermType targetType);

    FunctionSymbol getUnaryLexicalFunctionSymbol(Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct);

    FunctionSymbol getBinaryLatelyTypedFunctionSymbol(Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct,
                                                      DBTermType targetType);
}
