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

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    protected static final String NOT_A_SAFE_SEPARATOR_REGEX = "[^"
            + SOME_SAFE_SEPARATORS.stream()
                    .map(Object::toString)
                    .map(ObjectStringTemplateFunctionSymbolImpl::makeRegexSafe)
                    .collect(Collectors.joining())
            + "]*";

    protected static final String PLACEHOLDER = "{}";

    protected ObjectStringTemplateFunctionSymbolImpl(ImmutableList<TemplateComponent> components, TypeFactory typeFactory) {
        super(extractStringTemplate(components), createBaseTypes(components, typeFactory));
        this.template = extractStringTemplate(components);
        this.lexicalType = typeFactory.getDBTypeFactory().getDBStringType();
        this.components = components;
        this.pattern = extractPattern(components);
        this.isInjective = isInjective();
        this.onlyAlwaysSafeSeparators = extractOnlyAlwaysSafeSeparators(template);
    }

    /**
     * Must not produce false positive
     */
    private boolean isInjective() {
        long arity = components.stream()
                .filter(TemplateComponent::isColumnNameReference)
                .count();
        if (arity < 2)
            return true;

        // two consecutive columns
        for (int i = 1; i < components.size(); i++)
            if (components.get(i - 1).isColumnNameReference()
                    && components.get(i).isColumnNameReference())
                return false;

        return components.stream()
                .filter(c -> !c.isColumnNameReference())
                .map(TemplateComponent::getComponent)
                .allMatch(s -> firstIndexOfSafeSeparator(s, 0) >= 0);
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

    private static String encodeParameter(DBConstant constant, TermFactory termFactory, VariableNullability variableNullability) {
        return Optional.of(constant)
                .map(termFactory::getR2RMLIRISafeEncodeFunctionalTerm)
                .map(t -> t.simplify(variableNullability))
                .filter(t -> t instanceof DBConstant)
                .map(t -> ((DBConstant) t))
                .map(Constant::getValue)
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
        int minPrefixLength = Math.min(prefix.length(), otherPrefix.length());
        // Prefix comparison
        if (minPrefixLength > 0 &&
                !prefix.substring(0, minPrefixLength).equals(otherPrefix.substring(0, minPrefixLength)))
            return false;
/*
        String suffix = extractSuffix(template);
        String otherSuffix = extractSuffix(otherTemplate);
        int minSuffixLength = Math.min(suffix.length(), otherSuffix.length());
        if (!suffix.substring(suffix.length() - minSuffixLength).equals(otherSuffix.substring(otherSuffix.length() - minSuffixLength)))
            return false;
*/
        // Checks that both templates use the same safe separators in the same order
        if (!extractOnlyAlwaysSafeSeparators(otherTemplate).equals(onlyAlwaysSafeSeparators))
            return false;

        ImmutableList<String> fragments = splitOnSafeSeparators(template.substring(minPrefixLength)); // , template.length() - minSuffixLength
        ImmutableList<String> otherFragments = splitOnSafeSeparators(otherTemplate.substring(minPrefixLength)); // , otherTemplate.length() - minSuffixLength

        if (fragments.size() != otherFragments.size())
            throw new MinorOntopInternalBugException("Internal inconsistency detected while splitting IRI templates");

        return IntStream.range(0, fragments.size())
                .allMatch(i -> matchPatterns(fragments.get(i), otherFragments.get(i)));
    }

    private static ImmutableList<String> splitOnSafeSeparators(String s) {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        int start = 0, end;
        while ((end = firstIndexOfSafeSeparator(s, start)) != -1) {
            builder.add(s.substring(start, end));
            start = end + 1;
        }
        builder.add(s.substring(start));
        return builder.build();
    }

    private static int firstIndexOfSafeSeparator(String s, int start) {
        for (int i = start; i < s.length(); i++)
            if (SOME_SAFE_SEPARATORS.contains(s.charAt(i)))
                return i;
        return -1;
    }

    private static boolean matchPatterns(String subTemplate1, String subTemplate2) {
        return subTemplate1.equals(subTemplate2)
                || subTemplate1.indexOf('{') >= 0 && extractPattern(subTemplate1).matcher(subTemplate2).find()
                || subTemplate2.indexOf('{') >= 0 && extractPattern(subTemplate2).matcher(subTemplate1).find();
    }

    protected static Pattern extractPattern(String template) {
        StringBuilder patternString = new StringBuilder();
        int start = 0, end;
        while ((end = template.indexOf('{', start)) != -1) {
            patternString.append(makeRegexSafe(template.substring(start, end)))
                    .append(NOT_A_SAFE_SEPARATOR_REGEX);
            start = end + 2;
        }
        if (start < template.length())
            patternString.append(makeRegexSafe(template.substring(start)));

        return Pattern.compile("^" + patternString + "$");
    }

    protected static Pattern extractPattern(ImmutableList<TemplateComponent> components) {
        String patternString = components.stream()
                .map(c -> c.isColumnNameReference()
                    ? "(" + NOT_A_SAFE_SEPARATOR_REGEX + ")"
                    : makeRegexSafe(c.getComponent()))
                .collect(Collectors.joining());

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
        int index = template.indexOf('{');
        return index >= 0
                ? template.substring(0, index)
                : template;
    }

    private static String extractSuffix(String template) {
        int index = template.lastIndexOf('}');
        return index >= 0
                ? template.substring(index + 1)
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
