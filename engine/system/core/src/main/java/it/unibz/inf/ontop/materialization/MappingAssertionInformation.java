package it.unibz.inf.ontop.materialization;

import it.unibz.inf.ontop.iq.IQTree;

import java.util.Optional;

public interface MappingAssertionInformation {

    Optional<MappingAssertionInformation> merge(MappingAssertionInformation other);
    IQTree getIQTree();
    RDFFactTemplates getRDFFactTemplates();

}
