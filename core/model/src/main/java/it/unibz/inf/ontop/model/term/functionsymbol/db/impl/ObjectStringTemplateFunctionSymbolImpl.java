package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.template.impl.TemplateParser;
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
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.db.impl.SafeSeparatorFragment.NOT_A_SAFE_SEPARATOR_REGEX;

public abstract class ObjectStringTemplateFunctionSymbolImpl extends FunctionSymbolImpl
        implements ObjectStringTemplateFunctionSymbol {

    private final DBTermType lexicalType;
    private final boolean isInjective;
    private final ImmutableList<Template.Component> components;
    private final ImmutableList<SafeSeparatorFragment> safeSeparatorFragments;

    protected ObjectStringTemplateFunctionSymbolImpl(ImmutableList<Template.Component> components, TypeFactory typeFactory) {
        super(getTemplateString(components), createBaseTypes(components, typeFactory));
        this.lexicalType = typeFactory.getDBTypeFactory().getDBStringType();
        this.components = components;
        this.safeSeparatorFragments = SafeSeparatorFragment.split(TemplateParser.getEncodedTemplateString(components));
        // must not produce false positives
        this.isInjective = atMostOnePlaceholderPerSeparator(safeSeparatorFragments);
    }

    private boolean atMostOnePlaceholderPerSeparator(ImmutableList<SafeSeparatorFragment> safeSeparatorFragments) {
        return safeSeparatorFragments.stream()
                .map(SafeSeparatorFragment::getComponents)
                .allMatch(this::atMostOnePlaceholder);
    }

    private boolean atMostOnePlaceholder(ImmutableList<Template.Component> components) {
        return components.stream()
                .filter(Template.Component::isColumnNameReference)
                .count() <= 1;
    }

    private static String getTemplateString(ImmutableList<Template.Component> components) {
        return components.stream()
                .map(c -> c.isColumnNameReference() ? "{}" : c.getComponent())
                .collect(Collectors.joining());
    }


    private static ImmutableList<TermType> createBaseTypes(ImmutableList<Template.Component> components, TypeFactory typeFactory) {
        // TODO: require DB string instead
        TermType stringType = typeFactory.getXsdStringDatatype();
        return components.stream()
                .filter(Template.Component::isColumnNameReference)
                .map(c -> stringType)
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public String getTemplate() {
        return getName();
    }

    @Override
    public ImmutableList<Template.Component> getTemplateComponents() { return components; }

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

        if (newTerms.stream().allMatch(t -> t instanceof DBConstant)) {
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

            if (!SafeSeparatorFragment.areCompatible(this.safeSeparatorFragments, other.safeSeparatorFragments))
                return IncrementalEvaluation.declareIsFalse();

            if (!other.equals(this))
                return tryToSimplifyCompatibleTemplates(other, terms, otherTerm, termFactory, variableNullability);
        }

        // May decompose in case of injectivity
        return super.evaluateStrictEqWithFunctionalTerm(terms, otherTerm, termFactory, variableNullability);
    }

    /**
     * TODO: shall we try to handle non-injective templates?
     */
    private IncrementalEvaluation tryToSimplifyCompatibleTemplates(ObjectStringTemplateFunctionSymbolImpl other,
                                                                   ImmutableList<? extends ImmutableTerm> subTerms,
                                                                   ImmutableFunctionalTerm otherTerm,
                                                                   TermFactory termFactory,
                                                                   VariableNullability variableNullability) {
        UnmodifiableIterator<? extends ImmutableTerm> subTermIterator = subTerms.iterator();
        UnmodifiableIterator<? extends ImmutableTerm> otherSubTermIterator = otherTerm.getTerms().iterator();

        Stream<ImmutableExpression> expressionStream = IntStream.range(0, safeSeparatorFragments.size())
                // Sequential execution is essential
                .mapToObj(i -> convertToEquality(
                        safeSeparatorFragments.get(i), subTermIterator,
                        other.safeSeparatorFragments.get(i), otherSubTermIterator, termFactory))
                .filter(Optional::isPresent)
                .map(Optional::get);

        Optional<ImmutableExpression> expression = termFactory.getConjunction(expressionStream);
        if (expression.isPresent()) {
            ImmutableExpression nonNull = termFactory.getConjunction(
                    Stream.concat(subTerms.stream(), otherTerm.getTerms().stream())
                            .map(termFactory::getDBIsNotNull))
                    .get(); // this conjunction cannot be empty because
                            // there is at least one variable in the templates (taken together)
            ImmutableExpression ifElseNull = termFactory.getBooleanIfElseNull(nonNull, expression.get());
            return ifElseNull.evaluate(variableNullability, true);
        }
        else
            return IncrementalEvaluation.declareIsTrue();
    }

    private Optional<ImmutableExpression> convertToEquality(SafeSeparatorFragment safeSeparatorFragment,
                                            UnmodifiableIterator<? extends ImmutableTerm> subTermIterator,
                                            SafeSeparatorFragment otherSafeSeparatorFragment,
                                            UnmodifiableIterator<? extends ImmutableTerm> otherSubTermIterator,
                                            TermFactory termFactory) {

        ImmutableList<Template.Component> components = safeSeparatorFragment.getComponents();
        ImmutableList<Template.Component> otherComponents = otherSafeSeparatorFragment.getComponents();

        if (!components.get(0).isColumnNameReference()
                && !otherComponents.get(0).isColumnNameReference()) {
            String first = components.get(0).getComponent();
            String otherFirst = otherComponents.get(0).getComponent();
            if (first.startsWith(otherFirst)) {
                components = Template.replaceFirst(components, first.substring(otherFirst.length()));
                otherComponents = Template.replaceFirst(otherComponents, "");
            }
            else if (otherFirst.startsWith(first)) {
                components = Template.replaceFirst(components, "");
                otherComponents = Template.replaceFirst(otherComponents, otherFirst.substring(first.length()));
            }
            else
                return Optional.of(termFactory.getIsTrue(termFactory.getDBBooleanConstant(false)));

            if (components.isEmpty() && otherComponents.isEmpty())
                return Optional.empty();
        }

        if (!components.get(components.size() - 1).isColumnNameReference()
                && !otherComponents.get(otherComponents.size() - 1).isColumnNameReference()) {
            String last = components.get(components.size() - 1).getComponent();
            String otherLast = otherComponents.get(otherComponents.size() - 1).getComponent();
            if (last.endsWith(otherLast)) {
                components = Template.replaceLast(components, last.substring(0, last.length() - otherLast.length()));
                otherComponents = Template.replaceLast(otherComponents, "");
            }
            else if (otherLast.endsWith(last)) {
                components = Template.replaceLast(components, "");
                otherComponents = Template.replaceLast(otherComponents, otherLast.substring(0, otherLast.length() - last.length()));
            }
            else
                return Optional.of(termFactory.getIsTrue(termFactory.getDBBooleanConstant(false)));

            if (components.isEmpty() && otherComponents.isEmpty())
                return Optional.empty();
        }

        return Optional.of((ImmutableExpression) termFactory.getStrictEquality(
                convertIntoTerm(components, subTermIterator, termFactory),
                convertIntoTerm(otherComponents, otherSubTermIterator, termFactory)).simplify());
    }

    private ImmutableTerm convertIntoTerm(ImmutableList<Template.Component> components,
                                          UnmodifiableIterator<? extends ImmutableTerm> subTermIterator,
                                          TermFactory termFactory) {

        ImmutableList<ImmutableTerm> args = components.stream()
                .map(c -> c.isColumnNameReference()
                        // TODO: restore encoding
                        // ? termFactory.getR2RMLIRISafeEncodeFunctionalTerm(subTermIterator.next())
                        ? subTermIterator.next()
                        : termFactory.getDBStringConstant(c.getComponent()))
                .collect(ImmutableCollectors.toList());

        return args.size() == 1
                ? args.get(0)
                : termFactory.getNullRejectingDBConcatFunctionalTerm(args);
    }


    @Nullable
    private Pattern injectivePattern; // lazy initalization

    /**
     *  gives the pattern with a group for each placeholder
     *  applicable only to injective function symbols
     * @return
     */
    private Pattern getPattern() {
        if (injectivePattern == null) {
            String patternString = components.stream()
                    .map(c -> c.isColumnNameReference()
                            ? "(" + NOT_A_SAFE_SEPARATOR_REGEX + ")"
                            : SafeSeparatorFragment.makeRegexSafe(c.getComponent()))
                    .collect(Collectors.joining());

            injectivePattern = Pattern.compile("^" + patternString + "$");
        }
        return injectivePattern;
    }

    @Override
    protected IncrementalEvaluation evaluateStrictEqWithNonNullConstant(ImmutableList<? extends ImmutableTerm> terms,
                                                                        NonNullConstant otherTerm, TermFactory termFactory,
                                                                        VariableNullability variableNullability) {
        String otherValue = otherTerm.getValue();

        if (isInjective(terms, variableNullability, termFactory)) {
            Matcher matcher = getPattern().matcher(otherTerm.getValue());
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
        else if (!SafeSeparatorFragment.areCompatible(this.safeSeparatorFragments, SafeSeparatorFragment.split(otherValue)))
            return IncrementalEvaluation.declareIsFalse();

        return super.evaluateStrictEqWithNonNullConstant(terms, otherTerm, termFactory, variableNullability);
    }

    @Override
    public boolean isPreferringToBePostProcessedOverBeingBlocked() {
        return true;
    }
}

