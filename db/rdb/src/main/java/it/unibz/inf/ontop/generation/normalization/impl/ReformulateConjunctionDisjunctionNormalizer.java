package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBAndFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBOrFunctionSymbol;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * The CDataDynamoDB driver seems to be struggling with the boolean operators AND and OR.
 * However, converting them to the opposite operators using De Morgan's law seems to fix these issues.
 */
public class ReformulateConjunctionDisjunctionNormalizer implements DialectExtraNormalizer {

    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;
    private final Transformer transformer;

    @Inject
    protected ReformulateConjunctionDisjunctionNormalizer(CoreSingletons coreSingletons) {
        this.termFactory = coreSingletons.getTermFactory();
        this.iqFactory = coreSingletons.getIQFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.transformer = new Transformer();
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(transformer);
    }

    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        Transformer() {
            super(ReformulateConjunctionDisjunctionNormalizer.this.iqFactory);
        }

        @Override
        public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
            var expression = rootNode.getFilterCondition();
            var newExpression = transformTerm(expression);
            if (newExpression.equals(expression))
                return super.transformFilter(tree, rootNode, child);

            return iqFactory.createUnaryIQTree(
                    iqFactory.createFilterNode((ImmutableExpression) newExpression),
                    transformChild(child));
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            var expression = rootNode.getOptionalFilterCondition();
            var newExpression = expression.map(this::transformTerm);
            if (newExpression.equals(expression))
                return super.transformLeftJoin(tree, rootNode, leftChild, rightChild);

            return iqTreeTools.createLeftJoinTree(
                    Optional.of((ImmutableExpression) newExpression.get()),
                    transformChild(leftChild),
                    transformChild(rightChild));
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            var expression = rootNode.getOptionalFilterCondition();
            var newExpression = expression.map(this::transformTerm);
            if (newExpression.equals(expression))
                return super.transformInnerJoin(tree, rootNode, children);

            return iqTreeTools.createInnerJoinTree(Optional.of((ImmutableExpression) newExpression.get()),
                    NaryIQTreeTools.transformChildren(children, this::transformChild));
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
            var newConstruction = iqTreeTools.replaceSubstitution(rootNode, s -> s.transform(this::transformTerm));
            if (newConstruction.equals(rootNode))
                return super.transformConstruction(tree, rootNode, child);

            return iqFactory.createUnaryIQTree(newConstruction, transformChild(child));
        }

        @Override
        public IQTree transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
            var expressions = rootNode.getComparators().stream()
                    .map(c -> iqFactory.createOrderComparator((NonGroundTerm) transformTerm(c.getTerm()), c.isAscending()))
                    .collect(ImmutableCollectors.toList());
            var newOrderBy = iqFactory.createOrderByNode(expressions);
            if (newOrderBy.equals(rootNode))
                return super.transformOrderBy(tree, rootNode, child);

            return iqFactory.createUnaryIQTree(newOrderBy, transformChild(child));
        }

        @Override
        public IQTree transformAggregation(UnaryIQTree tree, AggregationNode rootNode, IQTree child) {
            var newSubstitution = rootNode.getSubstitution().transform(t -> (ImmutableFunctionalTerm) transformTerm(t));
            var newAggregation = iqFactory.createAggregationNode(rootNode.getGroupingVariables(), newSubstitution);
            if (newAggregation.equals(rootNode))
                return super.transformAggregation(tree, rootNode, child);

            return iqFactory.createUnaryIQTree(newAggregation, transformChild(child));
        }

        private ImmutableTerm transformTerm(ImmutableTerm term) {
            if (!(term instanceof ImmutableFunctionalTerm))
                return term;
            var f = (ImmutableFunctionalTerm) term;
            var fs = f.getFunctionSymbol();
            if (fs instanceof DBOrFunctionSymbol) {
                return negate(termFactory.getImmutableFunctionalTerm(
                                termFactory.getDBFunctionSymbolFactory().getDBAnd(fs.getArity()),
                                negate(f.getTerms())));
            }
            else if (fs instanceof DBAndFunctionSymbol) {
                return negate(termFactory.getImmutableFunctionalTerm(
                                termFactory.getDBFunctionSymbolFactory().getDBOr(fs.getArity()),
                                negate(f.getTerms())));
            }
            else {
                return termFactory.getImmutableFunctionalTerm(
                        fs,
                        f.getTerms().stream()
                                .map(this::transformTerm)
                                .collect(ImmutableCollectors.toList()));
            }
        }

        private ImmutableList<ImmutableTerm> negate(ImmutableList<? extends ImmutableTerm> terms) {
            return terms.stream()
                    .map(this::transformTerm)
                    .map(this::negate)
                    .collect(ImmutableCollectors.toList());
        }

        private ImmutableTerm negate(ImmutableTerm term) {
            return termFactory.getImmutableFunctionalTerm(
                    termFactory.getDBFunctionSymbolFactory().getDBNot(), term);
        }
    }
}
