package it.unibz.inf.ontop.iq.view.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.view.OntopViewUnfolder;
import it.unibz.inf.ontop.dbschema.OntopViewDefinition;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;

public class OntopViewUnfolderImpl implements OntopViewUnfolder {

    protected final CoreSingletons coreSingletons;
    protected final IntermediateQueryFactory iqFactory;

    @Inject
    protected OntopViewUnfolderImpl(CoreSingletons coreSingletons) {
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
        return new MaxLevelViewUnfoldingTransformer(maxLevel, variableGenerator, coreSingletons)
                .transform(tree);
    }

    /**
     * Recursive
     */
    private int extractMaxLevel(IQTree tree) {
        if (tree.getRootNode() instanceof ExtensionalDataNode) {
            RelationDefinition relationDefinition = ((ExtensionalDataNode) tree.getRootNode()).getRelationDefinition();
            return (relationDefinition instanceof OntopViewDefinition)
                    ? ((OntopViewDefinition) relationDefinition).getLevel()
                    : 0;
        }
        else {
            return tree.getChildren().stream()
                    .reduce(0,
                            (l, c) -> Math.max(l, extractMaxLevel(c)),
                            Math::max);
        }
    }


    protected static class MaxLevelViewUnfoldingTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        protected final int maxLevel;
        protected final VariableGenerator variableGenerator;
        protected final SubstitutionFactory substitutionFactory;
        protected final QueryTransformerFactory transformerFactory;

        protected MaxLevelViewUnfoldingTransformer(int maxLevel, VariableGenerator variableGenerator,
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
            if (relationDefinition instanceof OntopViewDefinition) {
                OntopViewDefinition viewDefinition = (OntopViewDefinition) relationDefinition;
                return viewDefinition.getLevel() < maxLevel
                        ? dataNode
                        : merge(dataNode, viewDefinition.getIQ());
            }
            else
                return dataNode;
        }

        protected IQTree merge(ExtensionalDataNode dataNode, IQ definition) {
            InjectiveVar2VarSubstitution renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(
                    variableGenerator, definition.getTree().getKnownVariables());

            IQ renamedDefinition = renamingSubstitution.isEmpty()
                    ? definition
                    : transformerFactory.createRenamer(renamingSubstitution).transform(definition);

            ImmutableSubstitution<VariableOrGroundTerm> descendingSubstitution = extractSubstitution(
                    renamingSubstitution.applyToVariableArguments(renamedDefinition.getProjectionAtom().getArguments()),
                    dataNode.getArgumentMap());

            IQTree substitutedDefinition = renamedDefinition.getTree()
                    .applyDescendingSubstitution(descendingSubstitution, Optional.empty());

            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(dataNode.getVariables()),
                    substitutedDefinition)
                    .normalizeForOptimization(variableGenerator);
        }

        protected ImmutableSubstitution<VariableOrGroundTerm> extractSubstitution(
                ImmutableList<Variable> sourceAtomArguments,
                ImmutableMap<Integer, ? extends VariableOrGroundTerm> targetArgumentMap) {

            ImmutableMap<Variable, VariableOrGroundTerm> newMap = targetArgumentMap.entrySet().stream()
                    .collect(ImmutableCollectors.toMap(
                            e -> sourceAtomArguments.get(e.getKey()),
                            Map.Entry::getValue
                    ));

            return substitutionFactory.getSubstitution(newMap);
        }
    }


}
