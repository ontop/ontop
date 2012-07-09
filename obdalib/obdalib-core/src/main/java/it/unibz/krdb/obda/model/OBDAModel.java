package it.unibz.krdb.obda.model;

import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.querymanager.QueryController;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public interface OBDAModel extends Cloneable, Serializable {

	public QueryController getQueryController();

	public String getVersion();

	public String getBuiltDate();

	public String getBuiltBy();

	public void setPrefixManager(PrefixManager prefman);

	public PrefixManager getPrefixManager();

	public OBDADataFactory getDataFactory();

	/*
	 * Methods related to data sources
	 */

	public void addSourcesListener(OBDAModelListener listener);

	public void removeSourcesListener(OBDAModelListener listener);

	public void fireSourceAdded(OBDADataSource source);

	public void fireSourceRemoved(OBDADataSource source);

	public void fireSourceParametersUpdated();

	public void fireSourceNameUpdated(URI old, OBDADataSource neu);	// TODO remove

	/**
	 * Returns the list of all sources defined in this OBDA model. This list is
	 * a non-modifiable copy of the internal list.
	 */
	public List<OBDADataSource> getSources();

	public OBDADataSource getSource(URI name);

	public void addSource(OBDADataSource source);

	public void removeSource(URI id);

	public void updateSource(URI id, OBDADataSource dsd);

	public boolean containsSource(URI name);

	/*
	 * Methods related to mappings
	 */

	public void addMappingsListener(OBDAMappingListener listener);

	public void removeMappingsListener(OBDAMappingListener listener);

	/**
	 * Deletes the mapping given its id and data source.
	 */
	public void removeMapping(URI sourceuri, String mappingid);

	/**
	 * Deletes all the mappings given the data source id
	 */
	public void removeAllMappings(URI sourceuri);

	/**
	 * Retrieves the mapping axiom given its id and data source.
	 */
	public OBDAMappingAxiom getMapping(URI sourceuri, String mappinid);

	/**
	 * Returns all the mappings the given data source id.
	 */
	public ArrayList<OBDAMappingAxiom> getMappings(URI sourceuri);

	/**
	 * Returns all the mappings in this model.
	 */
	public Hashtable<URI, ArrayList<OBDAMappingAxiom>> getMappings();

	/**
	 * Retrieves the position of the mapping given its id and data source.
	 */
	@Deprecated
	public int indexOf(URI sourceuri, String mappingid);

	/**
	 * Inserts a mappings into this model. If the mapping id already exits it
	 * throws an exception.
	 */
	public void addMapping(URI sourceuri, OBDAMappingAxiom mapping) throws DuplicateMappingException;

	/**
	 * Inserts a collection of mappings into this model. Any duplicates will be
	 * failed and the system will report such duplication failures.
	 */
	public void addMappings(URI sourceuri, Collection<OBDAMappingAxiom> mappings) throws DuplicateMappingException;

	/***
	 * Removes all mappings in the model.
	 */
	public void removeAllMappings();

	/**
	 * Updates the mapping id.
	 */
	public int updateMapping(URI datasource_uri, String mapping_id, String new_mappingid);

	/**
	 * Replaces the old target query with the new one given its id.
	 */
	public void updateTargetQueryMapping(URI datasource_uri, String mapping_id, OBDAQuery targetQuery);

	/**
	 * Replaces the old source query with the new one given its id.
	 */
	public void updateMappingsSourceQuery(URI datasource_uri, String mapping_id, OBDAQuery sourceQuery);

	/**
	 * Refactors every mapping in this OBDA model by modifying each mapping of
	 * the model by replacing each atom that use the predicate old name, with a
	 * new atom that uses newName and has the same terms as the old atom.
	 */
	public int renamePredicate(Predicate oldname, Predicate newName);

	/**
	 * Removes all atoms that contain the given predicate in all mappings.
	 */
	public int deletePredicate(Predicate predicate);

	public boolean containsMapping(URI datasourceUri, String mappingId);

	public Object clone();

	public void reset();

	public Set<Predicate> getDeclaredPredicates();

	public Set<Predicate> getDeclaredClasses();

	public Set<Predicate> getDeclaredObjectProperties();

	public Set<Predicate> getDeclaredDataProperties();

	public boolean declarePredicate(Predicate predicate);

	public boolean declareClass(Predicate classname);

	public boolean declareObjectProperty(Predicate property);

	public boolean declareDataProperty(Predicate property);
	
	public boolean unDeclarePredicate(Predicate predicate);

	public boolean unDeclareClass(Predicate classname);

	public boolean unDeclareObjectProperty(Predicate property);

	public boolean unDeclareDataProperty(Predicate property);

	public boolean isDeclaredClass(Predicate classname);

	public boolean isDeclaredObjectProperty(Predicate property);

	public boolean isDeclaredDataProperty(Predicate property);

	public boolean isDeclared(Predicate predicate);

}