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
 * a serializer and a {@link org.semanticweb.ontop.mapping.MappingParser}.
 *
 *
 * @see org.semanticweb.ontop.mapping.MappingParser
 *
 * Initial author (before refactoring):
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 *
 * TODO: remove the side-effect methods so that OBDA models can be truly
 * immutable.
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
     * Note that normal ODBA models are immutable so you
     * should use this method to "update" them.
     * 
     */
    public OBDAModel newModel(Set<OBDADataSource> dataSources,
                              Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings)
        throws DuplicateMappingException;
    /**
     * Constructs a new OBDA model with new mappings and a prefix manager.
     *
     * Note that normal ODBA models are immutable so you
     * should use this method to "update" them.
     *
     */
    public OBDAModel newModel(Set<OBDADataSource> dataSources,
                              Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings,
                              PrefixManager prefixManager) throws DuplicateMappingException;

    /**
     * Constructs a new OBDA model with new mappings, a prefix manager, class and properties
     * declarations.
     *
     * Note that normal ODBA models are immutable so you
     * should use this method to "update" them.
     *
     */
    public OBDAModel newModel(Set<OBDADataSource> dataSources,
                              Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings,
                              PrefixManager prefixManager, Set<Predicate> declaredClasses,
                              Set<Predicate> declaredObjectProperties,
                              Set<Predicate> declaredDataProperties,
                              Set<Predicate> otherDeclaredPredicates) throws DuplicateMappingException;

    /**
     * Returns the set of all sources defined in this OBDA model. This set
     * is immutable.
     */
    public Set<OBDADataSource> getSources();

    public OBDADataSource getSource(URI sourceURI);

    public boolean containsSource(URI sourceURI);

    public Set<Predicate> getDeclaredPredicates();

    public Set<Predicate> getDeclaredClasses();

    public Set<Predicate> getDeclaredObjectProperties();

    public Set<Predicate> getDeclaredDataProperties();

    public boolean isDeclaredClass(Predicate classname);

    public boolean isDeclaredObjectProperty(Predicate property);

    public boolean isDeclaredDataProperty(Predicate property);

    public boolean isDeclared(Predicate predicate);


    //--------------------------------
    // Side-effect methods (mutable)
    // TODO: remove them
    //--------------------------------

    @Deprecated
    public boolean declarePredicate(Predicate predicate);

    @Deprecated
    public boolean declareClass(Predicate className);

    @Deprecated
    public boolean declareObjectProperty(Predicate property);

    @Deprecated
    public boolean declareDataProperty(Predicate property);

    @Deprecated
    public boolean unDeclarePredicate(Predicate predicate);

    @Deprecated
    public boolean unDeclareClass(Predicate className);

    @Deprecated
    public boolean unDeclareObjectProperty(Predicate property);

    @Deprecated
    public boolean unDeclareDataProperty(Predicate property);
}
