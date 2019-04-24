package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Function symbols of each we don't know the return type.
 *
 * By default, it is treated as non-injective and non-postprocessable.
 *
 * This class is typically used for not recognized DB functions (e.g. when parsing the mapping)
 *
 * IMPORTANT ASSUMPTIONS (could be possibly violated as the function symbol is not recognized):
 *   1. Does not tolerate NULLs -> returns a NULL if it receives a NULL as input
 *   2. Does not return a NULL if its argument are all non-NULL
 *   3. The function symbol is DETERMINISTIC
 *
 * The assumption 1 and 2 are important for FILTERING out NULLs from the mapping assertions.
 * Violations of these assumptions are expected to be rare enough so that such function symbols can be recognized
 * and therefore be modelled properly.
 *
 */
public class DefaultUntypedDBFunctionSymbol extends FunctionSymbolImpl implements DBFunctionSymbol {

    private final DBFunctionSymbolSerializer serializer;

    protected DefaultUntypedDBFunctionSymbol(@Nonnull String nameInDialect, int arity, DBTermType rootDBTermType) {
        super(nameInDialect + arity, IntStream.range(0, arity)
                .boxed()
                .map(i -> (TermType) rootDBTermType)
                .collect(ImmutableCollectors.toList()));
        this.serializer = Serializers.getRegularSerializer(nameInDialect);
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public final Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.empty();
    }

    /**
     * ASSUMPTION 2
     */
    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializer.getNativeDBString(terms, termConverter, termFactory);
    }

    /**
     * ASSUMPTION 1
     */
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
