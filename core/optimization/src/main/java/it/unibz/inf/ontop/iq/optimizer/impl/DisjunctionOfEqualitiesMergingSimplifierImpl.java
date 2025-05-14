package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.optimizer.DisjunctionOfEqualitiesMergingSimplifier;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.iq.type.impl.AbstractExpressionTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBAndFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBInFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBOrFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBStrictEqFunctionSymbol;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DisjunctionOfEqualitiesMergingSimplifierImpl implements DisjunctionOfEqualitiesMergingSimplifier {

    private final CoreSingletons coreSingletons;
    private static final int MAX_ARITY = 8;

    @Inject
    protected DisjunctionOfEqualitiesMergingSimplifierImpl(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree tree = query.getTree();

        //Create IN terms where we have disjunctions of equalities.
        InCreatingTransformer transformer1 = new InCreatingTransformer(coreSingletons);
        IQTree newTree = transformer1.transform(tree);

        //Merge IN terms together.
        InMergingTransformer transformer2 = new InMergingTransformer(coreSingletons);
        IQTree finalTree = transformer2.transform(newTree);

        return finalTree == tree
                ? query
                : coreSingletons.getIQFactory().createIQ(query.getProjectionAtom(), finalTree)
                .normalizeForOptimization();
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
        if(!isConvertableEquality(term))
            return Optional.empty();
        var f = (ImmutableFunctionalTerm) term;
        return Optional.of(termFactory.getImmutableExpression(
                termFactory.getDBFunctionSymbolFactory().getStrictDBIn(2),
                f.getTerm(0) instanceof Constant ? f.getTerm(1) : f.getTerm(0),
                f.getTerm(1) instanceof Constant ? f.getTerm(1) : f.getTerm(0)
        ));
    }

    protected static class InCreatingTransformer extends AbstractExpressionTransformer {

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

    protected static class InMergingTransformer extends AbstractExpressionTransformer {

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
        private static boolean isBooleanOperation(ImmutableTerm term) {
            if(!(term instanceof ImmutableFunctionalTerm))
                return false;
            var f = (ImmutableFunctionalTerm) term;
            return f.getFunctionSymbol() instanceof DBAndFunctionSymbol || f.getFunctionSymbol() instanceof DBOrFunctionSymbol;
        }

        /**
         * Helper method that returns the BooleanFunctionSymbol corresponding to the given arity, depending on the value of `conjunction`.
         */
        private BooleanFunctionSymbol operatorSymbol(int arity, boolean conjunction) {
            return conjunction
                    ? termFactory.getDBFunctionSymbolFactory().getDBAnd(arity)
                    : termFactory.getDBFunctionSymbolFactory().getDBOr(arity);
        }

        /**
         * Assign an integer to different types of terms to sort them in the `mergeAll` method.
         */
        private int rankTerms(ImmutableTerm term, boolean conjunction) {
            if(!(term instanceof ImmutableFunctionalTerm))
                return 3;
            var f = (ImmutableFunctionalTerm) term;
            if(isBooleanOperation(f) && ((f.getFunctionSymbol() instanceof DBAndFunctionSymbol) != conjunction))
                return 2;
            if(isBooleanOperation(f) && ((f.getFunctionSymbol() instanceof DBAndFunctionSymbol) == conjunction))
                return 1;
            if(f.getFunctionSymbol() instanceof DBInFunctionSymbol)
                return 0;
            return 4;
        }

        /**
         * Merges all given terms with each other, using either a conjunction or disjunction, given by the parameter.
         */
        private ImmutableTerm mergeAll(ImmutableList<ImmutableTerm> terms, boolean conjunction) {
            //First sort terms so that simpler terms are considered first.
            var sorted = terms.stream()
                    .sorted((t1, t2) -> rankTerms(t1, conjunction) - rankTerms(t2, conjunction))
                    .collect(ImmutableCollectors.toList());
            return sorted.stream()
                    .skip(1)
                    .reduce(sorted.get(0),
                            (current, next) -> {
                                if(isBooleanOperation(current) && ((ImmutableFunctionalTerm) current).getArity() > MAX_ARITY)
                                    return conjunction
                                            ? termFactory.getConjunction((ImmutableExpression) next, (ImmutableExpression) current)
                                            : termFactory.getDisjunction((ImmutableExpression) next, (ImmutableExpression) current);
                                return merge(current, next, conjunction);
                            },
                            (m1, m2) -> { throw new UnsupportedOperationException("This should never happen!"); });
        }

        /**
         * Constructs a Conjunction or Disjunction (based on `conjunction` argument). Any IN operations will be merged
         * over the boolean operation, if possible.
         */
        private ImmutableTerm merge(ImmutableTerm left, ImmutableTerm right, boolean conjunction) {

            if(!(left instanceof ImmutableFunctionalTerm) || !(right instanceof ImmutableFunctionalTerm)) {
                //We can only simplify FunctionalTerms
                return conjunctionDisjunctionOf(ImmutableList.of(left, right), conjunction);
            }

            var fLeft = convertSingleEqualities(left, termFactory).orElse((ImmutableFunctionalTerm) left);
            var fRight = convertSingleEqualities(right, termFactory).orElse((ImmutableFunctionalTerm) right);


            if(fRight.getFunctionSymbol() instanceof DBInFunctionSymbol) {
                return mergeInto(fLeft, fRight, conjunction);
            }
            if(fLeft.getFunctionSymbol() instanceof DBInFunctionSymbol) {
                return mergeInto(fRight, fLeft, conjunction);
            }

            if(!isBooleanOperation(fLeft) || !isBooleanOperation(fRight)) {
                //We can only merge IN, AND, or OR
                return conjunctionDisjunctionOf(ImmutableList.of(left, right), conjunction);
            }

            if(fLeft.getArity() > MAX_ARITY || fRight.getArity() > MAX_ARITY)
                return conjunctionDisjunctionOf(ImmutableList.of(left, right), conjunction);

            if(!Sets.intersection(findAllSearchTerms(fLeft), findAllSearchTerms(fRight)).isEmpty())
                return crossMerge(fLeft, fRight, conjunction);
            return conjunctionDisjunctionOf(ImmutableList.of(left, right), conjunction);
        }

        /**
         * Cross-expands boolean expressions of the form `(a * b) * (c * d)`.
         * where `*` is a boolean AND or OR (not necessarily the same each time it appears).
         */
        private ImmutableTerm crossMerge(ImmutableFunctionalTerm left, ImmutableFunctionalTerm right, boolean conjunction) {
            var rightConjunction = right.getFunctionSymbol() instanceof DBAndFunctionSymbol;
            var terms = right.getTerms().stream()
                    .map(t -> merge(left, t, conjunction))
                    .flatMap(t -> (isBooleanOperation(t) && ((ImmutableFunctionalTerm) t).getFunctionSymbol() instanceof DBAndFunctionSymbol == rightConjunction)
                            ? ((ImmutableFunctionalTerm) t).getTerms().stream()
                            : Stream.of(t))
                    .collect(ImmutableCollectors.toList());
            return conjunctionDisjunctionOf(terms, rightConjunction);
        }

        /**
         * Compute the set of all terms that are searched with IN operations within this term or its children,
         * if it is a AND or OR.
         */
        private ImmutableSet<ImmutableTerm> findAllSearchTerms(ImmutableFunctionalTerm term) {
            if(term.getFunctionSymbol() instanceof DBInFunctionSymbol) {
                return ImmutableSet.of(term.getTerm(0));
            }
            if(isBooleanOperation(term)) {
                return term.getTerms().stream()
                        .filter(t -> t instanceof ImmutableFunctionalTerm)
                        .flatMap(t -> findAllSearchTerms((ImmutableFunctionalTerm) t).stream())
                        .collect(ImmutableCollectors.toSet());
            }
            return ImmutableSet.of();
        }

        /**
         * Merges an IN function call into a complex boolean expression.
         */
        private ImmutableTerm mergeInto(ImmutableTerm target, ImmutableFunctionalTerm in, boolean conjunction) {
            var targetFunction = Optional.of(target)
                    .filter(t -> t instanceof ImmutableFunctionalTerm)
                    .map(t -> (ImmutableFunctionalTerm) t);

            if(targetFunction
                        .filter(t -> (isBooleanOperation(t) && t.getArity() <= MAX_ARITY) || (t.getFunctionSymbol() instanceof DBInFunctionSymbol))
                        .isEmpty()
                    || !findAllSearchTerms(targetFunction.get()).contains(in.getTerm(0))
            ) {
                /* We can only merge into AND, OR, or IN FunctionSymbols and only if
                *    - target has any IN terms that may be merged
                *    - target does not already have too many children
                 */
                return conjunctionDisjunctionOf(ImmutableList.of(target, in), conjunction);
            }

            
            var f = targetFunction.get();
            if(f.getFunctionSymbol() instanceof DBInFunctionSymbol) {
                //If target is an IN expression, we simply merge.
                if(canMergeWith(f.getTerms(), in.getTerms()))
                    return mergeWith(f.getTerms(), in.getTerms(), conjunction);
                return conjunctionDisjunctionOf(ImmutableList.of(f, in), conjunction);
            }

            //Otherwise, it is an AND or OR FunctionSymbol.

            //In some cases, we may be able to completely neglect the `target` operation because it is already implied by the `in`
            // or because it implies the `in` term.
            if((conjunction && isImpliedBy(in, f)) || (!conjunction && isImpliedBy(f, in))) {
                return in;
            }

            var targetConjunction = f.getFunctionSymbol() instanceof DBAndFunctionSymbol;
            return targetConjunction == conjunction 
                    ? mergeSameOperation(f, in, conjunction)
                    : mergeOppositeOperation(f, in, conjunction);
        }

        /**
         * Determines, whether an IN can be "merged into" a FunctionSymbol, i.e.
         *      1)    `target` is a compatible IN expression.
         *      2)    `target` is an AND or OR expression and `in`
         *                      can be merged into ANY of its children.
         */
        private boolean canMergeInto(ImmutableTerm target, ImmutableFunctionalTerm in) {
            if(!(target instanceof ImmutableFunctionalTerm))
                return false;
            var f = (ImmutableFunctionalTerm) target;

            if(f.getFunctionSymbol() instanceof DBInFunctionSymbol)
                return canMergeWith(f.getTerms(), in.getTerms());
            if(isBooleanOperation(f))
                return f.getTerms().stream()
                        .anyMatch(child -> canMergeInto(child, in));
            return false;
        }

        /**
         * EXAMPLE SCENARIO: (x AND y) OR z
         * where x, y are any expressions and z is an IN expression.
         * Here, a disjunction with z must be applied to x and to y.
         */
        private ImmutableTerm mergeOppositeOperation(ImmutableFunctionalTerm operation, ImmutableFunctionalTerm in, boolean conjunction) {
            var canCancel = operation.getTerms().stream()
                    .collect(ImmutableCollectors.partitioningBy(
                            child -> canMergeInto(child, in))
                    );

            var finalTerms = Streams.concat(
                    canCancel.get(false).isEmpty()
                            ? Stream.of()
                            : Stream.of(termFactory.getImmutableExpression(
                                    operatorSymbol(2, conjunction),
                                    conjunctionDisjunctionOf(canCancel.get(false), !conjunction),
                                    in
                    )),
                    canCancel.get(true).stream()
                            .map(term -> mergeInto(term, in, conjunction))
            ).collect(ImmutableCollectors.toList());

            return conjunctionDisjunctionOf(finalTerms, !conjunction);
        }

        private boolean isImpliedByTerm(ImmutableTerm determinant, ImmutableTerm dependent) {
            if(!(determinant instanceof ImmutableFunctionalTerm) || !(dependent instanceof ImmutableFunctionalTerm))
                return false;
            return isImpliedBy((ImmutableFunctionalTerm) determinant, (ImmutableFunctionalTerm) dependent);
        }

        /**
         * Determines whether the term `dependent` is implied by the term `determinant` in temrs of IN operations,
         * where IN1 implies IN2 if they both share the same search term and the list of matches of IN1 is a subset
         * of the list of matches of IN2.
         */
        private boolean isImpliedBy(ImmutableFunctionalTerm determinant, ImmutableFunctionalTerm dependent) {
            if(determinant.getFunctionSymbol() instanceof DBInFunctionSymbol && dependent.getFunctionSymbol() instanceof DBInFunctionSymbol) {
                //IN1 --> IN2 <==> search(IN1) = search(IN2) AND terms(IN1) subsetof terms(IN2)
                return determinant.getTerm(0).equals(dependent.getTerm(0))
                        && Sets.difference(ImmutableSet.copyOf(determinant.getTerms()), ImmutableSet.copyOf(dependent.getTerms())).isEmpty();
            }
            if(determinant.getFunctionSymbol() instanceof DBInFunctionSymbol && isBooleanOperation(dependent)) {
                if(dependent.getFunctionSymbol() instanceof DBAndFunctionSymbol) {
                    return dependent.getTerms().stream()
                            .allMatch(term -> isImpliedByTerm(determinant, term));
                } else {
                    return dependent.getTerms().stream()
                            .anyMatch(term -> isImpliedByTerm(determinant, term));
                }
            }
            if(dependent.getFunctionSymbol() instanceof DBInFunctionSymbol && isBooleanOperation(determinant)) {
                if(determinant.getFunctionSymbol() instanceof DBAndFunctionSymbol) {
                    return determinant.getTerms().stream()
                            .anyMatch(term -> isImpliedByTerm(term, dependent));
                } else {
                    return determinant.getTerms().stream()
                            .allMatch(term -> isImpliedByTerm(term, dependent));
                }
            }
            if(isBooleanOperation(determinant) && isBooleanOperation(dependent)) {
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
        private ImmutableTerm mergeSameOperation(ImmutableFunctionalTerm operation, ImmutableFunctionalTerm in, boolean conjunction) {
            var compatibleChild = operation.getTerms().stream()
                    .filter(child -> canMergeInto(child, in))
                    .findFirst();
            if(compatibleChild.isPresent()) {
                return termFactory.getImmutableExpression(
                        operatorSymbol(operation.getArity(), conjunction),
                        operation.getTerms().stream()
                                .map(child -> child == compatibleChild.get()
                                        ? mergeInto(child, in, conjunction)
                                        : child)
                                .collect(ImmutableCollectors.toList())
                );
            }
            else {
                return termFactory.getImmutableExpression(
                        operatorSymbol(operation.getArity() + 1, conjunction),
                        Streams.concat(
                                operation.getTerms().stream(),
                                Stream.of(in)
                        ).collect(ImmutableCollectors.toList())
                );
            }
        }

        @Override
        protected ImmutableFunctionalTerm replaceFunctionSymbol(FunctionSymbol functionSymbol, ImmutableList<ImmutableTerm> newTerms, IQTree tree) {
            if(newTerms.size() > MAX_ARITY)
                return termFactory.getImmutableExpression((BooleanFunctionSymbol) functionSymbol, newTerms);
            return (ImmutableFunctionalTerm) mergeAll(newTerms, functionSymbol instanceof DBAndFunctionSymbol);
        }

        /**
         * Constructs a Disjunction or Conjunction ImmutableExpression based on the value of `conjunction`.
         */
        private ImmutableTerm conjunctionDisjunctionOf(ImmutableList<? extends ImmutableTerm> terms, boolean conjunction) {
            if(terms.size() == 1)
                return terms.get(0);
            return termFactory.getImmutableExpression(operatorSymbol(terms.size(), conjunction), terms);
        }

        /**
         * Check if the term to compare is the same for both `IN` calls
         * We only merge if the arguments are all constant (for a disjunction, it would also work with non-constants
         *          but it could cause problems with short-circuiting behaviour if we change the order of operands)
         */
        private boolean canMergeWith(ImmutableList<? extends ImmutableTerm> ownTerms, ImmutableList<? extends ImmutableTerm> otherTerms) {
            return ownTerms.get(0).equals(otherTerms.get(0))
                    && (
                    ownTerms.stream().skip(1)
                            .allMatch(t -> t instanceof Constant)
                            && otherTerms.stream().skip(1)
                            .allMatch(t -> t instanceof Constant)
            );
        }

        /**
         * Merges two individual IN terms based on their child terms. If `conjunction` is true, we compute the set intersection of their
         * values, otherwise we compute the set union.
         */
        private ImmutableExpression mergeWith(ImmutableList<? extends ImmutableTerm> ownTerms, ImmutableList<? extends ImmutableTerm> otherTerms, boolean conjunction) {
            //as the merging procedure will destroy any short-circuiting guarantees anyway, we can transform the list of children to a set here.
            ImmutableSet<ImmutableTerm> ownChildren = ownTerms.stream()
                    .skip(1)
                    .collect(ImmutableCollectors.toSet());
            ImmutableSet<ImmutableTerm> otherChildren = otherTerms.stream()
                    .skip(1)
                    .collect(ImmutableCollectors.toSet());
            ImmutableList<ImmutableTerm> mergedChildren = Streams.concat(
                    Stream.of(ownTerms.get(0)),
                    conjunction ? Sets.intersection(ownChildren, otherChildren).stream() : Sets.union(ownChildren, otherChildren).stream()
            ).collect(ImmutableCollectors.toList());
            return termFactory.getImmutableExpression(
                    termFactory.getDBFunctionSymbolFactory().getStrictDBIn(mergedChildren.size()),
                    mergedChildren
            );
        }
    }

}
