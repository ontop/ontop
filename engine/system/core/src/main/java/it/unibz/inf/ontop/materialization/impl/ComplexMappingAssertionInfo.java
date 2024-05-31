package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.materialization.MappingAssertionInformation;

import java.util.Optional;

public class ComplexMappingAssertionInfo implements MappingAssertionInformation {
    private final IQ originalTree;

    public ComplexMappingAssertionInfo(IQ originalTree) {
        this.originalTree = originalTree;
    }


    @Override
    public Optional<MappingAssertionInformation> merge(MappingAssertionInformation other) {
        return Optional.empty();
    }

    @Override
    public IQTree getIQTree() {
        return originalTree.getTree();
    }

    @Override
    public RDFFactTemplates getRDFFactTemplates() {
        return new RDFFactTemplatesImpl(ImmutableList.of(originalTree.getProjectionAtom().getArguments()));
    }


}
