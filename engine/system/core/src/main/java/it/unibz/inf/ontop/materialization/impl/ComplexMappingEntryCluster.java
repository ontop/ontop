package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.materialization.MappingEntryCluster;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Optional;

public class ComplexMappingEntryCluster implements MappingEntryCluster {
    private final IQTree tree;
    private final RDFFactTemplates rdfFactTemplates;
    private final SubstitutionFactory substitutionFactory;

    public ComplexMappingEntryCluster(IQTree tree, RDFFactTemplates rdfFactTemplates, SubstitutionFactory substitutionFactory) {
        this.tree = tree;
        this.rdfFactTemplates = rdfFactTemplates;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public Optional<MappingEntryCluster> merge(MappingEntryCluster other) {
        return Optional.empty();
    }

    @Override
    public IQTree getIQTree() {
        return tree;
    }

    @Override
    public RDFFactTemplates getRDFFactTemplates() {
        return rdfFactTemplates;
    }

    @Override
    public ImmutableList<ExtensionalDataNode> getDataNodes() {
        return findRelations(tree);
    }

    @Override
    public MappingEntryCluster renameConflictingVariables(VariableGenerator conflictingVariableGenerator) {
        InjectiveSubstitution<Variable> renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(
                conflictingVariableGenerator, tree.getKnownVariables());
        IQTree renamedTree = tree.applyFreshRenaming(renamingSubstitution);
        RDFFactTemplates renamedRDFTemplates = rdfFactTemplates.apply(renamingSubstitution);

        return new ComplexMappingEntryCluster(renamedTree, renamedRDFTemplates, substitutionFactory);
    }

    public ImmutableList<ExtensionalDataNode> findRelations(IQTree tree) {
        if (tree.getChildren().isEmpty()) {
            if (tree.getRootNode() instanceof ExtensionalDataNode) {
                return ImmutableList.of(((ExtensionalDataNode) tree.getRootNode()));
            }
        } else {
            return tree.getChildren().stream()
                    .map(this::findRelations)
                    .flatMap(Collection::stream)
                    .collect(ImmutableCollectors.toList());
        }
        return ImmutableList.of();
    }
}
