package org.semanticweb.ontop.model;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.io.PrefixManager;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An OBDA model contains mapping information.
 *
 * This interface is generic regarding the targeted native query language.
 *
 *
 * An OBDA model is a container for the database and mapping declarations needed to define a
 * Virtual ABox or Virtual RDF graph. That is, this is a manager for a
 * collection of JDBC databases (when SQL is the native query language) and their corresponding mappings.
 * It is used as input to any Quest instance (either OWLAPI or Sesame).
 *
 * <p>
 * SQLOBDAModels are also used internally by the Protege plugin and many other
 * utilities including the mapping materializer (e.g. to generate ABox assertions or
 * RDF triples from a .obda file and a database).
 *
 * <p>
 * SQLOBAModels can be serialized and read to/from mapping files using
 * {@link org.semanticweb.ontop.mapping.MappingParser}.
 *
 *
 * @see org.semanticweb.ontop.mapping.MappingParser
 *
 * Initial author (before refactoring):
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 *
 */
public interface OBDAModel {

    /**
     * TODO: describe
     */
    public PrefixManager getPrefixManager();

    /**
     * Retrieves the mapping axiom given its id.
     */
    public OBDAMappingAxiom getMapping(String mappingId);

    /**
     * Returns the mappings of a given data source.
     * This set is immutable.
     */
    public ImmutableList<OBDAMappingAxiom> getMappings(URI sourceUri);

    /**
     * Returns all the mappings in this model indexed by
     * their datasource.
     */
    public ImmutableMap<URI, ImmutableList<OBDAMappingAxiom>> getMappings();

    /**
     * Constructs a new OBDA model with new mappings.
     *
     * Note that some ODBA models are immutable so you
     * should use this method to "update" them.
     * 
     */
    public OBDAModel newModel(Set<OBDADataSource> dataSources,
                              Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings)
        throws DuplicateMappingException;
    /**
     * Constructs a new OBDA model with new mappings and a prefix manager.
     *
     * Note that some ODBA models are immutable so you
     * should use this method to "update" them.
     *
     */
    public OBDAModel newModel(Set<OBDADataSource> dataSources,
                              Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings,
                              PrefixManager prefixManager) throws DuplicateMappingException;

    /**
     * Returns the set of all sources defined in this OBDA model. This set
     * is immutable.
     */
    public Set<OBDADataSource> getSources();

    public OBDADataSource getSource(URI sourceURI);

    public boolean containsSource(URI sourceURI);

    //----------------------------------------
    // Inherited from the former SQLOBDAModel
    // TODO: remove them
    //----------------------------------------

    @Deprecated
    public String getVersion();
    @Deprecated
    public String getBuiltDate();
    @Deprecated
    public String getBuiltBy();

    /**
     * Retrieves the mapping axiom given its id and data source.
     * Please note that the source URI is irrelevant here.
     * This method is thus deprecated.
     *
     * TODO: remove it.
     */
    @Deprecated
    public OBDAMappingAxiom getMapping(URI sourceUri, String mappingId);

	/*
	 * Methods related to mappings
	 */

    @Deprecated
    public Set<Predicate> getDeclaredPredicates();

    @Deprecated
    public Set<Predicate> getDeclaredClasses();

    @Deprecated
    public Set<Predicate> getDeclaredObjectProperties();

    @Deprecated
    public Set<Predicate> getDeclaredDataProperties();

    @Deprecated
    public boolean declarePredicate(Predicate predicate);

    @Deprecated
    public boolean declareClass(Predicate classname);

    @Deprecated
    public boolean declareObjectProperty(Predicate property);

    @Deprecated
    public boolean declareDataProperty(Predicate property);

    @Deprecated
    public boolean unDeclarePredicate(Predicate predicate);

    @Deprecated
    public boolean unDeclareClass(Predicate classname);

    @Deprecated
    public boolean unDeclareObjectProperty(Predicate property);

    @Deprecated
    public boolean unDeclareDataProperty(Predicate property);

    @Deprecated
    public boolean isDeclaredClass(Predicate classname);

    @Deprecated
    public boolean isDeclaredObjectProperty(Predicate property);

    @Deprecated
    public boolean isDeclaredDataProperty(Predicate property);

    @Deprecated
    public boolean isDeclared(Predicate predicate);


}
