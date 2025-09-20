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
        IQTree liftedChild = child.normalizeForOptimization(variableGenerator);
        IQTree shrunkChild = notRequiredVariableRemover.optimize(liftedChild, constructionNode.getChildVariables(), variableGenerator);
        var shrunkChildConstruction = IQTreeTools.UnaryIQTreeDecomposition.of(shrunkChild, ConstructionNode.class);
        if (shrunkChildConstruction.isPresent()) {
            return mergeWithChild(constructionNode, shrunkChildConstruction.getNode(), shrunkChildConstruction.getChild(), treeCache, variableGenerator);
        }
        else if (shrunkChild.isDeclaredAsEmpty()) {
            return iqFactory.createEmptyNode(constructionNode.getVariables());
        }
        /*
         * If useless, returns the child
         */
        else if (shrunkChild.getVariables().equals(constructionNode.getVariables())) {
            return shrunkChild;
        }
        else {
            ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization normalization = substitutionNormalizer.normalizeSubstitution(
                    constructionNode.getSubstitution().transform(t -> t.simplify(shrunkChild.getVariableNullability())),
                    constructionNode.getVariables());

            IQTree updatedChild = normalization.updateChild(shrunkChild, variableGenerator);

            if (!normalization.getNormalizedSubstitution().isEmpty()) {
                ConstructionNode newTopConstructionNode =
                        iqFactory.createConstructionNode(constructionNode.getVariables(), normalization.getNormalizedSubstitution());

                IQTree newChild = notRequiredVariableRemover.optimize(
                                updatedChild,
                                newTopConstructionNode.getChildVariables(),
                                variableGenerator)
                        .normalizeForOptimization(variableGenerator);

                return iqFactory.createUnaryIQTree(
                        newTopConstructionNode,
                        newChild,
                        treeCache.declareAsNormalizedForOptimizationWithEffect());
            }
            else {
                IQTree newChild = updatedChild
                        .normalizeForOptimization(variableGenerator);

                return iqTreeTools.unaryIQTreeBuilder(constructionNode.getVariables())
                        .build(newChild);
            }
        }
    }

    private IQTree mergeWithChild(ConstructionNode constructionNode, ConstructionNode childConstructionNode, IQTree grandChild, IQTreeCache treeCache, VariableGenerator variableGenerator) {

        ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization substitutionNormalization = substitutionNormalizer.normalizeSubstitution(
                childConstructionNode.getSubstitution().compose(constructionNode.getSubstitution())
                        .transform(t -> t.simplify(grandChild.getVariableNullability())),
                constructionNode.getVariables());

        Substitution<ImmutableTerm> newSubstitution = substitutionNormalization.getNormalizedSubstitution();

        ConstructionNode newConstructionNode = iqFactory.createConstructionNode(constructionNode.getVariables(), newSubstitution);

        IQTree updatedGrandChild = substitutionNormalization.updateChild(grandChild, variableGenerator);
        IQTree newGrandChild = notRequiredVariableRemover.optimize(updatedGrandChild,
                        newConstructionNode.getChildVariables(), variableGenerator)
                .normalizeForOptimization(variableGenerator);

        return newGrandChild.getVariables().equals(newConstructionNode.getVariables())
                ? newGrandChild
                : iqFactory.createUnaryIQTree(newConstructionNode, newGrandChild, treeCache.declareAsNormalizedForOptimizationWithEffect());
    }
}
