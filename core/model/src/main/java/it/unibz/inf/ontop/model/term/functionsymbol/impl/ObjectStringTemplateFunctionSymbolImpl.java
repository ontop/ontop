package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.mikael.urlbuilder.util.Encoder;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.URITemplates;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class ObjectStringTemplateFunctionSymbolImpl extends FunctionSymbolImpl
        implements ObjectStringTemplateFunctionSymbol {

    private final String template;
    private final DBTermType lexicalType;

    private final Encoder iriEncoder;

    // Lazy
    @Nullable
    private ImmutableList<DBConstant> templateConstants;

    protected ObjectStringTemplateFunctionSymbolImpl(String template, int arity, TypeFactory typeFactory) {
        super(template, createBaseTypes(arity, typeFactory));
        this.template = template;
        this.lexicalType = typeFactory.getDBTypeFactory().getDBStringType();
        this.iriEncoder = new Encoder(Charset.forName("utf-8"));
        this.templateConstants = null;
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
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     boolean isInConstructionNodeInOptimizationPhase,
                                                     TermFactory termFactory) {
        // For efficiency purposes, we keep the term functional to make decomposition easier
        if ((!isInConstructionNodeInOptimizationPhase) && newTerms.stream()
            .allMatch(t -> t instanceof DBConstant)) {
            ImmutableList<String> values = newTerms.stream()
                    .map(t -> (DBConstant) t)
                    .map(this::encodeParameter)
                    .collect(ImmutableCollectors.toList());

            return termFactory.getDBConstant(URITemplates.format(template, values), lexicalType);
        }
        else if (isOneArgumentNull(newTerms)) {
            return termFactory.getNullConstant();
        }
        else
            return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    private String encodeParameter(DBConstant constant) {
        // Query element: percent-encoding except if in iunreserved
        // TODO: this implementation seems to ignore the ucschar range. Check if it is a problem
        // TODO: redundant with R2RMLIRISafeEncoder. Which one shall we choose?
        return iriEncoder.encodeQueryElement(constant.getValue());
    }

    protected ImmutableList<DBConstant> getTemplateConstants(TermFactory termFactory) {
        if (templateConstants == null) {
            // An actual template: the first term is a string of the form
            // http://.../.../ or empty "{}" with placeholders of the form {}
            // The other terms are variables or constants that should replace
            // the placeholders. We need to tokenize and form the CONCAT
            String[] split = template.split("[{][}]");
            templateConstants = Stream.of(split)
                    .map(termFactory::getDBStringConstant)
                    .collect(ImmutableCollectors.toList());
        }

        return templateConstants;
    }


    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return true;
    }


    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        ImmutableList<DBConstant> templateCsts = getTemplateConstants(termFactory);

        ImmutableList<ImmutableTerm> termsToConcatenate = IntStream.range(0, templateCsts.size())
                .boxed()
                .flatMap(i -> (i < terms.size())
                        ? Stream.of(templateCsts.get(i), encodeTerm(terms.get(i), termFactory))
                        : Stream.of(templateCsts.get(i)))
                .collect(ImmutableCollectors.toList());

        ImmutableTerm concatTerm = termsToConcatenate.isEmpty()
                ? termFactory.getDBStringConstant("")
                : (termsToConcatenate.size() == 1)
                    ? termsToConcatenate.get(0)
                    : termFactory.getDBConcatFunctionalTerm(termsToConcatenate);

        return termConverter.apply(concatTerm);
    }

    /**
     * TODO: delegate everything to R2RMLIRISafeEncodeFunctionSymbol
     */
    protected ImmutableTerm encodeTerm(ImmutableTerm term, TermFactory termFactory) {
        if (term instanceof DBConstant) {
            return termFactory.getDBStringConstant(encodeParameter((DBConstant) term));
        }
        return termFactory.getR2RMLIRISafeEncodeFunctionalTerm(term);
    }
}
