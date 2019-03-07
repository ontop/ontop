package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.iq.optimizer.OrderBySimplifier;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transformer.DefinitionPushDownTransformer.DefPushDownRequest;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfElseNullFunctionSymbol;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

public class OrderBySimplifierImpl implements OrderBySimplifier {

    private final OrderBySimplifyingTransformer transformer;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected OrderBySimplifierImpl(IntermediateQueryFactory iqFactory, TermFactory termFactory,
                                    OptimizerFactory optimizerFactory) {
        this.iqFactory = iqFactory;
        this.transformer = new OrderBySimplifyingTransformer(iqFactory, termFactory, optimizerFactory);
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree newTree = query.getTree().acceptTransformer(transformer);
        return iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }


    protected static class OrderBySimplifyingTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final TermFactory termFactory;
        private final OptimizerFactory optimizerFactory;

        protected OrderBySimplifyingTransformer(IntermediateQueryFactory iqFactory, TermFactory termFactory,
                                                OptimizerFactory optimizerFactory) {
            super(iqFactory);
            this.termFactory = termFactory;
            this.optimizerFactory = optimizerFactory;
        }

        @Override
        public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {

            ImmutableList<ComparatorSimplification> simplifications = rootNode.getComparators().stream()
                    .flatMap(c -> simplifyComparator(c, child))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<OrderByNode.OrderComparator> newConditions = simplifications.stream()
                    .map(s -> s.newComparator)
                    .collect(ImmutableCollectors.toList());

            Stream<DefPushDownRequest> definitionsToPushDown = simplifications.stream()
                    .map(s -> s.request)
                    .filter(Optional::isPresent)
                    .map(Optional::get);

            if (newConditions.isEmpty())
                return child;

            OrderByNode newNode = iqFactory.createOrderByNode(newConditions);

            IQTree pushDownChildTree = pushDownDefinitions(child, definitionsToPushDown);

            return iqFactory.createUnaryIQTree(newNode, pushDownChildTree.acceptTransformer(this));
        }

        protected Stream<ComparatorSimplification> simplifyComparator(OrderByNode.OrderComparator comparator,
                                                                         IQTree child) {
            ImmutableTerm term = comparator.getTerm().simplify();
            if (term instanceof Constant)
                return Stream.empty();

            if ((term instanceof ImmutableFunctionalTerm)
                    && ((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof RDFTermFunctionSymbol) {
                ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;

                boolean isAscending = comparator.isAscending();

                return simplifyRDFTerm(
                        functionalTerm.getTerm(0),
                        unwrapIfElseNull(functionalTerm.getTerm(1)),
                        child,
                        isAscending);
            }
            else
                return Stream.of(new ComparatorSimplification(comparator));
        }

        protected Stream<ComparatorSimplification> simplifyRDFTerm(ImmutableTerm lexicalTerm, ImmutableTerm rdfTypeTerm,
                                                                   IQTree child, boolean isAscending) {
            if (rdfTypeTerm instanceof RDFTermTypeConstant) {
                return lexicalTerm.isGround()
                        ? Stream.empty()
                        : Stream.of(computeDBTerm((NonGroundTerm) lexicalTerm, (RDFTermTypeConstant) rdfTypeTerm))
                            .map(t -> iqFactory.createOrderComparator(t, isAscending))
                            .map(ComparatorSimplification::new);
            }
            else
                throw new RuntimeException("TODO: support the multi-typed case");
        }

        protected NonGroundTerm computeDBTerm(NonGroundTerm lexicalTerm, RDFTermTypeConstant typeConstant) {
            return (NonGroundTerm) termFactory.getConversionFromRDFLexical2DB(lexicalTerm, typeConstant.getRDFTermType())
                    .simplify();
        }

        private ImmutableTerm unwrapIfElseNull(ImmutableTerm term) {
            return Optional.of(term)
                    .filter(t -> t instanceof ImmutableFunctionalTerm)
                    .map(t -> (ImmutableFunctionalTerm) t)
                    .filter(t -> t.getFunctionSymbol() instanceof DBIfElseNullFunctionSymbol)
                    .map(t -> t.getTerm(1))
                    .orElse(term);
        }


        /**
         * Pushes down definitions emerging from the simplification of the order comparators
         */
        private IQTree pushDownDefinitions(IQTree initialChild, Stream<DefPushDownRequest> definitionsToPushDown) {
            return definitionsToPushDown
                    .reduce(initialChild,
                            (c, r) -> optimizerFactory.createDefinitionPushDownTransformer(r).transform(c),
                            (c1, c2) -> { throw new MinorOntopInternalBugException("Merging must not happen") ; });
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected static class ComparatorSimplification {
        protected final OrderByNode.OrderComparator newComparator;
        protected final Optional<DefPushDownRequest> request;

        protected ComparatorSimplification(OrderByNode.OrderComparator newComparator, DefPushDownRequest request) {
            this.newComparator = newComparator;
            this.request = Optional.of(request);
        }

        protected ComparatorSimplification(OrderByNode.OrderComparator newComparator) {
            this.newComparator = newComparator;
            this.request = Optional.empty();
        }
    }

}
