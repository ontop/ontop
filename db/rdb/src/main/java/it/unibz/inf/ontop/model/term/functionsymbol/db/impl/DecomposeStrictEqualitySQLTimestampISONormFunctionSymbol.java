package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

/**
 * Assumes that a strict equality over the lexical value can be decomposed into a strict equality of the DB timestamps.
 *
 */
public class DecomposeStrictEqualitySQLTimestampISONormFunctionSymbol extends AbstractTimestampISONormFunctionSymbol {

    protected DecomposeStrictEqualitySQLTimestampISONormFunctionSymbol(DBTermType timestampType, DBTermType dbStringType,
                                                                       DBFunctionSymbolSerializer serializer) {
        super(timestampType, dbStringType, serializer);
    }

    /**
     * TODO: try to return a constant
     */
    @Override
    protected ImmutableTerm convertDBConstant(DBConstant constant, TermFactory termFactory) {
        return termFactory.getImmutableFunctionalTerm(this, constant);
    }

    @Override
    protected IncrementalEvaluation evaluateStringEqWithValidDatetimeConstantString(ImmutableTerm subTerm,
                                                                                    NonNullConstant otherTerm,
                                                                                    TermFactory termFactory,
                                                                                    VariableNullability variableNullability) {
        return termFactory.getStrictEquality(
                subTerm,
                termFactory.getConversionFromRDFLexical2DB(timestampType, otherTerm))
                .evaluate(variableNullability, true);
    }
}
