package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfElseNullFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class DefaultBooleanDenormFunctionSymbol
        extends AbstractDBTypeConversionFunctionSymbolImpl implements DBBooleanFunctionSymbol {

    private final DBTermType dbStringType;

    protected DefaultBooleanDenormFunctionSymbol(DBTermType booleanType, DBTermType dbStringType) {
        super("booleanLexicalDenorm", dbStringType, booleanType);
        this.dbStringType = dbStringType;
    }

    @Override
    public Optional<DBTermType> getInputType() {
        return Optional.of(dbStringType);
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    /**
     * "0" and "false" are equivalent lexical terms, "1" and "true" are also equivalent
     */
    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected DBConstant convertDBConstant(DBConstant constant, TermFactory termFactory) {
        ImmutableTerm newTerm = transformIntoDBCase(constant, termFactory)
                .simplify();
        if (newTerm instanceof DBConstant)
            return (DBConstant) newTerm;

        throw new DBTypeConversionException("Problem while de-normalizing " + constant + "(value: " + newTerm + ")");
    }

    protected ImmutableTerm buildTermFromFunctionalTerm(ImmutableFunctionalTerm subTerm, TermFactory termFactory,
                                                        VariableNullability variableNullability) {
        FunctionSymbol subFunctionSymbol = subTerm.getFunctionSymbol();
        // TODO: use an interface
        if (subFunctionSymbol instanceof DefaultBooleanNormFunctionSymbol) {
            return subTerm.getTerm(0);
        }
        else if (subFunctionSymbol instanceof DBIfElseNullFunctionSymbol)
            return ((DBIfElseNullFunctionSymbol) subFunctionSymbol).liftUnaryBooleanFunctionSymbol(
                    subTerm.getTerms(), this, termFactory)
                    .simplify(variableNullability);
        return termFactory.getImmutableFunctionalTerm(this, ImmutableList.of(subTerm));
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        ImmutableFunctionalTerm newFunctionalTerm = transformIntoDBCase(terms.get(0), termFactory);
        return termConverter.apply(newFunctionalTerm);
    }

    protected ImmutableFunctionalTerm transformIntoDBCase(ImmutableTerm subTerm, TermFactory termFactory) {
        return termFactory.getDBCase(Stream.of(
                buildEntry(subTerm, termFactory.getXsdBooleanLexicalConstant(true), true, termFactory),
                buildEntry(subTerm, termFactory.getDBStringConstant("1"), true, termFactory),
                buildEntry(subTerm, termFactory.getXsdBooleanLexicalConstant(false), false, termFactory),
                buildEntry(subTerm, termFactory.getDBStringConstant("0"), false, termFactory)),
                // TODO: unclear if it should return NULL
                termFactory.getNullConstant(),
                false);
    }


    private Map.Entry<ImmutableExpression, ? extends ImmutableTerm> buildEntry(ImmutableTerm term, DBConstant lexicalConstant,
                                                                               boolean b,
                                                                               TermFactory termFactory) {
        return Maps.immutableEntry(termFactory.getStrictEquality(term, lexicalConstant),
                termFactory.getDBBooleanConstant(b));
    }
}
