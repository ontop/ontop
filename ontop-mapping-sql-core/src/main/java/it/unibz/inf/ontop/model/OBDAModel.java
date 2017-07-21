package it.unibz.inf.ontop.model;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;

/**
 * An OBDA model contains a mapping.
 * This interface is generic regarding the targeted native query language.
 *
 * An OBDA model is a container for  mapping declarations needed to define a
 * Virtual ABox or Virtual RDF graph.
 *
 * <p>
 * OBDAModels are also used indirectly by the Protege plugin and many other
 * utilities including the mapping materializer (e.g. to generate ABox assertions or
 * RDF triples from a .obda file and a database).
 *
 * <p>
 * OBDAModels can be serialized and parsed to/from mapping files using
 * a serializer and a MappingParser.
 *
 *
 * Initial author (before refactoring):
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 *
 *
 */
public interface OBDAModel extends PreProcessedMapping {

    /**
     * Retrieves the mapping axiom given its id.
     */
    OBDAMappingAxiom getMappingAssertion(String mappingAssertionId);

    /**
     * Returns all the mapping assertions in this model indexed by
     * their datasource.
     */
    ImmutableList<OBDAMappingAxiom> getMappingAssertions();

    /**
     * Constructs a new OBDA model with new mapping assertions.
     *
     * Note that normal ODBA models are immutable so you
     * should use this method to "update" them.
     * 
     */
    OBDAModel newModel(ImmutableList<OBDAMappingAxiom> newMappingAssertions)
        throws DuplicateMappingException;
    /**
     * Constructs a new OBDA model with new mapping assertions and a prefix manager.
     *
     * Note that normal ODBA models are immutable so you
     * should use this method to "update" them.
     *
     */
    OBDAModel newModel(ImmutableList<OBDAMappingAxiom> newMappingAssertions,
                              MappingMetadata metadata) throws DuplicateMappingException;

}
