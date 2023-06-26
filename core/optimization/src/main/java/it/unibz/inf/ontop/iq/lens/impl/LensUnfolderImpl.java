package it.unibz.inf.ontop.iq.lens.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.lens.LensUnfolder;
import it.unibz.inf.ontop.dbschema.Lens;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;

public class LensUnfolderImpl implements LensUnfolder {

    protected final CoreSingletons coreSingletons;
    protected final IntermediateQueryFactory iqFactory;

    @Inject
    protected LensUnfolderImpl(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();
        int maxLevel = extractMaxLevel(initialTree);
        if (maxLevel < 1)
            return query;
        IQTree newTree = transformTree(initialTree, query.getVariableGenerator(), maxLevel);
        return newTree.equals(initialTree)
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree)
                .normalizeForOptimization();
    }

    protected IQTree transformTree(IQTree tree, VariableGenerator variableGenerator, int maxLevel) {
        return new MaxLevelLensUnfoldingTransformer(maxLevel, variableGenerator, coreSingletons)
                .transform(tree);
    }

    /**
     * Recursive
     */
    private int extractMaxLevel(IQTree tree) {
        if (tree.getRootNode() instanceof ExtensionalDataNode) {
            RelationDefinition relationDefinition = ((ExtensionalDataNode) tree.getRootNode()).getRelationDefinition();
            return (relationDefinition instanceof Lens)
                    ? ((Lens) relationDefinition).getLevel()
                    : 0;
        }
        else {
            return tree.getChildren().stream()
                    .reduce(0,
                            (l, c) -> Math.max(l, extractMaxLevel(c)),
                            Math::max);
        }
    }


    protected static class MaxLevelLensUnfoldingTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        protected final int maxLevel;
        protected final VariableGenerator variableGenerator;
        protected final SubstitutionFactory substitutionFactory;
        protected final QueryTransformerFactory transformerFactory;

        protected MaxLevelLensUnfoldingTransformer(int maxLevel, VariableGenerator variableGenerator,
                                                   CoreSingletons coreSingletons) {
            super(coreSingletons);
            this.maxLevel = maxLevel;
            this.variableGenerator = variableGenerator;
            substitutionFactory = coreSingletons.getSubstitutionFactory();
            transformerFactory = coreSingletons.getQueryTransformerFactory();
        }

        @Override
        public IQTree transformExtensionalData(ExtensionalDataNode dataNode) {
            RelationDefinition relationDefinition = dataNode.getRelationDefinition();
            if (relationDefinition instanceof Lens) {
                Lens lens = (Lens) relationDefinition;
                return lens.getLevel() < maxLevel
                        ? dataNode
                        : merge(dataNode, lens.getIQ());
            }
            else
                return dataNode;
        }

        protected IQTree merge(ExtensionalDataNode dataNode, IQ definition) {
            InjectiveSubstitution<Variable> renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(
                    variableGenerator, definition.getTree().getKnownVariables());

            IQ renamedDefinition = transformerFactory.createRenamer(renamingSubstitution).transform(definition);

            ImmutableList<Variable> sourceAtomArguments = substitutionFactory.apply(
                    renamingSubstitution,
                    renamedDefinition.getProjectionAtom().getArguments());

            Substitution<VariableOrGroundTerm> descendingSubstitution = dataNode.getArgumentMap().entrySet().stream()
                    .collect(substitutionFactory.toSubstitutionSkippingIdentityEntries(
                            e -> sourceAtomArguments.get(e.getKey()),
                            Map.Entry::getValue));

            IQTree substitutedDefinition = renamedDefinition.getTree()
                    .applyDescendingSubstitution(descendingSubstitution, Optional.empty(), variableGenerator);

            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(dataNode.getVariables()),
                    substitutedDefinition)
                    .normalizeForOptimization(variableGenerator);
        }

    }


}
