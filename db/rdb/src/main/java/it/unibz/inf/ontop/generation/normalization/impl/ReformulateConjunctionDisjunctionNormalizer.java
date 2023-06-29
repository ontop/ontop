package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeExtendedTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBAndFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBOrFunctionSymbol;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

/**
 * The CDataDynamoDB driver seems to be struggling with the boolean operators AND and OR. However, converting them to
 * the opposite operators using De Morgan's law seems to fix these issues.
 */
public class ReformulateConjunctionDisjunctionNormalizer extends DefaultRecursiveIQTreeExtendedTransformer<VariableGenerator> implements DialectExtraNormalizer {

    private final TermFactory termFactory;

    @Inject
    protected ReformulateConjunctionDisjunctionNormalizer(CoreSingletons coreSingletons) {
        super(coreSingletons);
        this.termFactory = coreSingletons.getTermFactory();
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptTransformer(this, variableGenerator);
    }

    @Override
    public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child, VariableGenerator context) {
        var expression = rootNode.getFilterCondition();
        var newExpression = transformTerm(expression);
        if(newExpression.equals(expression))
            return super.transformFilter(tree, rootNode, child, context);
        return iqFactory.createUnaryIQTree(
                iqFactory.createFilterNode((ImmutableExpression) newExpression),
                child.acceptTransformer(this, context)
        );
    }

    @Override
    public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild, VariableGenerator context) {
        var expression = rootNode.getOptionalFilterCondition();
        var newExpression = expression.map(this::transformTerm);
        if(newExpression.isEmpty() || newExpression.equals(expression))
            return super.transformLeftJoin(tree, rootNode, leftChild, rightChild, context);
        return iqFactory.createBinaryNonCommutativeIQTree(
                iqFactory.createLeftJoinNode((ImmutableExpression) newExpression.get()),
                leftChild.acceptTransformer(this, context),
                rightChild.acceptTransformer(this, context)
        );
    }

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children, VariableGenerator context) {
        var expression = rootNode.getOptionalFilterCondition();
        var newExpression = expression.map(this::transformTerm);
        if(newExpression.isEmpty() || newExpression.equals(expression))
            return super.transformInnerJoin(tree, rootNode, children, context);
        return iqFactory.createNaryIQTree(
                iqFactory.createInnerJoinNode((ImmutableExpression) newExpression.get()),
                children.stream()
                        .map(child -> child.acceptTransformer(this, context))
                        .collect(ImmutableCollectors.toList())
        );
    }

    @Override
    public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child, VariableGenerator context) {
        var newSubstitution = rootNode.getSubstitution().transform(this::transformTerm);
        var newConstruction = iqFactory.createConstructionNode(rootNode.getVariables(), newSubstitution);
        if(newConstruction.equals(rootNode))
            return super.transformConstruction(tree, rootNode, child, context);
        return iqFactory.createUnaryIQTree(
                newConstruction,
                child.acceptTransformer(this, context)
        );
    }

    @Override
    public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child, VariableGenerator context) {
        var expressions = rootNode.getComparators().stream()
                .map(c -> iqFactory.createOrderComparator((NonGroundTerm) transformTerm(c.getTerm()), c.isAscending()))
                .collect(ImmutableCollectors.toList());
        var newOrderBy = iqFactory.createOrderByNode(expressions);
        if(newOrderBy.equals(rootNode))
            return super.transformOrderBy(tree, rootNode, child, context);
        return iqFactory.createUnaryIQTree(
                newOrderBy,
                child.acceptTransformer(this, context)
        );
    }

    @Override
    public IQTree transformAggregation(IQTree tree, AggregationNode rootNode, IQTree child, VariableGenerator context) {
        var newSubstitution = rootNode.getSubstitution().transform(t -> (ImmutableFunctionalTerm) transformTerm(t));
        var newAggregation = iqFactory.createAggregationNode(rootNode.getGroupingVariables(), newSubstitution);
        if(newAggregation.equals(rootNode))
            return super.transformAggregation(tree, rootNode, child, context);
        return iqFactory.createUnaryIQTree(
                newAggregation,
                child.acceptTransformer(this, context)
        );

    }

    private ImmutableTerm transformTerm(ImmutableTerm term) {
        if(!(term instanceof ImmutableFunctionalTerm))
            return term;
        var f = (ImmutableFunctionalTerm) term;
        if(f.getFunctionSymbol() instanceof DBOrFunctionSymbol) {
            var or = (DBOrFunctionSymbol) f.getFunctionSymbol();
            return termFactory.getImmutableFunctionalTerm(
                    termFactory.getDBFunctionSymbolFactory().getDBNot(),
                    termFactory.getImmutableFunctionalTerm(
                        termFactory.getDBFunctionSymbolFactory().getDBAnd(or.getArity()),
                        f.getTerms().stream()
                                .map(this::transformTerm)
                                .map(t -> termFactory.getImmutableFunctionalTerm(
                                        termFactory.getDBFunctionSymbolFactory().getDBNot(),
                                        t))
                                .collect(ImmutableCollectors.toList()))
            );
        }
        if(f.getFunctionSymbol() instanceof DBAndFunctionSymbol) {
            var and = (DBAndFunctionSymbol) f.getFunctionSymbol();
            return termFactory.getImmutableFunctionalTerm(
                    termFactory.getDBFunctionSymbolFactory().getDBNot(),
                    termFactory.getImmutableFunctionalTerm(
                            termFactory.getDBFunctionSymbolFactory().getDBOr(and.getArity()),
                            f.getTerms().stream()
                                    .map(this::transformTerm)
                                    .map(t -> termFactory.getImmutableFunctionalTerm(
                                            termFactory.getDBFunctionSymbolFactory().getDBNot(),
                                            t))
                                    .collect(ImmutableCollectors.toList()))
            );
        }
        return termFactory.getImmutableFunctionalTerm(
                f.getFunctionSymbol(),
                f.getTerms().stream()
                        .map(this::transformTerm)
                        .collect(ImmutableCollectors.toList())
        );
    }
}
