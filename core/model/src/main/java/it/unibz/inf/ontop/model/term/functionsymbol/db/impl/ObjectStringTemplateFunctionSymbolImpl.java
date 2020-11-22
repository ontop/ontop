package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.Templates;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class ObjectStringTemplateFunctionSymbolImpl extends FunctionSymbolImpl
        implements ObjectStringTemplateFunctionSymbol {

    private final String template;
    private final DBTermType lexicalType;
    private final Pattern pattern;
    private final boolean isInjective;

    /**
     * TODO: enrich this list (incomplete)
     */
    protected static final ImmutableList<Character> SOME_SAFE_SEPARATORS = ImmutableList.of(
        '/','!','$','&','\'', '(', ')','*','+',',',';', '=', '#');
    protected static final String PLACE_HOLDER = "{}";

    // Lazy
    @Nullable
    private ImmutableList<DBConstant> templateConstants;

    protected ObjectStringTemplateFunctionSymbolImpl(String template, int arity, TypeFactory typeFactory) {
        super(template, createBaseTypes(arity, typeFactory));
        this.template = template;
        this.lexicalType = typeFactory.getDBTypeFactory().getDBStringType();
        this.templateConstants = null;
        this.pattern = extractPattern(template, true);

        this.isInjective = isInjective(arity, template);
    }

    /**
     * Must not produce false positive
     */
    protected boolean isInjective(int arity, String template) {
        if (arity < 2)
            return true;

        ImmutableList<String> intermediateStrings = extractIntermediateStrings(template);
        if (intermediateStrings.size() != (arity - 1))
            throw new IllegalArgumentException(
                    String.format("The template %s is not matching the arity %d",
                            template,
                            arity));
        return intermediateStrings.stream()
                .allMatch(interm -> SOME_SAFE_SEPARATORS.stream()
                        .anyMatch(sep -> interm.indexOf(sep) >= 0));
    }

    /**
     * Strings between the place holders
     */
    protected static ImmutableList<String> extractIntermediateStrings(String template) {
        ImmutableList.Builder<String> builder = ImmutableList.builder();

        // Non-final
        int afterPlaceHolderIndex = template.indexOf(PLACE_HOLDER) + 2;
        // Following index
        int nextPlaceHolderIndex = template.indexOf(PLACE_HOLDER, afterPlaceHolderIndex);
        while(nextPlaceHolderIndex > 0) {
            builder.add(template.substring(afterPlaceHolderIndex, nextPlaceHolderIndex));
            afterPlaceHolderIndex = nextPlaceHolderIndex + 2;
            nextPlaceHolderIndex = template.indexOf(PLACE_HOLDER, afterPlaceHolderIndex);
        }
        return builder.build();
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
                                                     TermFactory termFactory, VariableNullability variableNullability) {
        if (newTerms.stream()
            .allMatch(t -> t instanceof DBConstant)) {
            ImmutableList<String> values = newTerms.stream()
                    .map(t -> (DBConstant) t)
                    .map(c -> encodeParameter(c, termFactory, variableNullability))
                    .collect(ImmutableCollectors.toList());

            return termFactory.getDBConstant(Templates.format(template, values), lexicalType);
        }
        else
            return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    private String encodeParameter(DBConstant constant, TermFactory termFactory, VariableNullability variableNullability) {
        return Optional.of(termFactory.getR2RMLIRISafeEncodeFunctionalTerm(constant).simplify(variableNullability))
                .filter(t -> t instanceof DBConstant)
                .map(t -> ((DBConstant) t).getValue())
                .orElseThrow(() -> new MinorOntopInternalBugException("Was expecting " +
                        "the getR2RMLIRISafeEncodeFunctionalTerm to simplify itself to a DBConstant " +
                        "when receving a DBConstant"));
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
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return isInjective;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
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
                        ? Stream.of(
                                templateCsts.get(i),
                                termFactory.getR2RMLIRISafeEncodeFunctionalTerm(terms.get(i))
                                        // Avoids the encoding when possible
                                        .simplify())
                        : Stream.of(templateCsts.get(i)))
                .collect(ImmutableCollectors.toList());

        ImmutableTerm concatTerm = termsToConcatenate.isEmpty()
                ? termFactory.getDBStringConstant("")
                : (termsToConcatenate.size() == 1)
                    ? termsToConcatenate.get(0)
                    : termFactory.getNullRejectingDBConcatFunctionalTerm(termsToConcatenate).simplify();

        return termConverter.apply(concatTerm);
    }

    @Override
    protected IncrementalEvaluation evaluateStrictEqWithFunctionalTerm(ImmutableList<? extends ImmutableTerm> terms,
                                                                       ImmutableFunctionalTerm otherTerm, TermFactory termFactory,
                                                                       VariableNullability variableNullability) {
        FunctionSymbol otherFunctionSymbol = otherTerm.getFunctionSymbol();
        if (otherFunctionSymbol instanceof ObjectStringTemplateFunctionSymbol) {
            String otherTemplate = ((ObjectStringTemplateFunctionSymbol) otherFunctionSymbol).getTemplate();

            if (!areCompatible(otherTemplate))
                return IncrementalEvaluation.declareIsFalse();
        }

        // May decompose in case of injectivity
        return super.evaluateStrictEqWithFunctionalTerm(terms, otherTerm, termFactory, variableNullability);
    }

    /**
     * Is guaranteed not to return false negative.
     */
    protected boolean areCompatible(String otherTemplate) {
        if (template.equals(otherTemplate))
            return true;

        String prefix = extractPrefix(template);
        String otherPrefix = extractPrefix(otherTemplate);

        int prefixLength = prefix.length();
        int otherPrefixLength = otherPrefix.length();

        int minLength = Math.min(prefixLength, otherPrefixLength);

        /*
         * Prefix comparison
         */
        if (!prefix.substring(0, minLength).equals(otherPrefix.substring(0, minLength)))
            return false;

        String remainingTemplate = template.substring(minLength);
        String otherRemainingTemplate = otherTemplate.substring(minLength);

        /**
         * TODO: fix as it can return false negative
         */
        return matchPattern(remainingTemplate, otherRemainingTemplate)
                || matchPattern(otherRemainingTemplate, remainingTemplate);
    }

    private boolean matchPattern(String template1, String template2) {
        Pattern subPattern = extractPattern(template1, false);
        return subPattern.matcher(template2).find();
    }

    protected static Pattern extractPattern(String template, boolean surroundWithParentheses) {
        String tmpPlaceholder = UUID.randomUUID().toString().replace("-", "");
        String safeTemplate = makeRegexSafe(template
                .replace(PLACE_HOLDER, tmpPlaceholder));

        String notSeparator = SOME_SAFE_SEPARATORS.stream()
                .map(Object::toString)
                .map(ObjectStringTemplateFunctionSymbolImpl::makeRegexSafe)
                .reduce("[^", (c1, c2) -> c1 + c2, (c1, c2) -> c1 + c2) + "]*";

        String replacement = surroundWithParentheses ? "(" + notSeparator + ")" : notSeparator;

        String patternString = "^" + safeTemplate
                .replace(tmpPlaceholder, replacement)
                + "$";

        return Pattern.compile(patternString);
    }

    private static String makeRegexSafe(String s) {
        return s.replaceAll(
                "[\\<\\(\\[\\{\\\\\\^\\=\\$\\!\\|\\]\\}\\)\\?\\*\\+\\.\\>]", "\\\\$0");
    }


    private static String extractPrefix(String template) {
        int index = template.indexOf("{");
        return index >= 0
                ? template.substring(0, index)
                : template;
    }

    @Override
    protected IncrementalEvaluation evaluateStrictEqWithNonNullConstant(ImmutableList<? extends ImmutableTerm> terms,
                                                                        NonNullConstant otherTerm, TermFactory termFactory,
                                                                        VariableNullability variableNullability) {
        String otherValue = otherTerm.getValue();

        if (isInjective(terms, variableNullability, termFactory)) {
            Matcher matcher = pattern.matcher(otherTerm.getValue());
            if (matcher.find()) {
                ImmutableList<DBConstant> subConstants = IntStream.range(0, getArity())
                        .boxed()
                        .map(i -> matcher.group(i + 1))
                        .map(termFactory::getDBStringConstant)
                        .collect(ImmutableCollectors.toList());
                ImmutableExpression newExpression = termFactory.getConjunction(
                        IntStream.range(0, getArity())
                                .boxed()
                                .map(i -> termFactory.getStrictEquality(termFactory.getR2RMLIRISafeEncodeFunctionalTerm(terms.get(i)), subConstants.get(i))))
                        .orElseThrow(() -> new MinorOntopInternalBugException(
                                "An ObjectStringTemplateFunctionSymbolImpl is expected to have a non-null arity"));

                return newExpression.evaluate(variableNullability, true);
            }
            else
                return IncrementalEvaluation.declareIsFalse();
        }
        else if (!areCompatible(otherValue))
            return IncrementalEvaluation.declareIsFalse();

        return super.evaluateStrictEqWithNonNullConstant(terms, otherTerm, termFactory, variableNullability);
    }

    @Override
    public boolean isPreferringToBePostProcessedOverBeingBlocked() {
        return true;
    }
}
