package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.FlattenNode;
import it.unibz.inf.ontop.iq.node.normalization.FlattenNormalizer;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;


public class FlattenNormalizerImpl implements FlattenNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    private FlattenNormalizerImpl(IntermediateQueryFactory iqFactory, IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    public IQTree normalizeForOptimization(FlattenNode flattenNode, IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        Context context = new Context(variableGenerator, treeCache);
        return context.normalize(flattenNode, child);
    }

    private class Context extends NormalizationContext {

        private final IQTreeCache treeCache;

        Context(VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(variableGenerator);
            this.treeCache = treeCache;
        }

        IQTree normalize(FlattenNode flattenNode, IQTree child) {
            IQTree normalizedChild = normalizeSubTreeRecursively(child);

            var construction = IQTreeTools.UnaryIQTreeDecomposition.of(normalizedChild, ConstructionNode.class);
            if (construction.isPresent()) {
                /*
                 * Let c be the root of the child tree before lift, of the form CONSTRUCT[V, S].
                 * Let f be the flattened variable.
                 * Let o be the output variable of flattening.
                 * Let i be the (optional) index variable of flattening.
                 * <p>
                 * We split S into S_f and S', where S_f contains:
                 *    - the definition of f
                 *    - variable definitions that depend on f.
                 * Note that these two cases are exclusive.
                 * Note also that S_f may be empty.
                 *
                 * If S' is nonempty, then we lift it above the flatten node,
                 * i.e. we create a parent CONSTRUCT[V',S'],
                 * where
                 *   V' = (V minus {f}) union {o,i}
                 */
                Variable flattenedVar = flattenNode.getFlattenedVariable();
                Substitution<ImmutableTerm> substitution = construction.getNode().getSubstitution();

                Substitution<ImmutableTerm> flattenedVarSubstitution = substitution.builder()
                        .restrict((v, t) -> v.equals(flattenedVar) || t.getVariableStream().anyMatch(tv -> tv.equals(flattenedVar)))
                        .build();

                Substitution<ImmutableTerm> primeSubstitution = substitution
                        .removeFromDomain(flattenedVarSubstitution.getDomain());

                // Nothing can be lifted, declare the new tree normalized
                if (!primeSubstitution.isEmpty()) {
                    ConstructionNode newParentCn = iqFactory.createConstructionNode(
                            Sets.union(
                                    flattenNode.getLocallyDefinedVariables(),
                                    Sets.difference(construction.getNode().getVariables(), flattenNode.getLocallyRequiredVariables())).immutableCopy(),
                            primeSubstitution);

                    IQTree updatedChild = normalizeSubTreeRecursively(
                            iqTreeTools.unaryIQTreeBuilder()
                                    .append(iqTreeTools.createOptionalConstructionNode(
                                            () -> Sets.union(
                                                    flattenNode.getLocallyRequiredVariables(),
                                                    Sets.difference(newParentCn.getChildVariables(), flattenNode.getLocallyDefinedVariables())).immutableCopy(),
                                            flattenedVarSubstitution))
                                    .build(construction.getChild()));

                    return iqFactory.createUnaryIQTree(
                            newParentCn,
                            iqFactory.createUnaryIQTree(flattenNode, updatedChild).normalizeForOptimization(variableGenerator),
                            getNormalizedTreeCache());
                }
            }

            // Nothing can be lifted, declare the new tree normalized
            return iqFactory.createUnaryIQTree(flattenNode, normalizedChild, getNormalizedTreeCache());
        }

        // Among the trees that are created, the outermost (aka output) tree is declared as normalized.
        // The other ones (if any) are normalized immediately after they are created.
        // without effect!
        IQTreeCache getNormalizedTreeCache() {
            return treeCache.declareAsNormalizedForOptimizationWithoutEffect();
        }
    }
}
