package it.unibz.inf.ontop.materialization;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import org.eclipse.rdf4j.model.IRI;

import java.util.Optional;

public interface MappingAssertionInformation {

    Optional<MappingAssertionInformation> merge(MappingAssertionInformation other);
    IQTree getIQTree();
    RDFFactTemplates getRDFFactTemplates();
    /**
     * Returns a new RDFFactTemplates with only the triples/quads that have a predicate in the given set
     */
    RDFFactTemplates restrict(ImmutableSet<IRI> predicates);


}
