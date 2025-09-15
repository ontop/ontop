package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ValuesNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;


/**
 * Many databases do not support VALUES, they use this normalizer and replace ValuesNodes
 * with a union of Construction/True pairs.
 *
 * @author Lukas Sundqvist
 */
public class ConvertValuesToUnionNormalizer implements DialectExtraNormalizer {
    private final SubstitutionFactory substitutionFactory;
    private final IQTreeTools iqTreeTools;
    private final IntermediateQueryFactory iqFactory;
    private final Transformer transformer;

    @Inject
    protected  ConvertValuesToUnionNormalizer(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory, IQTreeTools iqTreeTools) {
        this.substitutionFactory = substitutionFactory;
        this.iqTreeTools = iqTreeTools;
        this.iqFactory = iqFactory;
        this.transformer = new Transformer();
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(transformer);
    }

    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {

        Transformer() {
            super(ConvertValuesToUnionNormalizer.this.iqFactory);
        }

        @Override
        public IQTree transformValues(ValuesNode node) {
            return iqTreeTools.createUnionTree(node.getVariables(),
                    node.getValueMaps().stream()
                            .map(m -> m.entrySet().stream()
                                    .collect(substitutionFactory.<ImmutableTerm>toSubstitution()))
                            .map(substitution -> iqFactory.createUnaryIQTree(
                                    iqTreeTools.createExtendingConstructionNode(ImmutableSet.of(), substitution),
                                    iqFactory.createTrueNode()))
                            .collect(ImmutableCollectors.toList()));
        }
    }
}
