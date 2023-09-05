package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.DisjunctionOfEqualitiesMergingSimplifier;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.iq.type.impl.AbstractExpressionTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBAndFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBOrFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBStrictEqFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.DBInFunctionSymbolImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Stream;

public class DisjunctionOfEqualitiesMergingSimplifierImpl implements DisjunctionOfEqualitiesMergingSimplifier {

    private final CoreSingletons coreSingletons;
    private static final int MAX_CROSS_DEPTH = 10;

    @Inject
    protected DisjunctionOfEqualitiesMergingSimplifierImpl(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree tree = query.getTree();

        InMergingTransformer transformer = new InMergingTransformer(coreSingletons.getIQFactory(), coreSingletons.getUniqueTermTypeExtractor(), coreSingletons.getTermFactory());
        IQTree newTree = transformer.transform(tree);

        return newTree == tree
                ? query
                : coreSingletons.getIQFactory().createIQ(query.getProjectionAtom(), newTree)
                .normalizeForOptimization();
    }

    protected static class InMergingTransformer extends AbstractExpressionTransformer {

        protected InMergingTransformer(IntermediateQueryFactory iqFactory, SingleTermTypeExtractor typeExtractor, TermFactory termFactory) {
            super(iqFactory, typeExtractor, termFactory);
        }

        @Override
        protected boolean isFunctionSymbolToReplace(FunctionSymbol functionSymbol) {
            return (functionSymbol instanceof DBAndFunctionSymbol) || (functionSymbol instanceof DBOrFunctionSymbol);
        }

        private static boolean isBooleanOperation(ImmutableTerm term) {
            if(!(term instanceof ImmutableFunctionalTerm))
                return false;
            var f = (ImmutableFunctionalTerm) term;
            return f.getFunctionSymbol() instanceof DBAndFunctionSymbol || f.getFunctionSymbol() instanceof DBOrFunctionSymbol;
        }

        private BooleanFunctionSymbol operatorSymbol(int arity, boolean conjunction) {
            return conjunction
                    ? termFactory.getDBFunctionSymbolFactory().getDBAnd(arity)
                    : termFactory.getDBFunctionSymbolFactory().getDBOr(arity);
        }

        private ImmutableTerm mergeAll(ImmutableList<ImmutableTerm> terms, boolean conjunction) {
            return terms.stream()
                    .skip(1)
                    .reduce(terms.get(0),
                            (current, next) -> merge(current, next, conjunction, 0),
                            (m1, m2) -> merge(m1, m2, conjunction, 0));
        }

        /**
         * Constructs a Conjunction or Disjunction (based on `conjunction` argument). Any IN operations will be merged
         * over the boolean operation, if possible.
         */
        private ImmutableTerm merge(ImmutableTerm left, ImmutableTerm right, boolean conjunction, int crossDepth) {

            if(!(left instanceof ImmutableFunctionalTerm) || !(right instanceof ImmutableFunctionalTerm)) {
                //We can only simplify FunctionalTerms
                return conjunctionDisjunctionOf(ImmutableList.of(left, right), conjunction);
            }

            var fLeft = convertSingleEqualities(left).orElse((ImmutableFunctionalTerm) left);
            var fRight = convertSingleEqualities(right).orElse((ImmutableFunctionalTerm) right);


            if(fRight.getFunctionSymbol() instanceof DBInFunctionSymbolImpl) {
                return mergeInto(fLeft, fRight, conjunction, crossDepth + 1);
            }
            if(fLeft.getFunctionSymbol() instanceof DBInFunctionSymbolImpl) {
                return mergeInto(fRight, fLeft, conjunction, crossDepth + 1);
            }

            if(!isBooleanOperation(fLeft) || !isBooleanOperation(fRight)) {
                //We can only merge IN, AND, or OR
                return conjunctionDisjunctionOf(ImmutableList.of(left, right), conjunction);
            }

            if(crossDepth < MAX_CROSS_DEPTH && !Sets.intersection(findAllSearchTerms(fLeft), findAllSearchTerms(fRight)).isEmpty())
                return crossMerge(fLeft, fRight, conjunction, crossDepth + 1);
            return conjunctionDisjunctionOf(ImmutableList.of(left, right), conjunction);
        }

        /**
         * Cross-expands boolean expressions of the form `(a * b) * (c * d)`.
         * where `*` is a boolean AND or OR (not necessarily the same each time it appears).
         */
        private ImmutableTerm crossMerge(ImmutableFunctionalTerm left, ImmutableFunctionalTerm right, boolean conjunction, int depth) {
            var rightConjunction = right.getFunctionSymbol() instanceof DBAndFunctionSymbol;
            var terms = right.getTerms().stream()
                    .map(t -> merge(left, t, conjunction, depth))
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
            if(term.getFunctionSymbol() instanceof DBInFunctionSymbolImpl) {
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
        private ImmutableTerm mergeInto(ImmutableTerm target, ImmutableFunctionalTerm in, boolean conjunction, int depth) {
            if(depth >= MAX_CROSS_DEPTH
                    || Optional.of(target)
                        .filter(t -> t instanceof ImmutableFunctionalTerm)
                        .map(t -> (ImmutableFunctionalTerm) t)
                        .filter(t -> (t.getFunctionSymbol() instanceof DBAndFunctionSymbol)
                            || (t.getFunctionSymbol() instanceof DBOrFunctionSymbol)
                            || (t.getFunctionSymbol() instanceof DBInFunctionSymbolImpl))
                        .isEmpty()
                    || !findAllSearchTerms((ImmutableFunctionalTerm) target).contains(in.getTerm(0))) {
                //We can only merge into AND, OR, or IN FunctionSymbols and only if target has any IN terms that may be merged
                return conjunctionDisjunctionOf(ImmutableList.of(target, in), conjunction);
            }

            
            var f = (ImmutableFunctionalTerm) target;
            if(f.getFunctionSymbol() instanceof DBInFunctionSymbolImpl) {
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
                    ? mergeSameOperation(f, in, conjunction, depth)
                    : mergeOppositeOperation(f, in, conjunction, depth);
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

            if(f.getFunctionSymbol() instanceof DBInFunctionSymbolImpl)
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
        private ImmutableTerm mergeOppositeOperation(ImmutableFunctionalTerm operation, ImmutableFunctionalTerm in, boolean conjunction, int depth) {
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
                            .map(term -> mergeInto(term, in, conjunction, depth))
            ).collect(ImmutableCollectors.toList());

            return conjunctionDisjunctionOf(finalTerms, !conjunction);
        }

        /**
         * Determines whether the term `dependent` is implied by the term `determinant` in temrs of IN operations,
         * where IN1 implies IN2 if they both share the same search term and the list of matches of IN1 is a subset
         * of the list of matches of IN2.
         */
        private boolean isImpliedBy(ImmutableFunctionalTerm determinant, ImmutableFunctionalTerm dependent) {
            if(determinant.getFunctionSymbol() instanceof DBInFunctionSymbolImpl && dependent.getFunctionSymbol() instanceof DBInFunctionSymbolImpl) {
                //IN1 --> IN2 <==> search(IN1) = search(IN2) AND terms(IN1) subsetof terms(IN2)
                return determinant.getTerm(0).equals(dependent.getTerm(0))
                        && Sets.difference(ImmutableSet.copyOf(determinant.getTerms()), ImmutableSet.copyOf(dependent.getTerms())).isEmpty();
            }
            if(determinant.getFunctionSymbol() instanceof DBInFunctionSymbolImpl && isBooleanOperation(dependent)) {
                if(dependent.getFunctionSymbol() instanceof DBAndFunctionSymbol) {
                    return dependent.getTerms().stream()
                            .allMatch(term -> (term instanceof ImmutableFunctionalTerm) && isImpliedBy(determinant, (ImmutableFunctionalTerm) term));
                } else {
                    return dependent.getTerms().stream()
                            .anyMatch(term -> (term instanceof ImmutableFunctionalTerm) && isImpliedBy(determinant, (ImmutableFunctionalTerm) term));
                }
            }
            if(dependent.getFunctionSymbol() instanceof DBInFunctionSymbolImpl && isBooleanOperation(determinant)) {
                if(determinant.getFunctionSymbol() instanceof DBAndFunctionSymbol) {
                    return determinant.getTerms().stream()
                            .anyMatch(term -> (term instanceof ImmutableFunctionalTerm) && isImpliedBy((ImmutableFunctionalTerm) term, dependent));
                } else {
                    return determinant.getTerms().stream()
                            .allMatch(term -> (term instanceof ImmutableFunctionalTerm) && isImpliedBy((ImmutableFunctionalTerm) term, dependent));
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
        private ImmutableTerm mergeSameOperation(ImmutableFunctionalTerm operation, ImmutableFunctionalTerm in, boolean conjunction, int depth) {
            var compatibleChild = operation.getTerms().stream()
                    .filter(child -> canMergeInto(child, in))
                    .findFirst();
            if(compatibleChild.isPresent()) {
                return termFactory.getImmutableExpression(
                        operatorSymbol(operation.getArity(), conjunction),
                        operation.getTerms().stream()
                                .map(child -> child == compatibleChild.get()
                                        ? mergeInto(child, in, conjunction, depth)
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

        /**
         * Determines if the given term is a Strict Equality functional term that does not contain null constants and uses a constant.
         */
        private boolean isConvertableEquality(ImmutableTerm term) {
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
        private Optional<ImmutableFunctionalTerm> convertSingleEqualities(ImmutableTerm term) {
            if(!isConvertableEquality(term))
                return Optional.empty();
            var f = (ImmutableFunctionalTerm) term;
            return Optional.of(termFactory.getImmutableExpression(
                    termFactory.getDBFunctionSymbolFactory().getDBIn(2),
                    f.getTerm(0) instanceof Constant ? f.getTerm(1) : f.getTerm(0),
                    f.getTerm(1) instanceof Constant ? f.getTerm(1) : f.getTerm(0)
            ));
        }

        @Override
        protected ImmutableFunctionalTerm replaceFunctionSymbol(FunctionSymbol functionSymbol, ImmutableList<ImmutableTerm> newTerms, IQTree tree) {
            return (ImmutableFunctionalTerm) mergeAll(newTerms, functionSymbol instanceof DBAndFunctionSymbol);
        }

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
                    termFactory.getDBFunctionSymbolFactory().getDBIn(mergedChildren.size()),
                    mergedChildren
            );
        }
    }

}
