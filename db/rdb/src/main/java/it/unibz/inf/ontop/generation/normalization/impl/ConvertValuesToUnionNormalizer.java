package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ValuesNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;


/**
 * Many databases do not support VALUES, they use this normalizer and replace ValuesNodes
 * with a union of Construction/True pairs.
 *
 * @author Lukas Sundqvist
 */
public class ConvertValuesToUnionNormalizer extends DefaultRecursiveIQTreeVisitingTransformer implements DialectExtraNormalizer {
    private final SubstitutionFactory substitutionFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    protected  ConvertValuesToUnionNormalizer(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory, IQTreeTools iqTreeTools) {
        super(iqFactory);
        this.substitutionFactory = substitutionFactory;
        this.iqTreeTools = iqTreeTools;
    }


    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(this);
    }

    @Override
    public IQTree transformValues(ValuesNode node) {
        return convertToUnion(node);
    }

    private IQTree convertToUnion(ValuesNode valuesNode) {
        ImmutableList<Variable> orderedVariables = valuesNode.getOrderedVariables();
        ImmutableList<Substitution<ImmutableTerm>> substitutionList =
                valuesNode.getValues().stream()
                        .map(tuple -> substitutionFactory.<ImmutableTerm>getSubstitution(orderedVariables, tuple))
                .collect(ImmutableCollectors.toList());

        return iqTreeTools.createUnionTree(valuesNode.getVariables(),
                substitutionList.stream()
                        .map(substitution -> iqFactory.createUnaryIQTree(
                                iqTreeTools.createExtendingConstructionNode(ImmutableSet.of(), substitution),
                                iqFactory.createTrueNode()))
                        .collect(ImmutableCollectors.toList()));
    }
}
