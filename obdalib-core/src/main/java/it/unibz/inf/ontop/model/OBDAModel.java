package it.unibz.inf.ontop.model;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.mapping.MappingParser;
import it.unibz.inf.ontop.ontology.*;

/**
 * An OBDA model contains mapping information.
 * This interface is generic regarding the targeted native query language.
 *
 * An OBDA model is a container for the database and mapping declarations needed to define a
 * Virtual ABox or Virtual RDF graph. That is, this is a manager for a
 * collection of JDBC databases (when SQL is the native query language) and their corresponding mappings.
 * It is used as input to any Quest instance (either OWLAPI or Sesame).
 *
 * An OBDA model also contains lists of the declared properties and classes.
 * TODO: move this concern into a separated class.
 *
 * <p>
 * OBDAModels are also used indirectly by the Protege plugin and many other
 * utilities including the mapping materializer (e.g. to generate ABox assertions or
 * RDF triples from a .obda file and a database).
 *
 * <p>
 * OBDAModels can be serialized and parsed to/from mapping files using
 * a serializer and a {@link MappingParser}.
 *
 *
 * @see MappingParser
 *
 * Initial author (before refactoring):
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 *
 * TODO: make the ontology vocabulary immutable
 *
 */
public interface OBDAModel {

    /**
     * Prefix manager. Normally immutable.
     */
    public PrefixManager getPrefixManager();

    /**
     * Retrieves the mapping axiom given its id.
     */
    public OBDAMappingAxiom getMapping(String mappingId);

    /**
     * Returns all the mappings in this model indexed by
     * their datasource.
     */
    public ImmutableList<OBDAMappingAxiom> getMappings();

    /**
     * Constructs a new OBDA model with new mappings.
     *
     * Note that normal ODBA models are immutable so you
     * should use this method to "update" them.
     * 
     */
    public OBDAModel newModel(ImmutableList<OBDAMappingAxiom> newMappings)
        throws DuplicateMappingException;
    /**
     * Constructs a new OBDA model with new mappings and a prefix manager.
     *
     * Note that normal ODBA models are immutable so you
     * should use this method to "update" them.
     *
     */
    public OBDAModel newModel(ImmutableList<OBDAMappingAxiom> newMappings,
                              PrefixManager prefixManager) throws DuplicateMappingException;

    /**
     * Constructs a new OBDA model with new mappings, a prefix manager, class and properties
     * declarations.
     *
     * Note that normal ODBA models are immutable so you
     * should use this method to "update" them.
     *
     */
    public OBDAModel newModel(ImmutableList<OBDAMappingAxiom> newMappings,
                              PrefixManager prefixManager, OntologyVocabulary ontologyVocabulary)
            throws DuplicateMappingException;

    /**
     * TODO: remove it when OBDA models will be FULLY immutable.
     */
    public OBDAModel clone();

	public OntologyVocabulary getOntologyVocabulary();

}
