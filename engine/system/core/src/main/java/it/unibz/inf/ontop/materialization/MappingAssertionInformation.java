package it.unibz.inf.ontop.materialization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.eclipse.rdf4j.model.IRI;

import java.util.Optional;

public interface MappingAssertionInformation {

    IQTree getIQTree();
    RDFFactTemplates getRDFFactTemplates();

    Optional<MappingAssertionInformation> merge(MappingAssertionInformation other);
    /**
     * Returns a new RDFFactTemplates with only the triples/quads that have a predicate in the given set
     */
    RDFFactTemplates restrict(ImmutableSet<IRI> predicates);

    ImmutableList<RelationDefinition> getRelationsDefinitions();

    MappingAssertionInformation renameConflictingVariables(VariableGenerator conflictingVariableGenerator);


}
