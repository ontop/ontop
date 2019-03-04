package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.iq.optimizer.OrderBySimplifier;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
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
    protected OrderBySimplifierImpl(IntermediateQueryFactory iqFactory, TermFactory termFactory) {
        this.iqFactory = iqFactory;
        this.transformer = new OrderBySimplifyingTransformer(iqFactory, termFactory);
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree newTree = query.getTree().acceptTransformer(transformer);
        return iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }


    protected static class OrderBySimplifyingTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final TermFactory termFactory;

        protected OrderBySimplifyingTransformer(IntermediateQueryFactory iqFactory, TermFactory termFactory) {
            super(iqFactory);
            this.termFactory = termFactory;
        }

        @Override
        public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {

            ImmutableList<OrderByNode.OrderComparator> newConditions = rootNode.getComparators().stream()
                    .flatMap(c -> simplifyComparator(c, child))
                    .collect(ImmutableCollectors.toList());

            if (newConditions.isEmpty())
                return child;

            OrderByNode newNode = iqFactory.createOrderByNode(newConditions);

            return iqFactory.createUnaryIQTree(newNode, child.acceptTransformer(this));
        }

        protected Stream<OrderByNode.OrderComparator> simplifyComparator(OrderByNode.OrderComparator comparator,
                                                                         IQTree child) {
            ImmutableTerm term = comparator.getTerm().simplify();
            if (term instanceof Constant)
                return Stream.empty();

            if ((term instanceof ImmutableFunctionalTerm)
                    && ((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof RDFTermFunctionSymbol) {
                ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;

                boolean isAscending = comparator.isAscending();

                return simplifyRDFTerm(functionalTerm.getTerm(0), unwrapIfElseNull(functionalTerm.getTerm(1)), child)
                        .map(t -> iqFactory.createOrderComparator(t, isAscending));
            }
            else
                return Stream.of(comparator);
        }

        protected Stream<NonGroundTerm> simplifyRDFTerm(ImmutableTerm lexicalTerm, ImmutableTerm rdfTypeTerm,
                                                        IQTree child) {
            if (rdfTypeTerm instanceof RDFTermTypeConstant) {
                return lexicalTerm.isGround()
                        ? Stream.empty()
                        : Stream.of(computeDBTerm((NonGroundTerm) lexicalTerm, (RDFTermTypeConstant) rdfTypeTerm));
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
    }

}
