package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.NotRequiredVariableRemover;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

@Singleton
public class ConstructionNormalizerImpl implements ConstructionNormalizer {
    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;
    private final NotRequiredVariableRemover notRequiredVariableRemover;
    private final ConstructionSubstitutionNormalizer substitutionNormalizer;

    @Inject
    private ConstructionNormalizerImpl(IntermediateQueryFactory iqFactory, IQTreeTools iqTreeTools, NotRequiredVariableRemover notRequiredVariableRemover, ConstructionSubstitutionNormalizer substitutionNormalizer) {
        this.iqFactory = iqFactory;
        this.iqTreeTools = iqTreeTools;
        this.notRequiredVariableRemover = notRequiredVariableRemover;
        this.substitutionNormalizer = substitutionNormalizer;
    }

    @Override
    public IQTree normalizeForOptimization(ConstructionNode constructionNode, IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        Context context = new Context(variableGenerator, treeCache);
        return context.normalize(constructionNode, child);
    }

    private class Context extends NormalizationContext {
        private final IQTreeCache treeCache;

        Context(VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(variableGenerator);
            this.treeCache = treeCache;
        }

        IQTree normalize(ConstructionNode constructionNode, IQTree child) {
            IQTree shrunkChild = removeNonRequiredVariables(constructionNode,
                                        normalizeSubTreeRecursively(child));

            if (shrunkChild.isDeclaredAsEmpty()) {
                return iqFactory.createEmptyNode(constructionNode.getVariables());
            }

            var shrunkChildConstruction = IQTreeTools.UnaryIQTreeDecomposition.of(shrunkChild, ConstructionNode.class);
            if (shrunkChildConstruction.isPresent()) {
                ConstructionNode childConstructionNode = shrunkChildConstruction.getNode();
                IQTree grandChild = shrunkChildConstruction.getChild();

                ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization substitutionNormalization = substitutionNormalizer.normalizeSubstitution(
                        childConstructionNode.getSubstitution().compose(constructionNode.getSubstitution())
                                .transform(t -> t.simplify(grandChild.getVariableNullability())),
                        constructionNode.getVariables());

                Substitution<ImmutableTerm> newSubstitution = substitutionNormalization.getNormalizedSubstitution();

                ConstructionNode newConstructionNode = iqFactory.createConstructionNode(constructionNode.getVariables(), newSubstitution);

                IQTree updatedGrandChild = substitutionNormalization.updateChild(grandChild, variableGenerator);
                IQTree newGrandChild = normalizeSubTreeRecursively(removeNonRequiredVariables(newConstructionNode, updatedGrandChild));

                return newGrandChild.getVariables().equals(newConstructionNode.getVariables())
                        ? newGrandChild
                        : iqFactory.createUnaryIQTree(newConstructionNode, newGrandChild, getNormalizedTreeCache());
            }

            /*
             * If useless, returns the child
             */
            if (shrunkChild.getVariables().equals(constructionNode.getVariables())) {
                return shrunkChild;
            }

            ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization normalization = substitutionNormalizer.normalizeSubstitution(
                    constructionNode.getSubstitution().transform(t -> t.simplify(shrunkChild.getVariableNullability())),
                    constructionNode.getVariables());

            IQTree updatedChild = normalization.updateChild(shrunkChild, variableGenerator);

            if (!normalization.getNormalizedSubstitution().isEmpty()) {
                ConstructionNode newTopConstructionNode =
                        iqFactory.createConstructionNode(constructionNode.getVariables(), normalization.getNormalizedSubstitution());

                IQTree newChild = normalizeSubTreeRecursively(
                        removeNonRequiredVariables(newTopConstructionNode, updatedChild));

                return iqFactory.createUnaryIQTree(
                        newTopConstructionNode,
                        newChild,
                        getNormalizedTreeCache());
            }

            IQTree newChild = normalizeSubTreeRecursively(updatedChild);

            return iqTreeTools.unaryIQTreeBuilder(constructionNode.getVariables())
                    .build(newChild);
        }

        IQTree removeNonRequiredVariables(ConstructionNode constructionNode, IQTree tree) {
            return notRequiredVariableRemover.optimize(tree, constructionNode.getChildVariables(), variableGenerator);
        }

        IQTreeCache getNormalizedTreeCache() {
            return treeCache.declareAsNormalizedForOptimizationWithEffect();
        }
    }
}
