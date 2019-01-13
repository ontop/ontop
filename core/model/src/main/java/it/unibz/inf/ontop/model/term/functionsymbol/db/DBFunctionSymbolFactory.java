package it.unibz.inf.ontop.model.term.functionsymbol.db;

import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import org.mapdb.DB;

/**
 * Factory for DBFunctionSymbols
 */
public interface DBFunctionSymbolFactory {

    /**
     * NB: a functional term using this symbol is producing a NULL or a DB string
     */
    IRIStringTemplateFunctionSymbol getIRIStringTemplateFunctionSymbol(String iriTemplate);

    /**
     * NB: a functional term using this symbol is producing a NULL or a DB string
     */
    BnodeStringTemplateFunctionSymbol getBnodeStringTemplateFunctionSymbol(String bnodeTemplate);

    /**
     * Returns a fresh Bnode template
     */
    BnodeStringTemplateFunctionSymbol getFreshBnodeStringTemplateFunctionSymbol(int arity);

    /**
     * Temporary conversion function for the lexical part of an RDF term.
     *
     * ONLY for pre-processed mapping assertions
     * (TEMPORARY usage, to be replaced later on in the process by a fully defined cast function)
     *
     */
    DBTypeConversionFunctionSymbol getTemporaryConversionToDBStringFunctionSymbol();

    DBTypeConversionFunctionSymbol getDBCastFunctionSymbol(DBTermType targetType);
    DBTypeConversionFunctionSymbol getDBCastFunctionSymbol(DBTermType inputType, DBTermType targetType);

    /**
     * The output type is a DB string.
     *
     * This function symbol MAY also perform some normalization.
     *
     */
    DBTypeConversionFunctionSymbol getConversion2RDFLexicalFunctionSymbol(DBTermType inputType, RDFTermType rdfTermType);

    /**
     * Not for special DB function symbols such as casts.
     */
    DBFunctionSymbol getRegularDBFunctionSymbol(String nameInDialect, int arity);

    /**
     * IF THEN, ELSE IF ..., ELSE
     *
     * Arity must be odd and >= 3
     */
    DBFunctionSymbol getDBCase(int arity);

    DBFunctionSymbol getDBIfElseNull();

    DBFunctionSymbol getDBUpper();

    DBFunctionSymbol getDBLower();

    DBFunctionSymbol getDBReplace3();

    DBFunctionSymbol getDBRegexpReplace4();

    DBFunctionSymbol getDBSubString2();

    DBFunctionSymbol getDBSubString3();

    DBFunctionSymbol getDBRight();

    DBFunctionSymbol getDBCharLength();

    DBFunctionSymbol getR2RMLIRISafeEncode();

    /**
     * arity must be >= 2
     */
    DBConcatFunctionSymbol getDBConcat(int arity);

    /**
     * arity must be >= 2
     */
    DBBooleanFunctionSymbol getDBAnd(int arity);

    FalseOrNullFunctionSymbol getFalseOrNullFunctionSymbol(int arity);

    TrueOrNullFunctionSymbol getTrueOrNullFunctionSymbol(int arity);

    DBStrictEqFunctionSymbol getDBStrictEquality(int arity);

    DBBooleanFunctionSymbol getDBStrictNEquality(int arity);

    DBBooleanFunctionSymbol getDBStartsWith();

    DBBooleanFunctionSymbol getDBEndsWith();

    /**
     * Arity 2, first argument is the string in which to search, second argument is subString
     */
    DBBooleanFunctionSymbol getDBContains();

    DBFunctionSymbol getDBUUIDFunctionSymbol();

    DBBooleanFunctionSymbol getDBRegexpMatches2();
    DBBooleanFunctionSymbol getDBRegexpMatches3();
}
