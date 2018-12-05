package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class BooleanFunctionSymbolImpl extends FunctionSymbolImpl implements BooleanFunctionSymbol {

    private final DBTermType dbBooleanTermType;

    protected BooleanFunctionSymbolImpl(@Nonnull String name, @Nonnull ImmutableList<TermType> expectedBaseTypes,
                                        DBTermType dbBooleanTermType) {
        super(name, expectedBaseTypes);
        this.dbBooleanTermType = dbBooleanTermType;
    }

    @Override
    public Optional<TermTypeInference> inferTypeFromArgumentTypes(ImmutableList<Optional<TermTypeInference>> actualArgumentTypes) {
        throw new RuntimeException("TODO: support inferTypeFromArgumentTypes?");
    }

    @Override
    public Optional<TermTypeInference> inferTypeFromArgumentTypesAndCheckForFatalError(
            ImmutableList<Optional<TermTypeInference>> actualArgumentTypes) throws FatalTypingException {
        throw new RuntimeException("TODO: support inferTypeFromArgumentTypesAndCheckForFatalError?");
    }

    /**
     * TODO: look at it seriously
     */
    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(dbBooleanTermType));
    }

    @Override
    public Optional<TermTypeInference> inferAndValidateType(ImmutableList<? extends ImmutableTerm> terms) throws FatalTypingException {
        validateSubTermTypes(terms);
        return inferType(terms);
    }
}
