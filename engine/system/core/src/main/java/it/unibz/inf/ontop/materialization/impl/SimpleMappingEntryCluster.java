package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.materialization.MappingEntryCluster;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * Its tree is composed of one construction node and one distinct-variable-only extensional node
 */
public class SimpleMappingEntryCluster extends AbstractMappingEntryCluster implements MappingEntryCluster {
    private final ExtensionalDataNode dataNode;

    public SimpleMappingEntryCluster (IQTree tree,
                                     RDFFactTemplates rdfTemplates,
                                     VariableGenerator variableGenerator,
                                     IntermediateQueryFactory iqFactory,
                                     SubstitutionFactory substitutionFactory) {
        super(tree, rdfTemplates, variableGenerator, iqFactory, substitutionFactory);

        this.dataNode = (ExtensionalDataNode) tree.getChildren().get(0);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SimpleMappingEntryCluster) {
            SimpleMappingEntryCluster that = (SimpleMappingEntryCluster) other;
            return this.tree.equals(that.tree);
        }
        return false;
    }

    @Override
    public ImmutableList<ExtensionalDataNode> getDataNodes() {
        return ImmutableList.of(dataNode);
    }

    @Override
    public Optional<MappingEntryCluster> merge(MappingEntryCluster otherCluster) {

        // Not simple but having a potential for merging implemented somewhere else
        if (otherCluster instanceof FilterMappingEntryCluster
                || otherCluster instanceof DictionaryPatternMappingEntryCluster) {
            return otherCluster.merge(this);
        }

        if (!(otherCluster instanceof SimpleMappingEntryCluster)) {
            return Optional.empty();
        }

        SimpleMappingEntryCluster otherSimpleCluster = (SimpleMappingEntryCluster) otherCluster;
        if (!dataNode.getRelationDefinition().equals(otherSimpleCluster.dataNode.getRelationDefinition())) {
            return Optional.empty();
        }

        return Optional.of(mergeWithSimpleCluster(otherSimpleCluster));
    }

    private MappingEntryCluster mergeWithSimpleCluster(SimpleMappingEntryCluster otherSimpleCluster) {
        variableGenerator.registerAdditionalVariables(otherSimpleCluster.variableGenerator.getKnownVariables());
        SimpleMappingEntryCluster otherRenamed = otherSimpleCluster.renameConflictingVariables(variableGenerator);

        ConstructionNode constructionNodeAfterUnification = unify(dataNode, otherRenamed.dataNode);

        ExtensionalDataNode mergedDataNode = mergeDataNodes(dataNode, otherRenamed.dataNode);

        ConstructionNode topConstructionNode = createMergedTopConstructionNode(
                (ConstructionNode) tree.getRootNode(),
                (ConstructionNode) otherRenamed.tree.getRootNode());

        IQTree newTree = iqFactory.createUnaryIQTree(
                topConstructionNode,
                iqFactory.createUnaryIQTree(
                        constructionNodeAfterUnification,
                        mergedDataNode));

        RDFFactTemplates mergedRDFTemplates = rdfTemplates.merge(otherRenamed.rdfTemplates);

        return compressCluster(
                newTree.normalizeForOptimization(variableGenerator),
                mergedRDFTemplates);
    }


    public SimpleMappingEntryCluster renameConflictingVariables(VariableGenerator conflictingVariableGenerator) {
        InjectiveSubstitution<Variable> renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(
                conflictingVariableGenerator, tree.getKnownVariables());
        IQTree renamedTree = tree.applyFreshRenaming(renamingSubstitution);
        RDFFactTemplates renamedRDFTemplates = rdfTemplates.apply(renamingSubstitution);

        return new SimpleMappingEntryCluster(
                renamedTree,
                renamedRDFTemplates,
                variableGenerator,
                iqFactory,
                substitutionFactory);
    }

    @Override
    protected MappingEntryCluster buildCluster(IQTree compressedTree, RDFFactTemplates compressedTemplates) {
        return new SimpleMappingEntryCluster(
                compressedTree,
                compressedTemplates,
                variableGenerator,
                iqFactory,
                substitutionFactory);
    }
}
