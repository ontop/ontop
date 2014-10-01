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
     * Returns all the mappings the given data source id.
     */
    public List<OBDAMappingAxiom> getMappings(URI sourceUri);

    /**
     * Returns all the mappings in this model.
     */
    public Map<URI, List<OBDAMappingAxiom>> getMappings();
}
