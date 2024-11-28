package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.materialization.MappingEntryCluster;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

public abstract class AbstractMappingEntryCluster implements MappingEntryCluster {

    protected final IQTree tree;
    protected final VariableGenerator variableGenerator;
    protected final RDFFactTemplates rdfTemplates;
    protected final IntermediateQueryFactory iqFactory;
    protected final SubstitutionFactory substitutionFactory;

    protected AbstractMappingEntryCluster(IQTree tree,
                                          RDFFactTemplates rdfTemplates,
                                          VariableGenerator variableGenerator,
                                          IntermediateQueryFactory iqFactory,
                                          SubstitutionFactory substitutionFactory) {
        this.tree = tree;
        this.rdfTemplates = rdfTemplates;
        this.variableGenerator = variableGenerator;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public IQTree getIQTree() {
        return tree;
    }

    @Override
    public RDFFactTemplates getRDFFactTemplates() {
        return rdfTemplates;
    }


    protected MappingEntryCluster compressCluster(IQTree normalizedTree, RDFFactTemplates mergedRDFTemplates) {
        Substitution<ImmutableTerm> normalizedSubstitution = ((ConstructionNode) normalizedTree.getRootNode()).getSubstitution();
        RDFFactTemplates compressedTemplates = mergedRDFTemplates.compress(normalizedSubstitution.inverseMap().values().stream()
                .filter(vs -> vs.size() > 1)
                .map(ImmutableList::copyOf)
                .collect(ImmutableCollectors.toSet()));

        ImmutableSet<Variable> compressedVariables = compressedTemplates.getVariables();
        Substitution<ImmutableTerm> compressedSubstitution = normalizedSubstitution.restrictDomainTo(compressedVariables);

        IQTree compressedTree = iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(compressedVariables, compressedSubstitution),
                normalizedTree.getChildren().get(0));

        return buildCluster(compressedTree, compressedTemplates);
    }

    protected abstract MappingEntryCluster buildCluster(IQTree compressedTree, RDFFactTemplates compressedTemplates);
}
