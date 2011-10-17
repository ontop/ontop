package it.unibz.krdb.obda.model;

import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.querymanager.QueryController;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

public interface OBDAModel extends Cloneable {

	public abstract QueryController getQueryController();

	// public abstract OBDAModelImpl getDatasourcesController();

	// public abstract OBDAModelImpl getMappingController();

	public abstract String getVersion();

	public abstract String getBuiltDate();

	public abstract String getBuiltBy();

	public abstract void setPrefixManager(PrefixManager prefman);

	public abstract PrefixManager getPrefixManager();

	/***
	 * DATASOURCES
	 */

	public void addSourcesListener(OBDAModelListener listener);

	public void removeSourcesListener(OBDAModelListener listener);

	public void fireSourceAdded(OBDADataSource source);

	public void fireSourceRemoved(OBDADataSource source);

	public void fireSourceParametersUpdated();

	// TODO remove
	public void fireSourceNameUpdated(URI old, OBDADataSource neu);

	/***
	 * Returns the list of all sources defined in this OBDA model. This list is
	 * a non-modifiable copy of the internal list.
	 * 
	 * @return
	 */
	public List<OBDADataSource> getSources();

	public OBDADataSource getSource(URI name);

	public void addSource(OBDADataSource source);

	public void removeSource(URI id);

	public void updateSource(URI id, OBDADataSource dsd);

	public boolean containsSource(URI name);

	/***
	 * 
	 * MAPPINGS
	 * 
	 */

	public void addMappingsListener(OBDAMappingListener listener);

	public void removeMappingsListener(OBDAMappingListener listener);

	/***************************************************************************
	 * Deletes the mapping with ID id
	 * 
	 * @param sourceuri
	 * @param mappingid
	 */
	public void removeMapping(URI sourceuri, String mappingid);

	/***************************************************************************
	 * Deletes all the mappings for a given datasource
	 * 
	 * @param sourceuri
	 */
	public void removeAllMappings(URI sourceuri);

	/***************************************************************************
	 * Returns the object of the mapping with id ID for the datasource
	 * source_uri
	 * 
	 * @param sourceuri
	 * @param mappinid
	 * @return
	 */
	public OBDAMappingAxiom getMapping(URI sourceuri, String mappinid);

	/***************************************************************************
	 * Returns all the mappings hold by the controller. Warning. do not modify
	 * this mappings manually, use the controllers methods.
	 * 
	 * @return
	 */
	@Deprecated
	public Hashtable<URI, ArrayList<OBDAMappingAxiom>> getMappings();

	/***************************************************************************
	 * Returns all the mappings for a given datasource identified by its uri.
	 * 
	 * @param sourceuri
	 * @return
	 */
	public ArrayList<OBDAMappingAxiom> getMappings(URI sourceuri);

	/***************************************************************************
	 * Retrieves the position of the mapping identified by mapping_id in the
	 * array of mappings for the given data source.
	 * 
	 * @param sourceuri
	 *            The source to whom the mapping belongs.
	 * @param mappingid
	 *            The id of the mapping that is being searched.
	 * @return The position of the mapping in the array OR -1 if the mapping is
	 *         not found.
	 */
	@Deprecated
	public int indexOf(URI sourceuri, String mappingid);

	/***************************************************************************
	 * Inserts a mappings into the mappings for a datasource. If the ID of the
	 * mapping already exits it throws an exception.
	 * 
	 * @param sourceuri
	 * @param mapping
	 * @throws DuplicateMappingException
	 */
	public void addMapping(URI sourceuri, OBDAMappingAxiom mapping) throws DuplicateMappingException;

	/***
	 * @param sourceuri
	 * @param mappings
	 * @throws DuplicateMappingException
	 */
	public void addMappings(URI sourceuri, Collection<OBDAMappingAxiom> mappings) throws DuplicateMappingException;

	/***
	 * Removes all mappings
	 */
	public void removeAllMappings();

	/***************************************************************************
	 * Updates the indicated mapping and fires the appropiate event.
	 * 
	 * @param datasource_uri
	 *            The datasource URI.
	 * @param mapping_id
	 *            The old mapping id.
	 * @param new_mappingid
	 *            The new mapping id to replace the old id.
	 * @return Returns -1 if the update fails.
	 */
	public int updateMapping(URI datasource_uri, String mapping_id, String new_mappingid);

	/***************************************************************************
	 * Updates the indicated mapping and fires the appropiate event.
	 * 
	 * @param datasource_uri
	 * @param mapping_id
	 * @param head
	 */
	public void updateTargetQueryMapping(URI datasource_uri, String mapping_id, OBDAQuery targetQuery);

	/***************************************************************************
	 * Updates the indicated mapping and fires the appropiate event.
	 * 
	 * @param datasource_uri
	 * @param mapping_id
	 * @param body
	 */
	public void updateMappingsSourceQuery(URI datasource_uri, String mapping_id, OBDAQuery sourceQuery);

	public boolean containsMapping(URI datasourceUri, String mappingId);

	public Object clone();
}