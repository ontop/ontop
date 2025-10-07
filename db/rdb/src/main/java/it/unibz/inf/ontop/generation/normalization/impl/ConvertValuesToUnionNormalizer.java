package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ValuesNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultDelegatingIQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;


/**
 * Many databases do not support VALUES, they use this normalizer and replace ValuesNodes
 * with a union of Construction/True pairs.
 *
 * @author Lukas Sundqvist
 */
public class ConvertValuesToUnionNormalizer extends DefaultDelegatingIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    protected ConvertValuesToUnionNormalizer(CoreSingletons coreSingletons) {
        super(new Transformer(coreSingletons)::transform);
    }

    private static class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        private final SubstitutionFactory substitutionFactory;
        private final IQTreeTools iqTreeTools;

        Transformer(CoreSingletons coreSingletons) {
            super(coreSingletons.getIQFactory());
            this.substitutionFactory = coreSingletons.getSubstitutionFactory();
            this.iqTreeTools = coreSingletons.getIQTreeTools();
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
