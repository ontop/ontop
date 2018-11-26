package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.mikael.urlbuilder.util.Encoder;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.URITemplates;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.stream.IntStream;

public abstract class ObjectStringTemplateFunctionSymbolImpl extends FunctionSymbolImpl
        implements ObjectStringTemplateFunctionSymbol {

    private final String template;
    private final DBTermType lexicalType;
    private final Encoder iriEncoder;

    protected ObjectStringTemplateFunctionSymbolImpl(String template, int arity, TypeFactory typeFactory) {
        super(template, createBaseTypes(arity, typeFactory));
        this.template = template;
        this.lexicalType = typeFactory.getDBTypeFactory().getDBStringType();
        this.iriEncoder = new Encoder(Charset.forName("utf-8"));
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

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     boolean isInConstructionNodeInOptimizationPhase,
                                                     TermFactory termFactory) {
        if (newTerms.stream()
            .allMatch(t -> t instanceof DBConstant)) {
            ImmutableList<String> values = newTerms.stream()
                    .map(t -> (DBConstant) t)
                    .map(this::encodeParameter)
                    .collect(ImmutableCollectors.toList());

            return termFactory.getDBConstant(URITemplates.format(template, values), lexicalType);
        }
        else
            return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    private String encodeParameter(DBConstant constant) {
        // Query element: percent-encoding except if in iunreserved
        // TODO: this implementation seems to ignore the ucschar range. Check if it is a problem
        return iriEncoder.encodeQueryElement(constant.getValue());
    }


    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return true;
    }


    @Override
    public boolean canBePostProcessed() {
        return true;
    }
}
