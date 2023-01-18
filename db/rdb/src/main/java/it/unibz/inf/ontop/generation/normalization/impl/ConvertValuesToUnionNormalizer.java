package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ValuesNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
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
public class ConvertValuesToUnionNormalizer extends DefaultRecursiveIQTreeVisitingTransformer implements DialectExtraNormalizer {
    private final SubstitutionFactory substitutionFactory;

    @Inject
    protected  ConvertValuesToUnionNormalizer(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory) {
        super(iqFactory);
        this.substitutionFactory = substitutionFactory;
    }


    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return transform(tree);
    }

    @Override
    public IQTree transformValues(ValuesNode node) {
        return convertToUnion(node);
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
