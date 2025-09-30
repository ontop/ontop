package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableSet;
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
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

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
        Context context = new Context(constructionNode.getVariables(), variableGenerator, treeCache);
        return context.normalize(constructionNode, child);
    }


    private class Context extends NormalizationContext {

        Context(ImmutableSet<Variable> projectedVariables, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(projectedVariables, variableGenerator, treeCache, ConstructionNormalizerImpl.this.iqTreeTools);
        }

        private ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization getSubstitutionNormalization(Substitution<ImmutableTerm> substitution, IQTree child) {
            var variableNullability = child.getVariableNullability();
            return substitutionNormalizer.normalizeSubstitution(
                    substitution.transform(t -> t.simplify(variableNullability)),
                    projectedVariables);
        }

        IQTree normalize(ConstructionNode constructionNode, IQTree child) {
            IQTree shrunkChild = removeNonRequiredVariables(constructionNode,
                                        normalizeSubTreeRecursively(child));

            if (shrunkChild.isDeclaredAsEmpty()) {
                return createEmptyNode();
            }

            var shrunkChildConstruction = IQTreeTools.UnaryIQTreeDecomposition.of(shrunkChild, ConstructionNode.class);
            if (shrunkChildConstruction.isPresent()) {
                var substitutionNormalization = getSubstitutionNormalization(
                        shrunkChildConstruction.getNode().getSubstitution()
                                .compose(constructionNode.getSubstitution()),
                        shrunkChildConstruction.getChild());

                ConstructionNode newConstructionNode = substitutionNormalization.createConstructionNode();
                IQTree updatedChild = substitutionNormalization.applyDownRenamingSubstitution(shrunkChildConstruction.getChild());

                IQTree newGrandChild = normalizeSubTreeRecursively(
                        removeNonRequiredVariables(newConstructionNode, updatedChild));

                // check the newConstructionNode is useless
                return newGrandChild.getVariables().equals(projectedVariables)
                        ? newGrandChild
                        : iqFactory.createUnaryIQTree(newConstructionNode, newGrandChild, getNormalizedTreeCache(true));
            }

            // check that constructionNode is useless
            if (shrunkChild.getVariables().equals(projectedVariables)) {
                return shrunkChild;
            }

            var substitutionNormalization = getSubstitutionNormalization(
                    constructionNode.getSubstitution(),
                    shrunkChild);

            Optional<ConstructionNode> optionalTopConstructionNode = substitutionNormalization.createOptionalConstructionNode();
            IQTree updatedChild = substitutionNormalization.applyDownRenamingSubstitution(shrunkChild);
            if (optionalTopConstructionNode.isPresent()) {
                IQTree newChild = normalizeSubTreeRecursively(
                        removeNonRequiredVariables(optionalTopConstructionNode.get(), updatedChild));

                return iqFactory.createUnaryIQTree(
                        optionalTopConstructionNode.get(),
                        newChild,
                        getNormalizedTreeCache(true));
            }

            IQTree newChild = normalizeSubTreeRecursively(updatedChild);
            return iqTreeTools.unaryIQTreeBuilder(projectedVariables)
                    .build(newChild);
        }

        IQTree removeNonRequiredVariables(ConstructionNode constructionNode, IQTree tree) {
            return notRequiredVariableRemover.optimize(tree, constructionNode.getChildVariables(), variableGenerator);
        }
    }
}
