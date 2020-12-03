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

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.model.term.functionsymbol.db.impl.SafeSeparatorFragment.NOT_A_SAFE_SEPARATOR_REGEX;

public abstract class ObjectStringTemplateFunctionSymbolImpl extends FunctionSymbolImpl
        implements ObjectStringTemplateFunctionSymbol {

    private final String template;
    private final DBTermType lexicalType;
    @Nullable
    private Pattern injectivePattern; // lazy initalization
    private final boolean isInjective;
    private final ImmutableList<TemplateComponent> components;
    private final ImmutableList<SafeSeparatorFragment> safeSeparatorFragments;


    protected static final String PLACEHOLDER = "{}";

    protected ObjectStringTemplateFunctionSymbolImpl(ImmutableList<TemplateComponent> components, TypeFactory typeFactory) {
        super(extractStringTemplate(components), createBaseTypes(components, typeFactory));
        this.lexicalType = typeFactory.getDBTypeFactory().getDBStringType();
        this.components = components;
        this.template = extractStringTemplate(components);
        this.safeSeparatorFragments = SafeSeparatorFragment.split(template);
        // must not produce false positives
        this.isInjective = safeSeparatorFragments.stream()
                .map(SafeSeparatorFragment::getFragment)
                .allMatch(ObjectStringTemplateFunctionSymbolImpl::atMostOnePlaceholder);
    }

    private static boolean atMostOnePlaceholder(String s) {
        int first = s.indexOf('{');
        if (first < 0)
            return true;
        return s.indexOf('{', first + 1) < 0;
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
        if (otherFunctionSymbol instanceof ObjectStringTemplateFunctionSymbolImpl) {
            ObjectStringTemplateFunctionSymbolImpl other = (ObjectStringTemplateFunctionSymbolImpl) otherFunctionSymbol;

            if (!areCompatible(other.safeSeparatorFragments))
                return IncrementalEvaluation.declareIsFalse();
        }

        // May decompose in case of injectivity
        return super.evaluateStrictEqWithFunctionalTerm(terms, otherTerm, termFactory, variableNullability);
    }

    /**
     * Is guaranteed not to return false negative.
     */
    protected boolean areCompatible(ImmutableList<SafeSeparatorFragment> otherFragments) {
        ImmutableList<SafeSeparatorFragment> fragments = this.safeSeparatorFragments;

        if (fragments == otherFragments)
            return true;

        return fragments.size() == otherFragments.size()

            && IntStream.range(0, fragments.size())
                .allMatch(i -> fragments.get(i).getSeparator() == otherFragments.get(i).getSeparator())

            && IntStream.range(0, fragments.size())
                .allMatch(i -> SafeSeparatorFragment.matchFragments(fragments.get(i), otherFragments.get(i)));
    }



    protected static Pattern extractPattern(ImmutableList<TemplateComponent> components) {
        String patternString = components.stream()
                .map(c -> c.isColumnNameReference()
                    ? "(" + NOT_A_SAFE_SEPARATOR_REGEX + ")"
                    : SafeSeparatorFragment.makeRegexSafe(c.getComponent()))
                .collect(Collectors.joining());

        return Pattern.compile("^" + patternString + "$");
    }

    @Override
    protected IncrementalEvaluation evaluateStrictEqWithNonNullConstant(ImmutableList<? extends ImmutableTerm> terms,
                                                                        NonNullConstant otherTerm, TermFactory termFactory,
                                                                        VariableNullability variableNullability) {
        String otherValue = otherTerm.getValue();

        if (isInjective(terms, variableNullability, termFactory)) {
            if (injectivePattern == null)
                this.injectivePattern = extractPattern(components);
            Matcher matcher = injectivePattern.matcher(otherTerm.getValue());
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
        else if (!areCompatible(SafeSeparatorFragment.split(otherValue)))
            return IncrementalEvaluation.declareIsFalse();

        return super.evaluateStrictEqWithNonNullConstant(terms, otherTerm, termFactory, variableNullability);
    }

    @Override
    public boolean isPreferringToBePostProcessedOverBeingBlocked() {
        return true;
    }
}

class SafeSeparatorFragment {
    private final String fragment;
    private final char separator;

    /**
     * TODO: enrich this list (incomplete)
     */
    protected static final ImmutableSet<Character> SOME_SAFE_SEPARATORS = ImmutableSet.of(
            '/','!','$','&','\'', '(', ')','*','+',',',';', '=', '#');

    protected static final String NOT_A_SAFE_SEPARATOR_REGEX = "[^"
            + SOME_SAFE_SEPARATORS.stream()
            .map(Object::toString)
            .map(SafeSeparatorFragment::makeRegexSafe)
            .collect(Collectors.joining())
            + "]*";

    SafeSeparatorFragment(String fragment, char separator) {
        this.fragment = fragment;
        this.separator = separator;
    }

    public String getFragment() { return fragment; }

    public char getSeparator() { return separator; }

    static ImmutableList<SafeSeparatorFragment> split(String s) {
        ImmutableList.Builder<SafeSeparatorFragment> builder = ImmutableList.builder();
        int start = 0, end;
        while ((end = firstIndexOfSafeSeparator(s, start)) != -1) {
            builder.add(new SafeSeparatorFragment(s.substring(start, end), s.charAt(end)));
            start = end + 1;
        }
        builder.add(new SafeSeparatorFragment(s.substring(start), (char)0));
        return builder.build();
    }

    private static int firstIndexOfSafeSeparator(String s, int start) {
        for (int i = start; i < s.length(); i++)
            if (SOME_SAFE_SEPARATORS.contains(s.charAt(i)))
                return i;
        return -1;
    }

    @Nullable
    private Pattern pattern;

    private Pattern extractPattern() {
        if (pattern == null) {
            StringBuilder patternString = new StringBuilder();
            int start = 0, end;
            while ((end = fragment.indexOf('{', start)) != -1) {
                patternString.append(makeRegexSafe(fragment.substring(start, end)))
                        .append(NOT_A_SAFE_SEPARATOR_REGEX);
                start = end + 2;
            }
            if (start < fragment.length())
                patternString.append(makeRegexSafe(fragment.substring(start)));

            pattern = Pattern.compile("^" + patternString + "$");
        }
        return pattern;
    }

    private boolean matchDifferent(String other) {
        return fragment.indexOf('{') >= 0
                && extractPattern().matcher(other).find();
    }

    public static boolean matchFragments(SafeSeparatorFragment subTemplate1, SafeSeparatorFragment subTemplate2) {
        return subTemplate1.getFragment().equals(subTemplate2.getFragment())
                || subTemplate1.matchDifferent(subTemplate2.getFragment())
                || subTemplate2.matchDifferent(subTemplate1.getFragment());
    }

    public static String makeRegexSafe(String s) {
        return s.replaceAll("[<(\\[{\\\\^=$!|\\]})?*+.>]", "\\\\$0");
    }


}