package org.semanticweb.ontop.model;


import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * An OBDA model contains mapping information.
 *
 * This interface is generic regarding the targeted native query language.
 *
 * See SQLOBDAModel for an SQL-specific interface.
 *
 */
public interface OBDAModel {

    /**
     * Retrieves the mapping axiom given its id and data source.
     */
    public OBDAMappingAxiom getMapping(URI sourceUri, String mappingId);

    /**
     * Returns the mappings of a given data source.
     */
    public List<OBDAMappingAxiom> getMappings(URI sourceUri);

    /**
     * Returns all the mappings in this model.
     */
    public Map<URI, List<OBDAMappingAxiom>> getMappings();

    /**
     * Constructs a new OBDA model with new mappings.
     *
     * Note that some ODBA models are immutable so you
     * should use this method to "update" them.
     */
    public OBDAModel newModel(Map<URI, List<OBDAMappingAxiom>> newMappings);

    /**
     * Returns the list of all sources defined in this OBDA model. This list is
     * a non-modifiable copy of the internal list.
     */
    public List<OBDADataSource> getSources();


}
