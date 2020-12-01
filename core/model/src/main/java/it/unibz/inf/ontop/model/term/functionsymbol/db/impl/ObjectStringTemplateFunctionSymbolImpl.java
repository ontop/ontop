package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.template.TemplateComponent;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class ObjectStringTemplateFunctionSymbolImpl extends FunctionSymbolImpl
        implements ObjectStringTemplateFunctionSymbol {

    private final String template;
    private final DBTermType lexicalType;
    private final Pattern pattern;
    private final boolean isInjective;
    private final ImmutableList<TemplateComponent> components;
    // Use for checking compatibility
    private final String onlyAlwaysSafeSeparators;

    /**
     * TODO: enrich this list (incomplete)
     */
    protected static final ImmutableSet<Character> SOME_SAFE_SEPARATORS = ImmutableSet.of(
        '/','!','$','&','\'', '(', ')','*','+',',',';', '=', '#');
    protected static final String NOT_A_SAFE_SEPARATOR_REGEX = "[^" +
            SOME_SAFE_SEPARATORS.stream()
                    .map(Object::toString)
                    .map(ObjectStringTemplateFunctionSymbolImpl::makeRegexSafe)
                    .collect(Collectors.joining()) + "]*";

    protected static final String PLACEHOLDER = "{}";

    protected ObjectStringTemplateFunctionSymbolImpl(ImmutableList<TemplateComponent> components, TypeFactory typeFactory) {
        super(extractStringTemplate(components), createBaseTypes(components, typeFactory));
        this.template = extractStringTemplate(components);
        this.lexicalType = typeFactory.getDBTypeFactory().getDBStringType();
        this.components = components;
        this.pattern = extractPattern(this.template, true);
        this.isInjective = isInjective();
        this.onlyAlwaysSafeSeparators = extractOnlyAlwaysSafeSeparators(template);
    }

    /**
     * Must not produce false positive
     */
    protected boolean isInjective() {
        long arity = components.stream()
                .filter(TemplateComponent::isColumnNameReference)
                .count();
        if (arity < 2)
            return true;

        return components.stream()
                .filter(c -> !c.isColumnNameReference())
                .map(TemplateComponent::getComponent)
                .allMatch(interm -> SOME_SAFE_SEPARATORS.stream()
                        .anyMatch(sep -> interm.indexOf(sep) >= 0));
    }

    public static String extractStringTemplate(ImmutableList<TemplateComponent> template) {
        return template.stream()
                .map(c -> c.isColumnNameReference() ? PLACEHOLDER : c.getComponent())
                .collect(Collectors.joining());
    }


    private static ImmutableList<TermType> createBaseTypes(ImmutableList<TemplateComponent> template, TypeFactory typeFactory) {
        // TODO: require DB string instead
        TermType stringType = typeFactory.getXsdStringDatatype();
        return template.stream()
                .filter(TemplateComponent::isColumnNameReference)
                .map(c -> stringType)
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

            String value = components.stream()
                    .map(c -> c.isColumnNameReference()
                        ? encodeParameter((DBConstant)newTerms.get(c.getIndex()), termFactory, variableNullability)
                        : c.getComponent())
                    .collect(Collectors.joining());

            return termFactory.getDBConstant(value, lexicalType);
        }
        else
            return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    private String encodeParameter(DBConstant constant, TermFactory termFactory, VariableNullability variableNullability) {
        return Optional.of(constant)
                .map(termFactory::getR2RMLIRISafeEncodeFunctionalTerm)
                .map(t -> t.simplify(variableNullability))
                .filter(t -> t instanceof DBConstant)
                .map(t -> ((DBConstant) t).getValue())
                .orElseThrow(() -> new MinorOntopInternalBugException("Was expecting " +
                        "the getR2RMLIRISafeEncodeFunctionalTerm to simplify itself to a DBConstant " +
                        "when receiving a DBConstant"));
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

        ImmutableList<ImmutableTerm> termsToConcatenate = components.stream()
                .map(c -> c.isColumnNameReference()
                        ? termFactory.getR2RMLIRISafeEncodeFunctionalTerm(terms.get(c.getIndex()))
                            // Avoids the encoding when possible
                            .simplify()
                        : termFactory.getDBStringConstant(c.getComponent()))
                .collect(ImmutableCollectors.toList());

        ImmutableTerm concatTerm = termsToConcatenate.isEmpty()
                ? termFactory.getDBStringConstant("")
                : (termsToConcatenate.size() == 1)
                    ? termsToConcatenate.get(0)
                    : termFactory.getNullRejectingDBConcatFunctionalTerm(termsToConcatenate)
                            .simplify();

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

        int minLength = Math.min(prefix.length(), otherPrefix.length());

        /*
         * Prefix comparison
         */
        if (!prefix.substring(0, minLength).equals(otherPrefix.substring(0, minLength)))
            return false;

        // Checks that both templates use the same safe separators in the same order
        if (!extractOnlyAlwaysSafeSeparators(otherTemplate).equals(onlyAlwaysSafeSeparators))
            return false;

        ImmutableList<String> fragments = splitTemplate(template.substring(minLength));
        ImmutableList<String> otherFragments = splitTemplate(otherTemplate.substring(minLength));

        if (fragments.size() != otherFragments.size())
            throw new MinorOntopInternalBugException("Internal inconsistency detected while splitting IRI templates");

        return IntStream.range(0, fragments.size())
                .allMatch(i -> matchPatterns(fragments.get(i), otherFragments.get(i)));
    }

    private static ImmutableList<String> splitTemplate(String remainingTemplate) {
        return SOME_SAFE_SEPARATORS.stream()
                .reduce(Stream.of(remainingTemplate),
                        (st, c) -> st.flatMap(s -> Arrays.stream(s.split(Pattern.quote(c.toString())))),
                        (s1, s2) -> {
                            throw new MinorOntopInternalBugException("");
                        })
                .collect(ImmutableCollectors.toList());
    }
    private static boolean matchPatterns(String subTemplate1, String subTemplate2) {
        return subTemplate1.equals(subTemplate2)
                || matchPattern(subTemplate1, subTemplate2)
                || matchPattern(subTemplate2, subTemplate1);
    }

    private static boolean matchPattern(String subTemplate1, String subTemplate2) {
        return extractPattern(subTemplate1, false).matcher(subTemplate2).find();
    }

    protected static Pattern extractPattern(String template, boolean surroundWithParentheses) {
        String tmpPlaceholder = UUID.randomUUID().toString().replace("-", "");
        String safeTemplate = makeRegexSafe(template
                .replace(PLACEHOLDER, tmpPlaceholder));

        String replacement = surroundWithParentheses ? "(" + NOT_A_SAFE_SEPARATOR_REGEX + ")" : NOT_A_SAFE_SEPARATOR_REGEX;

        String patternString = safeTemplate
                .replace(tmpPlaceholder, replacement);

        return Pattern.compile("^" + patternString + "$");
    }

    private static String extractOnlyAlwaysSafeSeparators(String template) {
        return template.chars()
                .mapToObj(c -> (char) c)
                .filter(SOME_SAFE_SEPARATORS::contains)
                .collect(Collector.of(StringBuilder::new,
                        StringBuilder::append,
                        StringBuilder::append,
                        StringBuilder::toString));
    }

    private static String makeRegexSafe(String s) {
        return s.replaceAll("[<(\\[{\\\\^=$!|\\]})?*+.>]", "\\\\$0");
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
                ImmutableExpression newExpression = termFactory.getConjunction(
                        IntStream.range(0, getArity())
                                .mapToObj(i -> termFactory.getStrictEquality(
                                        termFactory.getR2RMLIRISafeEncodeFunctionalTerm(terms.get(i)),
                                        termFactory.getDBStringConstant(matcher.group(i + 1)))))
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
