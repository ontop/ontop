package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeExtendedTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBAndFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBOrFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.NonDeterministicDBFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CastBooleanExpressionsInFilterNormalizer extends DefaultRecursiveIQTreeExtendedTransformer<VariableGenerator> implements DialectExtraNormalizer {

    private final TermFactory termFactory;
    private final TypeFactory typeFactory;

    @Inject
    protected CastBooleanExpressionsInFilterNormalizer(CoreSingletons coreSingletons) {
        super(coreSingletons);
        this.termFactory = coreSingletons.getTermFactory();
        this.typeFactory = coreSingletons.getTypeFactory();
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptTransformer(this, variableGenerator);
    }

    /*@Override
    public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child, VariableGenerator context) {
        var childTree = applyTransformerToDescendantTree(child, context);
        var condition = rootNode.getFilterCondition();
        var normalizedCondition = normalize(condition);
        var newFilter = iqFactory.createFilterNode(normalizedCondition);
        return iqFactory.createUnaryIQTree(newFilter, childTree);
    }

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children, VariableGenerator context) {
        var childTrees = children.stream()
                .map(child -> applyTransformerToDescendantTree(child, context))
                .collect(ImmutableCollectors.toList());
        var optionalCondition = rootNode.getOptionalFilterCondition();
        if(optionalCondition.isEmpty())
            return iqFactory.createNaryIQTree(rootNode, childTrees);

        var condition = optionalCondition.get();
        var normalizedCondition = normalize(condition);
        var newInnerJoin = iqFactory.createInnerJoinNode(normalizedCondition);
        return iqFactory.createNaryIQTree(newInnerJoin, childTrees);
    }

    @Override
    public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild, VariableGenerator context) {
        var leftTree = applyTransformerToDescendantTree(leftChild, context);
        var rightTree = applyTransformerToDescendantTree(rightChild, context);

        var optionalCondition = rootNode.getOptionalFilterCondition();
        if(optionalCondition.isEmpty())
            return iqFactory.createBinaryNonCommutativeIQTree(rootNode, leftTree, rightTree);

        var condition = optionalCondition.get();
        var normalizedCondition = normalize(condition);
        var newLeftJoin = iqFactory.createLeftJoinNode(normalizedCondition);
        return iqFactory.createBinaryNonCommutativeIQTree(newLeftJoin, leftTree, rightTree);
    }*/

    /**
     * Applies the transformer to a descendant tree, and rebuilds the parent tree.
     *
     * The root node of the parent tree must be a ConstructionNode, a SliceNode, a DistinctNode or an OrderByNode
     */
    private IQTree applyTransformerToDescendantTree(IQTree currentDescendantTree, VariableGenerator variableGenerator) {
        //Recursive
        IQTree newDescendantTree = transform(currentDescendantTree, variableGenerator);

        return currentDescendantTree.equals(newDescendantTree)
            ? currentDescendantTree
            : newDescendantTree;
    }

    private ImmutableExpression normalize(ImmutableExpression expression) {
        if(!(expression.getFunctionSymbol() instanceof DBAndFunctionSymbol || expression.getFunctionSymbol() instanceof DBOrFunctionSymbol)) {
            return expression;
        }
        var children = expression.getTerms().stream()
                .map(term -> normalize(term))
                .collect(ImmutableCollectors.toList());
        return termFactory.getImmutableExpression(expression.getFunctionSymbol(), children);
    }

    private ImmutableTerm normalize(ImmutableTerm term) {
        return termFactory.getImmutableFunctionalTerm(
                termFactory.getDBFunctionSymbolFactory().getDBCastFunctionSymbol(typeFactory.getDBTypeFactory().getDBBooleanType()),
                term
        );
    }
}
