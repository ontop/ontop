package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.IntStream;

public abstract class ObjectStringTemplateFunctionSymbolImpl extends FunctionSymbolImpl
        implements ObjectStringTemplateFunctionSymbol {

    private final String template;
    private final TermType lexicalType;

    protected ObjectStringTemplateFunctionSymbolImpl(String template, int arity, TypeFactory typeFactory) {
        super(template, arity, createBaseTypes(arity, typeFactory));
        this.template = template;
        // TODO: use a DB string instead
        this.lexicalType = typeFactory.getXsdStringDatatype();
    }

    private static ImmutableList<TermType> createBaseTypes(int arity, TypeFactory typeFactory) {
        // TODO: require DB string instead
        TermType stringType = typeFactory.getXsdStringDatatype();

        return IntStream.range(0, arity)
                .boxed()
                .map(i -> stringType)
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public String getTemplate() {
        return template;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        if(terms.stream()
                .filter(t -> t instanceof Constant)
                .anyMatch(t -> ((Constant)t).isNull())) {
            return Optional.empty();
        }
        return Optional.of(TermTypeInference.declareTermType(lexicalType));
    }

    @Override
    public Optional<TermTypeInference> inferAndValidateType(ImmutableList<? extends ImmutableTerm> terms)
            throws FatalTypingException {
        validateSubTermTypes(terms);
        return inferType(terms);
    }
}
