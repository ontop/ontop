package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.NaryOperatorNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.iq.node.ValuesNode;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.stream.IntStream;

/**
 * Many databases do not support VALUES, they use this normalizer and replace ValuesNodes
 * with a union of Construction/True pairs.
 *
 * @author Lukas Sundqvist
 */
public class ConvertValuesToUnionNormalizer implements DialectExtraNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    protected  ConvertValuesToUnionNormalizer(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
    }


    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return containsValuesNode(tree)
                ? normalize(tree)
                : tree;
    }

    /**
     * Recursive. TODO: uncertain if it is more efficient to have this "checker" method or not
     */
    private boolean containsValuesNode(IQTree tree) {
        return tree.isLeaf()
                ? (tree instanceof ValuesNode)
                : tree.getChildren().stream()
                    .anyMatch(this::containsValuesNode);
    }

    /**
     * Recursive.
     */
    private IQTree normalize(IQTree tree) {
        if (tree.isLeaf()) {
            return (tree instanceof ValuesNode)
                    ? convertToUnion((ValuesNode) tree)
                    : tree;
        }
        // Note, we assume here that every IQTree is either a LeafIQTree, NaryOperatorNode, or UnaryOperatorNode
        return (tree.getRootNode() instanceof NaryOperatorNode)
                ? iqFactory.createNaryIQTree(
                    (NaryOperatorNode) tree.getRootNode(),
                    tree.getChildren().stream()
                        .map(this::normalize)
                        .collect(ImmutableCollectors.toList()))
                : iqFactory.createUnaryIQTree(
                    (UnaryOperatorNode) tree.getRootNode(),
                    normalize(tree.getChildren().get(0)));
    }

    private IQTree convertToUnion(ValuesNode valuesNode) {
        ImmutableList<Variable> orderedVariables = valuesNode.getOrderedVariables();
        ImmutableList<ImmutableSubstitution<ImmutableTerm>> substitutionList =
                valuesNode.getValues().stream()
                        .map(tuple -> substitutionFactory.getSubstitution(
                                IntStream.range(0, orderedVariables.size())
                                    .boxed()
                                    .collect(ImmutableCollectors.toMap(
                                        orderedVariables::get,
                                        i -> (ImmutableTerm) tuple.get(i)))))
                .collect(ImmutableCollectors.toList());

        return iqFactory.createNaryIQTree(
                iqFactory.createUnionNode(
                        valuesNode.getVariables()),
                substitutionList.stream()
                        .map(this::createConstructionTrueTree)
                        .collect(ImmutableCollectors.toList()));
    }

    private IQTree createConstructionTrueTree(ImmutableSubstitution<ImmutableTerm> substitution) {
        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(substitution.getDomain(), substitution),
                iqFactory.createTrueNode());
    }
}
