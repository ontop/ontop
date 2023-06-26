package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.template.impl.TemplateParser;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.StringConstantDecomposer;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.AbstractEncodeURIorIRIFunctionSymbol.IRISafeEnDecoder;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.R2RMLIRISafeEncoder;

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
    private final IRISafeEnDecoder enDecoder;
    private final Pattern patternForInteger;
    private final Pattern patternForDecimalFloat;
    private final Pattern patternForUuid;

    protected ObjectStringTemplateFunctionSymbolImpl(ImmutableList<Template.Component> components, TypeFactory typeFactory) {
        super(getTemplateString(components), createBaseTypes(components, typeFactory));
        this.lexicalType = typeFactory.getDBTypeFactory().getDBStringType();
        this.components = components;
        this.safeSeparatorFragments = SafeSeparatorFragment.split(TemplateParser.getEncodedTemplateString(components));
        // must not produce false positives
        this.isInjective = atMostOnePlaceholderPerSeparator(safeSeparatorFragments);
        this.enDecoder = new IRISafeEnDecoder();

        this.patternForInteger = Pattern.compile("^[0-9]+$");
        this.patternForDecimalFloat = Pattern.compile("^[0-9.+\\-eE]+$");
        this.patternForUuid = Pattern.compile("^[0-9a-fA-F\\-]+$");
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
                .anyMatch(ImmutableTerm::isNull)) {
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

            if (!SafeSeparatorFragment.areCompatible(this.safeSeparatorFragments, other.safeSeparatorFragments)) {
                // 3VL: needs to check for term nullability (null: if at least one is null, false otherwise)
                Optional<ImmutableExpression> newExpression = termFactory.getDisjunction(
                                Stream.concat(terms.stream(), otherTerm.getTerms().stream())
                                        .map(termFactory::getDBIsNull))
                        .map(e -> termFactory.getFalseOrNullFunctionalTerm(ImmutableList.of(e)));

                return newExpression
                        .map(e -> e.evaluate(variableNullability, true))
                        .orElseGet(IncrementalEvaluation::declareIsFalse);
            }

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

        ImmutableList<ImmutableExpression> expressions = IntStream.range(0, safeSeparatorFragments.size())
                // Sequential execution is essential
                .mapToObj(i -> convertToEquality(
                        safeSeparatorFragments.get(i), subTermIterator,
                        other.safeSeparatorFragments.get(i), otherSubTermIterator, termFactory))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toList());

        if (!expressions.isEmpty()) {
            ImmutableExpression nonNull = termFactory.getDBIsNotNull(
                    Stream.concat(subTerms.stream(), otherTerm.getTerms().stream()))
                    .orElseThrow(() -> new MinorOntopInternalBugException("cannot be empty because there is at least one variable in the templates (taken together)"));
            ImmutableExpression ifElseNull = termFactory.getBooleanIfElseNull(nonNull, termFactory.getConjunction(expressions));
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

        if (components.size() > 0 && !components.get(components.size() - 1).isColumnNameReference()
                && otherComponents.size() > 0 && !otherComponents.get(otherComponents.size() - 1).isColumnNameReference()) {
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

        return Optional.of(termFactory.getStrictEquality(
                convertIntoTerm(components, subTermIterator, termFactory),
                convertIntoTerm(otherComponents, otherSubTermIterator, termFactory)));
    }

    /**
     * To be used only in equalities.
     *
     * As an optimization, we directly apply decoding to the strings. This saves the column references from being
     * encoded, while the string constants are immediately decoded.
     *
     * Particularly useful when the CONCAT cannot be eliminated. In particular, the SQL queries become much less
     * verbose.
     */
    private ImmutableTerm convertIntoTerm(ImmutableList<Template.Component> components,
                                          UnmodifiableIterator<? extends ImmutableTerm> subTermIterator,
                                          TermFactory termFactory) {

        ImmutableList<ImmutableTerm> args = components.stream()
                .map(c -> c.isColumnNameReference()
                        ? subTermIterator.next()
                        : termFactory.getDBStringConstant(enDecoder.decode(c.getComponent())))
                .collect(ImmutableCollectors.toList());

        switch (args.size()) {
            case 0:
                return termFactory.getDBStringConstant("");
            case 1:
                return args.get(0);
            default:
                return termFactory.getNullRejectingDBConcatFunctionalTerm(args);
        }
    }

    @Override
    protected boolean canBeSafelyDecomposedIntoConjunction(ImmutableList<? extends ImmutableTerm> terms,
                                                           VariableNullability variableNullability,
                                                           ImmutableList<? extends ImmutableTerm> otherTerms) {
        if (isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms())
            return canBeSafelyDecomposedIntoConjunctionWhenInjective(terms, variableNullability, otherTerms);

        ImmutableSet<Integer> columnPositions = IntStream.range(0, components.size())
                .filter(i -> components.get(i).isColumnNameReference())
                .boxed()
                .collect(ImmutableCollectors.toSet());

        // Needs to have a separator between variables
        if (columnPositions.stream().anyMatch(i -> columnPositions.contains(i+1)))
            return false;

        ImmutableSet<Integer> separatorPositions = IntStream.range(0, components.size())
                .filter(i -> !components.get(i).isColumnNameReference())
                .boxed()
                .collect(ImmutableCollectors.toSet());

        // TODO: remove this restriction and tolerates consecutive separators
        if (IntStream.range(0, components.size() - 1)
                .anyMatch(i -> separatorPositions.contains(i) && separatorPositions.contains(i+1)))
            return false;

        if (separatorPositions.stream()
                // Only those separating columns
                .filter(i -> columnPositions.contains(i-1) && columnPositions.contains(i+1))
                .allMatch(i -> isSafelySeparating(i, terms, otherTerms))) {
            return canBeSafelyDecomposedIntoConjunctionWhenInjective(terms, variableNullability, otherTerms);
        }

        return false;
    }

    private boolean isSafelySeparating(int separatorIndex, ImmutableList<? extends ImmutableTerm> terms,
                                       ImmutableList<? extends ImmutableTerm> otherTerms) {
        String separatorString = components.get(separatorIndex).getComponent();

        if (separatorString.isEmpty())
            return false;

        int previousTermIndex = components.get(separatorIndex - 1).getIndex();
        int nextTermIndex = components.get(separatorIndex + 1).getIndex();

        return Stream.of(terms.get(previousTermIndex), otherTerms.get(previousTermIndex))
                .anyMatch(t -> !couldContain(t, separatorString, true))
                || Stream.of(terms.get(nextTermIndex), otherTerms.get(nextTermIndex))
                .anyMatch(t -> !couldContain(t, separatorString, false));
    }

    /**
     * Must not return false negative
     */
    private boolean couldContain(ImmutableTerm term, String separatorString, boolean isTermBefore) {
        if (term instanceof Variable)
            return true;

        if (term instanceof Constant) {
            Constant constant = (Constant) term;

            // Should normally not happen
            return term.isNull()
                    || (isTermBefore
                        ? constant.getValue().endsWith(separatorString)
                        : constant.getValue().startsWith(separatorString));
        }
        else {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;
            FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();

            if (functionSymbol instanceof DBTypeConversionFunctionSymbol) {
                boolean isSafelySeparating = ((DBTypeConversionFunctionSymbol) functionSymbol).getInputType()
                        .filter(t -> { switch(t.getCategory()) {
                            case INTEGER:
                                return !patternForInteger.matcher(separatorString).find();
                            case DECIMAL:
                            case FLOAT_DOUBLE:
                                return !patternForDecimalFloat.matcher(separatorString).find();
                            case UUID:
                                return !patternForUuid.matcher(separatorString).find();
                            default:
                                return false;
                        }
                        })
                        .isPresent();

                return !isSafelySeparating;
            }

            return true;
        }
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
                                        termFactory.getDBStringConstant(matcher.group(i + 1))))
                                .collect(ImmutableCollectors.toList()));

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
    public Optional<StringConstantDecomposer> getDecomposer(ImmutableList<? extends ImmutableTerm> terms,
                                                            TermFactory termFactory, VariableNullability variableNullability) {

        if (isInjective(terms, variableNullability, termFactory)) {
            Pattern pattern = getPattern();
            return Optional.of( cst -> {
                Matcher matcher = pattern.matcher(cst.getValue());
                if (matcher.find()) {
                    return Optional.of(IntStream.range(0, getArity())
                            .mapToObj(i -> termFactory.getDBStringConstant(
                                    R2RMLIRISafeEncoder.decode(matcher.group(i + 1))))
                            .collect(ImmutableCollectors.toList()));
                }
                return Optional.empty();
            });
        }
        return Optional.empty();
    }

    @Override
    public boolean isPreferringToBePostProcessedOverBeingBlocked() {
        return true;
    }
}

