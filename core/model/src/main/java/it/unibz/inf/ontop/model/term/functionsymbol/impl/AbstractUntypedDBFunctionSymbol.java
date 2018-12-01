package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import javax.annotation.Nonnull;
import java.util.Optional;

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
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return false;
    }

    @Override
    public final Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.empty();
    }

    @Override
    public final Optional<TermTypeInference> inferAndValidateType(ImmutableList<? extends ImmutableTerm> terms)
            throws FatalTypingException {
        validateSubTermTypes(terms);
        return Optional.empty();
    }

    @Override
    public boolean canBePostProcessed() {
        return false;
    }

    @Override
    public String getNativeDBString(ImmutableList<String> termStrings) {
        String parameterString = String.join(",", termStrings);
        return String.format(FUNCTIONAL_TEMPLATE, nameInDialect, parameterString);
    }
}
