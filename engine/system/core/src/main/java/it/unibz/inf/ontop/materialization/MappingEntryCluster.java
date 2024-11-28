package it.unibz.inf.ontop.materialization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.eclipse.rdf4j.model.IRI;

import java.util.Optional;

/**
 * A MappingEntryCluster is the result of merging together different mapping entries sharing the same "source".
 */
public interface MappingEntryCluster {

    IQTree getIQTree();

    RDFFactTemplates getRDFFactTemplates();

    Optional<MappingEntryCluster> merge(MappingEntryCluster other);

    /**
     * Returns a new RDFFactTemplates with only the triples/quads that have a predicate in the given set
     */
    RDFFactTemplates restrict(ImmutableSet<IRI> predicates);

    ImmutableList<RelationDefinition> getRelationsDefinitions();

    MappingEntryCluster renameConflictingVariables(VariableGenerator conflictingVariableGenerator);


}
