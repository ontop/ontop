package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBArrayObjectAccessFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.impl.ArrayDBTermType;

import java.util.Optional;
import java.util.function.Function;

/**
 * Function symbol for Dremio's Array Access operation `ARRAY[<key: int>]`.
 * Using a typed function symbol that can extract the output types may be advantageous for the future, but
 * Dremio does not give us any such information anyways, so there is no need to implement it for now.
 *
 */
public class DremioArrayAccessDBFunctionSymbol extends FunctionSymbolImpl implements DBArrayObjectAccessFunctionSymbol {

    protected DremioArrayAccessDBFunctionSymbol(DBTermType dbRootType) {
        super("ARRAY_ACCESS", ImmutableList.of(dbRootType, dbRootType));
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format(
                "%s[%s]",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1))
        );
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.empty();
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public boolean isPreferringToBePostProcessedOverBeingBlocked() {
        return false;
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return true;
    }
}
