package it.unibz.inf.ontop.model.term.functionsymbol;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;

/**
 * Maps RDF term types to the natural DB term for their lexical term
 * (before being cast into a string)
 *
 * Excludes strings
 *
 */
public interface DBFunctionSymbolFactory {

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

    /**
     * arity must be >= 2
     */
    DBConcatFunctionSymbol getDBConcat(int arity);

    /**
     * arity must be >= 2
     */
    DBBooleanFunctionSymbol getDBAnd(int arity);

    DBBooleanFunctionSymbol getDBStrictEquality(int arity);
}
