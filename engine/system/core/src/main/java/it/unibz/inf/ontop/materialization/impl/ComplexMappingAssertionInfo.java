package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.materialization.MappingAssertionInformation;
import org.eclipse.rdf4j.model.IRI;

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

    @Override
    public RDFFactTemplates restrict(ImmutableSet<IRI> predicates) {
        return getRDFFactTemplates();
    }


}
