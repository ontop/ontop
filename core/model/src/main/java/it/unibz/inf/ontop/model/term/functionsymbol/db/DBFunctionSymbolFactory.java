package it.unibz.inf.ontop.model.term.functionsymbol.db;

import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;

import java.util.UUID;


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
     * From a possibly "normalized" DB string to another DB type
     */
    DBTypeConversionFunctionSymbol getConversionFromRDFLexical2DBFunctionSymbol(DBTermType targetDBType, RDFTermType rdfType);


    /**
     * Not for special DB function symbols such as casts.
     */
    DBFunctionSymbol getRegularDBFunctionSymbol(String nameInDialect, int arity);

    DBBooleanFunctionSymbol getRegularDBBooleanFunctionSymbol(String nameInDialect, int arity);

    /**
     * IF THEN, ELSE IF ..., ELSE
     *
     * Arity must be odd and >= 3
     */
    DBFunctionSymbol getDBCase(int arity);

    DBIfElseNullFunctionSymbol getDBIfElseNull();

    DBBooleanFunctionSymbol getDBBooleanIfElseNull();

    DBFunctionSymbol getDBIfThenElse();

    DBFunctionSymbol getDBUpper();

    DBFunctionSymbol getDBLower();

    DBFunctionSymbol getDBReplace();

    DBFunctionSymbol getDBRegexpReplace3();

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
    DBAndFunctionSymbol getDBAnd(int arity);

    /**
     * arity must be >= 2
     */
    DBOrFunctionSymbol getDBOr(int arity);

    DBNotFunctionSymbol getDBNot();

    DBIsNullOrNotFunctionSymbol getDBIsNull();
    DBIsNullOrNotFunctionSymbol getDBIsNotNull();

    FalseOrNullFunctionSymbol getFalseOrNullFunctionSymbol(int arity);

    TrueOrNullFunctionSymbol getTrueOrNullFunctionSymbol(int arity);

    DBStrictEqFunctionSymbol getDBStrictEquality(int arity);

    DBBooleanFunctionSymbol getDBStrictNEquality(int arity);

    DBBooleanFunctionSymbol getDBNonStrictNumericEquality();
    DBBooleanFunctionSymbol getDBNonStrictStringEquality();
    DBBooleanFunctionSymbol getDBNonStrictDatetimeEquality();
    DBBooleanFunctionSymbol getDBNonStrictDefaultEquality();

    DBBooleanFunctionSymbol getDBNumericInequality(InequalityLabel inequalityLabel);
    DBBooleanFunctionSymbol getDBBooleanInequality(InequalityLabel inequalityLabel);
    DBBooleanFunctionSymbol getDBStringInequality(InequalityLabel inequalityLabel);
    DBBooleanFunctionSymbol getDBDatetimeInequality(InequalityLabel inequalityLabel);
    DBBooleanFunctionSymbol getDBDefaultInequality(InequalityLabel inequalityLabel);

    DBBooleanFunctionSymbol getDBStartsWith();

    DBBooleanFunctionSymbol getDBEndsWith();

    DBBooleanFunctionSymbol getDBIsStringEmpty();

    DBIsTrueFunctionSymbol getIsTrue();

    /**
     * Arity 2, first argument is the string in which to search, second argument is subString
     */
    DBBooleanFunctionSymbol getDBContains();

    NonDeterministicDBFunctionSymbol getDBRand(UUID uuid);
    NonDeterministicDBFunctionSymbol getDBUUID(UUID uuid);

    DBBooleanFunctionSymbol getDBRegexpMatches2();
    DBBooleanFunctionSymbol getDBRegexpMatches3();

    DBBooleanFunctionSymbol getDBLike();

    DBFunctionSymbol getDBStrBefore();
    DBFunctionSymbol getDBStrAfter();

    DBFunctionSymbol getDBMd5();
    DBFunctionSymbol getDBSha1();
    DBFunctionSymbol getDBSha256();
    DBFunctionSymbol getDBSha512();

    DBMathBinaryOperator getDBMathBinaryOperator(String dbMathOperatorName, DBTermType dbNumericType);

    /**
     * Please use getDBMathBinaryOperator(...) if you know the type
     */
    DBMathBinaryOperator getUntypedDBMathBinaryOperator(String dbMathOperatorName);

    DBFunctionSymbol getAbs(DBTermType dbTermType);
    DBFunctionSymbol getCeil(DBTermType dbTermType);
    DBFunctionSymbol getFloor(DBTermType dbTermType);
    DBFunctionSymbol getRound(DBTermType dbTermType);

    DBFunctionSymbol getDBYear();
    DBFunctionSymbol getDBMonth();
    DBFunctionSymbol getDBDay();
    DBFunctionSymbol getDBHours();
    DBFunctionSymbol getDBMinutes();
    DBFunctionSymbol getDBSeconds();
    DBFunctionSymbol getDBTz();
    DBFunctionSymbol getNow();
}
