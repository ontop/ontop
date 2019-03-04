package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Function symbols of each we don't know the return type.
 *
 * By default, it is treated as non-injective and non-postprocessable.
 *
 * This class is typically used for not recognized DB functions (e.g. when parsing the mapping)
 *
 */
public class AbstractUntypedDBFunctionSymbol extends FunctionSymbolImpl implements DBFunctionSymbol {

    private static final String FUNCTIONAL_TEMPLATE = "%s(%s)";

    @Nonnull
    private final String nameInDialect;

    protected AbstractUntypedDBFunctionSymbol(@Nonnull String nameInDialect,
                                              @Nonnull ImmutableList<TermType> expectedBaseTypes) {
        super(nameInDialect + expectedBaseTypes.size(), expectedBaseTypes);
        this.nameInDialect = nameInDialect;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public final Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.empty();
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String parameterString = terms.stream()
                .map(termConverter::apply)
                .collect(Collectors.joining(","));
        return String.format(FUNCTIONAL_TEMPLATE, nameInDialect, parameterString);
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    /**
     * By default, to be overridden when necessary
     */
    @Override
    public boolean isPreferringToBePostProcessedOverBeingBlocked() {
        return false;
    }
}
