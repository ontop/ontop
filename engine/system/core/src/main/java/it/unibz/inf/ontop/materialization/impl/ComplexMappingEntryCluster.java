package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.materialization.MappingEntryCluster;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Optional;

public class ComplexMappingEntryCluster extends AbstractMappingEntryCluster implements MappingEntryCluster {

    public ComplexMappingEntryCluster(IQTree tree,
                                      RDFFactTemplates rdfFactTemplates,
                                      VariableGenerator variableGenerator,
                                      IntermediateQueryFactory iqFactory,
                                      SubstitutionFactory substitutionFactory,
                                      TermFactory termFactory) {
        super(tree, rdfFactTemplates, variableGenerator, iqFactory, substitutionFactory, termFactory);
    }

    @Override
    public Optional<MappingEntryCluster> merge(MappingEntryCluster other) {
        return Optional.empty();
    }

    @Override
    public ImmutableList<ExtensionalDataNode> getDataNodes() {
        return findRelations(tree);
    }

    @Override
    protected MappingEntryCluster buildCluster(IQTree compressedTree, RDFFactTemplates compressedTemplates) {
        return new ComplexMappingEntryCluster(compressedTree,
                compressedTemplates,
                variableGenerator,
                iqFactory,
                substitutionFactory,
                termFactory);
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
