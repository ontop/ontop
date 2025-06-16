package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.DisjunctionOfEqualitiesMergingSimplifier;
import it.unibz.inf.ontop.iq.type.impl.AbstractExpressionTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBAndFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBInFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBOrFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBStrictEqFunctionSymbol;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DisjunctionOfEqualitiesMergingSimplifierImpl extends AbstractIQOptimizer implements DisjunctionOfEqualitiesMergingSimplifier {

    private static final int MAX_ARITY = 8;

    private final IQVisitor<IQTree> inCreatingTransformer;
    private final IQVisitor<IQTree> inMergingTransformer;

    @Inject
    protected DisjunctionOfEqualitiesMergingSimplifierImpl(CoreSingletons coreSingletons) {
        super(coreSingletons.getIQFactory(), NORMALIZE_FOR_OPTIMIZATION);
        this.inCreatingTransformer = new InCreatingTransformer(coreSingletons);
        this.inMergingTransformer = new InMergingTransformer(coreSingletons);
    }

    @Override
    public IQTree transformTree(IQTree tree, VariableGenerator variableGenerator) {
        IQTree newTree = tree.acceptVisitor(inCreatingTransformer);
        return newTree.acceptVisitor(inMergingTransformer);
    }


    /**
     * Determines if the given term is a Strict Equality functional term that does not contain null constants and uses a constant.
     */
    private static boolean isConvertableEquality(ImmutableTerm term) {
        return Optional.of(term)
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm) t)
                .filter(t -> t.getFunctionSymbol() instanceof DBStrictEqFunctionSymbol)
                .filter(t -> t.getArity() == 2)
                .filter(t -> t.getTerms().stream()
                        .noneMatch(ImmutableTerm::isNull))
                .filter(t -> t.getTerms().stream()
                        .filter(child -> child instanceof Constant)
                        .count() == 1)
                .isPresent();
    }

    /**
     * Used to convert expressions of the form `X = [constant]` to `X IN ([constant])` so that
     * they can be merged with other IN calls.
     */
    private static Optional<ImmutableFunctionalTerm> convertSingleEqualities(ImmutableTerm term, TermFactory termFactory) {
        if (!isConvertableEquality(term))
            return Optional.empty();
        var f = (ImmutableFunctionalTerm) term;
        return Optional.of(termFactory.getImmutableExpression(
                termFactory.getDBFunctionSymbolFactory().getStrictDBIn(2),
                f.getTerm(0) instanceof Constant ? f.getTerm(1) : f.getTerm(0),
                f.getTerm(1) instanceof Constant ? f.getTerm(1) : f.getTerm(0)));
    }

    protected class InCreatingTransformer extends AbstractExpressionTransformer {

        protected InCreatingTransformer(CoreSingletons coreSingletons) {
            super(coreSingletons);
        }

        @Override
        protected boolean isFunctionSymbolToReplace(FunctionSymbol functionSymbol) {
            return functionSymbol instanceof DBOrFunctionSymbol;
        }

        @Override
        protected ImmutableFunctionalTerm replaceFunctionSymbol(FunctionSymbol functionSymbol, ImmutableList<ImmutableTerm> newTerms, IQTree tree) {
            //Partition terms into those that can be converted to a IN term and those that cannot.
            var equalities = newTerms.stream()
                    .collect(ImmutableCollectors.partitioningBy(DisjunctionOfEqualitiesMergingSimplifierImpl::isConvertableEquality));
            //Create IN terms and group them by their first term (= search term)
            var converted = equalities.get(true)
                    .stream()
                    .map(t -> convertSingleEqualities(t, termFactory).orElseThrow())
                    .collect(Collectors.groupingBy(t -> t.getTerm(0)));

            //Join all IN terms with the same search term together, then create a disjunction containing all new terms.
            return termFactory.getDisjunction(
                    Streams.concat(
                        equalities.get(false).stream()
                                .map(t -> (ImmutableExpression) t),
                        converted.entrySet().stream()
                                .map(entry -> termFactory.getImmutableExpression(
                                        termFactory.getDBFunctionSymbolFactory().getStrictDBIn(1 + entry.getValue().size()),
                                        Streams.concat(
                                                Stream.of(entry.getKey()),
                                                entry.getValue().stream()
                                                        .flatMap(t -> t.getTerms().stream().skip(1))
                                        ).collect(ImmutableCollectors.toList())
                                ))
                    )
            ).orElseThrow();
        }
    }

    private enum BooleanOperation {
        CONJUNCTION,
        DISJUNCTION;

        BooleanOperation dual() {
            switch (this) {
                case CONJUNCTION:
                    return DISJUNCTION;
                case DISJUNCTION:
                    return CONJUNCTION;
                default:
                    throw new IllegalArgumentException("Unknown operation " + this);
            }
        }

        static Optional<BooleanOperation> of(ImmutableFunctionalTerm term) {
            if (term.getFunctionSymbol() instanceof DBAndFunctionSymbol)
                return Optional.of(CONJUNCTION);
            if (term.getFunctionSymbol() instanceof DBOrFunctionSymbol)
                return Optional.of(DISJUNCTION);
            return Optional.empty();
        }
    }

    protected class InMergingTransformer extends AbstractExpressionTransformer {

        protected InMergingTransformer(CoreSingletons coreSingletons) {
            super(coreSingletons);
        }

        @Override
        protected boolean isFunctionSymbolToReplace(FunctionSymbol functionSymbol) {
            return (functionSymbol instanceof DBAndFunctionSymbol) || (functionSymbol instanceof DBOrFunctionSymbol);
        }

        /**
         * Determines if the given term is a boolean expression of the form [x] AND [y] / [x] OR [y].
         */
        private boolean isBooleanOperation(ImmutableFunctionalTerm f) {
            return BooleanOperation.of(f).isPresent();
        }

        private boolean isIn(ImmutableFunctionalTerm f) {
            return f.getFunctionSymbol() instanceof DBInFunctionSymbol;
        }

        /**
         * Assign an integer to different types of terms to sort them in the `mergeAll` method.
         */
        private int rankTerms(ImmutableTerm term, BooleanOperation operation) {
            if (!(term instanceof ImmutableFunctionalTerm))
                return 3;
            var f = (ImmutableFunctionalTerm) term;
            Optional<BooleanOperation> optionalOp = BooleanOperation.of(f);
            if (optionalOp.isPresent()) {
                BooleanOperation op = optionalOp.get();
                if (op != operation)
                    return 2;
                else
                    return 1;
            }
            if (isIn(f))
                return 0;
            return 4;
        }

        /**
         * Merges all given terms with each other, using either a conjunction or disjunction, given by the parameter.
         */
        private ImmutableTerm mergeAll(ImmutableList<ImmutableTerm> terms, BooleanOperation operation) {
            //First sort terms so that simpler terms are considered first.
            var sorted = terms.stream()
                    .sorted((t1, t2) -> rankTerms(t1, operation) - rankTerms(t2, operation))
                    .collect(ImmutableCollectors.toList());
            return sorted.stream()
                    .skip(1)
                    .reduce(sorted.get(0),
                            (current, next) -> {
                                if (current instanceof ImmutableFunctionalTerm
                                        && isBooleanOperation((ImmutableFunctionalTerm) current)
                                        && ((ImmutableFunctionalTerm) current).getArity() > MAX_ARITY) {
                                    switch (operation) {
                                        case CONJUNCTION:
                                            return termFactory.getConjunction((ImmutableExpression) next, (ImmutableExpression) current);
                                        case DISJUNCTION:
                                            return termFactory.getDisjunction((ImmutableExpression) next, (ImmutableExpression) current);
                                    }
                                }
                                return merge(current, next, operation);
                            },
                            (m1, m2) -> { throw new UnsupportedOperationException("This should never happen!"); });
        }

        /**
         * Constructs a Conjunction or Disjunction (based on the `operation` argument).
         * Any IN operations will be merged over the boolean operation, if possible.
         */
        private ImmutableTerm merge(ImmutableTerm left, ImmutableTerm right, BooleanOperation operation) {

            if (!(left instanceof ImmutableFunctionalTerm) || !(right instanceof ImmutableFunctionalTerm)) {
                //We can only simplify FunctionalTerms
                return booleanExpressionOf(operation, left, right);
            }

            ImmutableFunctionalTerm fLeft = convertSingleEqualities(left, termFactory).orElse((ImmutableFunctionalTerm) left);
            ImmutableFunctionalTerm fRight = convertSingleEqualities(right, termFactory).orElse((ImmutableFunctionalTerm) right);

            if (!isMergeable(fLeft) || !isMergeable(fRight)) {
                //We can only merge IN, AND, or OR
                return booleanExpressionOf(operation, left, right);
            }

            if (isIn(fRight)) {
                return mergeInto(fLeft, fRight, operation);
            }
            if (isIn(fLeft)) {
                return mergeInto(fRight, fLeft, operation);
            }

            if (!Sets.intersection(findAllSearchTerms(fLeft), findAllSearchTerms(fRight)).isEmpty())
                return crossMerge(fLeft, fRight, operation);

            return booleanExpressionOf(operation, left, right);
        }

        /**
         * Cross-expands boolean expressions of the form `(a * b) * (c * d)`.
         * where `*` is a boolean AND or OR (not necessarily the same each time it appears).
         */
        private ImmutableTerm crossMerge(ImmutableFunctionalTerm left, ImmutableFunctionalTerm right, BooleanOperation operation) {
            var rightConjunction = BooleanOperation.of(right);
            if (rightConjunction.isEmpty())
                throw new MinorOntopInternalBugException("Right must be a boolean operation");

            var terms = right.getTerms().stream()
                    .map(t -> merge(left, t, operation))
                    .flatMap(t -> BooleanOperation.of((ImmutableFunctionalTerm) t).equals(rightConjunction)
                            ? ((ImmutableFunctionalTerm) t).getTerms().stream()
                            : Stream.of(t));

            return booleanExpressionOf(rightConjunction.get(), terms);
        }

        /**
         * Compute the set of all terms that are searched with IN operations within this term or its children,
         * if it is a AND or OR.
         */
        private ImmutableSet<ImmutableTerm> findAllSearchTerms(ImmutableFunctionalTerm term) {
            if (isIn(term)) {
                return ImmutableSet.of(term.getTerm(0));
            }
            if (isBooleanOperation(term)) {
                return term.getTerms().stream()
                        .filter(t -> t instanceof ImmutableFunctionalTerm)
                        .map(t -> (ImmutableFunctionalTerm) t)
                        .map(this::findAllSearchTerms)
                        .flatMap(Collection::stream)
                        .collect(ImmutableCollectors.toSet());
            }
            return ImmutableSet.of();
        }

        private boolean isMergeable(ImmutableFunctionalTerm t) {
            return (isBooleanOperation(t) && t.getArity() <= MAX_ARITY) || isIn(t);
        }

        /**
         * Merges an IN function call into a complex boolean expression.
         */
        private ImmutableTerm mergeInto(ImmutableTerm target, ImmutableFunctionalTerm in, BooleanOperation operation) {
            if (!(target instanceof ImmutableFunctionalTerm))
                return booleanExpressionOf(operation, target, in);

            ImmutableFunctionalTerm f = (ImmutableFunctionalTerm)target;

            if (!isMergeable(f)
                    || !findAllSearchTerms(f).contains(in.getTerm(0))) {
                /* We can only merge into AND, OR, or IN FunctionSymbols and only if
                *    - target has any IN terms that may be merged
                *    - target does not already have too many children
                 */
                return booleanExpressionOf(operation, target, in);
            }

            if (isIn(f)) {
                //If target is an IN expression, we simply merge.
                if (canMergeWith(f.getTerms(), in.getTerms()))
                    return mergeWith(f.getTerms(), in.getTerms(), operation);

                return booleanExpressionOf(operation, f, in);
            }

            //Otherwise, it is an AND or an OR FunctionSymbol.

            //In some cases, we may be able to completely neglect the `target` operation because it is already implied by the `in`
            // or because it implies the `in` term.
            if ((operation == BooleanOperation.CONJUNCTION && isImpliedBy(in, f))
                    || (operation == BooleanOperation.DISJUNCTION && isImpliedBy(f, in))) {
                return in;
            }

            return BooleanOperation.of(f).get() == operation
                    ? mergeSameOperation(f, in, operation)
                    : mergeDualOperation(f, in, operation);
        }

        /**
         * Determines, whether an IN can be "merged into" a FunctionSymbol, i.e.
         *      1)    `target` is a compatible IN expression.
         *      2)    `target` is an AND or OR expression and `in`
         *                      can be merged into ANY of its children.
         */
        private boolean canMergeInto(ImmutableTerm target, ImmutableFunctionalTerm in) {
            if (!(target instanceof ImmutableFunctionalTerm))
                return false;

            var f = (ImmutableFunctionalTerm) target;
            if (isIn(f))
                return canMergeWith(f.getTerms(), in.getTerms());
            if (isBooleanOperation(f))
                return f.getTerms().stream()
                        .anyMatch(child -> canMergeInto(child, in));

            return false;
        }

        /**
         * EXAMPLE SCENARIO: (x AND y) OR z
         * where x, y are any expressions and z is an IN expression.
         * Here, a disjunction with z must be applied to x and to y.
         */
        private ImmutableTerm mergeDualOperation(ImmutableFunctionalTerm functionalTerm, ImmutableFunctionalTerm in, BooleanOperation dualOperation) {
            var canCancel = functionalTerm.getTerms().stream()
                    .collect(ImmutableCollectors.partitioningBy(
                            child -> canMergeInto(child, in)));

            var finalTerms = Streams.concat(
                    canCancel.get(false).isEmpty()
                            ? Stream.of()
                            : Stream.of(
                                booleanExpressionOf(dualOperation,
                                    booleanExpressionOf(dualOperation.dual(), canCancel.get(false)),
                                    in)),
                    canCancel.get(true).stream()
                            .map(term -> mergeInto(term, in, dualOperation)));

            return booleanExpressionOf(dualOperation.dual(), finalTerms);
        }

        private boolean isImpliedByTerm(ImmutableTerm determinant, ImmutableTerm dependent) {
            return (determinant instanceof ImmutableFunctionalTerm)
                    && (dependent instanceof ImmutableFunctionalTerm)
                    && isImpliedBy((ImmutableFunctionalTerm) determinant, (ImmutableFunctionalTerm) dependent);
        }

        /**
         * Determines whether the term `dependent` is implied by the term `determinant` in temrs of IN operations,
         * where IN1 implies IN2 if they both share the same search term and the list of matches of IN1 is a subset
         * of the list of matches of IN2.
         */
        private boolean isImpliedBy(ImmutableFunctionalTerm determinant, ImmutableFunctionalTerm dependent) {
            if (isIn(determinant) && isIn(dependent)) {
                //IN1 --> IN2 <==> search(IN1) = search(IN2) AND terms(IN1) subsetof terms(IN2)
                return determinant.getTerm(0).equals(dependent.getTerm(0))
                        && Sets.difference(ImmutableSet.copyOf(determinant.getTerms()), ImmutableSet.copyOf(dependent.getTerms())).isEmpty();
            }
            Optional<BooleanOperation> dependentOperation = BooleanOperation.of(dependent);
            if (isIn(determinant)) {
                if (dependentOperation.isPresent()) {
                    switch (dependentOperation.get()) {
                        case CONJUNCTION:
                            return dependent.getTerms().stream()
                                    .allMatch(term -> isImpliedByTerm(determinant, term));
                        case DISJUNCTION:
                            return dependent.getTerms().stream()
                                    .anyMatch(term -> isImpliedByTerm(determinant, term));
                        default:
                            throw new MinorOntopInternalBugException("Unexpected boolean operation: " + dependentOperation.get());
                    }
                }
            }
            Optional<BooleanOperation> determinantOperation = BooleanOperation.of(determinant);
            if (isIn(dependent)) {
                if (determinantOperation.isPresent()) {
                    switch (determinantOperation.get()) {
                        case CONJUNCTION:
                            return determinant.getTerms().stream()
                                    .anyMatch(term -> isImpliedByTerm(term, dependent));
                        case DISJUNCTION:
                            return determinant.getTerms().stream()
                                    .allMatch(term -> isImpliedByTerm(term, dependent));
                        default:
                            throw new MinorOntopInternalBugException("Unexpected boolean operation: " + determinantOperation.get());
                    }
                }
            }
            if (determinantOperation.isPresent() && dependentOperation.isPresent()) {
                throw new UnsupportedOperationException("This condition is not implemented because it should not come up.");
            }
            return false;
        }

        /**
         * EXAMPLE SCENARIO: (x AND y) AND z
         * where x, y are any expressions and z is an IN expression.
         * Here, a conjunction with z must be applied to either x or y.
         * If none of them can be merged with z, we can instead reformulate to
         * (x AND y AND z)
         */
        private ImmutableTerm mergeSameOperation(ImmutableFunctionalTerm functionalTerm, ImmutableFunctionalTerm in, BooleanOperation operation) {
            var compatibleChild = functionalTerm.getTerms().stream()
                    .filter(child -> canMergeInto(child, in))
                    .findFirst();

            if (compatibleChild.isPresent()) {
                return booleanExpressionOf(operation,
                        functionalTerm.getTerms().stream()
                                .map(child -> child == compatibleChild.get()
                                        ? mergeInto(child, in, operation)
                                        : child));
            }
            else {
                return booleanExpressionOf(operation,
                        Streams.concat(
                                functionalTerm.getTerms().stream(),
                                Stream.of(in)));
            }
        }

        @Override
        protected ImmutableFunctionalTerm replaceFunctionSymbol(FunctionSymbol functionSymbol, ImmutableList<ImmutableTerm> newTerms, IQTree tree) {
            if (newTerms.size() > MAX_ARITY)
                return termFactory.getImmutableExpression((BooleanFunctionSymbol) functionSymbol, newTerms);

            return (ImmutableFunctionalTerm) mergeAll(newTerms,
                    functionSymbol instanceof DBAndFunctionSymbol
                            ? BooleanOperation.CONJUNCTION
                            : BooleanOperation.DISJUNCTION);
        }

        /**
         * Constructs a Disjunction or Conjunction ImmutableExpression based on the value of `operation`.
         */
        private ImmutableTerm booleanExpressionOf(BooleanOperation operation, ImmutableList<? extends ImmutableTerm> terms) {
            if (terms.size() == 1)
                return terms.get(0);

            BooleanFunctionSymbol functionSymbol;
            switch (operation) {
                case CONJUNCTION:
                    functionSymbol = termFactory.getDBFunctionSymbolFactory().getDBAnd(terms.size());
                    break;
                case DISJUNCTION:
                    functionSymbol = termFactory.getDBFunctionSymbolFactory().getDBOr(terms.size());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown operation " + operation);
            }
            return termFactory.getImmutableExpression(functionSymbol, terms);
        }

        private ImmutableTerm booleanExpressionOf(BooleanOperation operation, Stream<? extends ImmutableTerm> termsStream) {
            return booleanExpressionOf(operation, termsStream.collect(ImmutableCollectors.toList()));
        }

        private ImmutableTerm booleanExpressionOf(BooleanOperation operation, ImmutableTerm term1, ImmutableTerm term2) {
            return booleanExpressionOf(operation, ImmutableList.of(term1, term2));
        }

        /**
         * Check if the term to compare is the same for both `IN` calls
         * We only merge if the arguments are all constant (for a disjunction, it would also work with non-constants
         *          but it could cause problems with short-circuiting behaviour if we change the order of operands)
         */
        private boolean canMergeWith(ImmutableList<? extends ImmutableTerm> ownTerms, ImmutableList<? extends ImmutableTerm> otherTerms) {
            return ownTerms.get(0).equals(otherTerms.get(0))
                    && (ownTerms.stream().skip(1)
                            .allMatch(t -> t instanceof Constant)
                            && otherTerms.stream().skip(1)
                            .allMatch(t -> t instanceof Constant));
        }

        /**
         * Merges two individuals IN terms based on their child terms. If `conjunction` is true, we compute the set intersection of their
         * values, otherwise we compute the set union.
         */
        private ImmutableExpression mergeWith(ImmutableList<? extends ImmutableTerm> ownTerms, ImmutableList<? extends ImmutableTerm> otherTerms, BooleanOperation operation) {
            //as the merging procedure will destroy any short-circuiting guarantees anyway, we can transform the list of children to a set here.
            ImmutableSet<ImmutableTerm> ownChildren = ownTerms.stream()
                    .skip(1)
                    .collect(ImmutableCollectors.toSet());
            ImmutableSet<ImmutableTerm> otherChildren = otherTerms.stream()
                    .skip(1)
                    .collect(ImmutableCollectors.toSet());
            ImmutableList<ImmutableTerm> mergedChildren = Streams.concat(
                    Stream.of(ownTerms.get(0)),
                    operation == BooleanOperation.CONJUNCTION
                            ? Sets.intersection(ownChildren, otherChildren).stream()
                            : Sets.union(ownChildren, otherChildren).stream()
            ).collect(ImmutableCollectors.toList());

            return termFactory.getImmutableExpression(
                    termFactory.getDBFunctionSymbolFactory().getStrictDBIn(mergedChildren.size()),
                    mergedChildren);
        }
    }
}
