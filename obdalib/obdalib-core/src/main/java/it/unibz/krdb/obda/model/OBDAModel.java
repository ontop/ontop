package it.unibz.krdb.obda.model;

import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.queryanswering.QueryController;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

public interface OBDAModel {

	public abstract QueryController getQueryController();

//	public abstract OBDAModelImpl getDatasourcesController();

//	public abstract OBDAModelImpl getMappingController();

	public abstract String getVersion();

	public abstract String getBuiltDate();

	public abstract String getBuiltBy();

	public abstract void setPrefixManager(PrefixManager prefman);

	public abstract PrefixManager getPrefixManager();
	
	/***
	 * DATASOURCES CONTROLLER
	 */

	
	public void addSourcesListener(OBDAModelListener listener);
	
	public void removeSourcesListener(OBDAModelListener listener);

//	public void fireAllDatasourcesDeleted();

	public void fireSourceAdded(DataSource source);

	public void fireSourceRemoved(DataSource source);

	public void fireSourceParametersUpdated();

	//TODO remove
	public void fireSourceNameUpdated(URI old, DataSource neu);

	public List<DataSource> getSources();

	public DataSource getSource(URI name);

	public void addSource(DataSource source);

	public void removeSource(URI id);
	
	public void updateSource(URI id, DataSource dsd);
	
	public boolean containsSource(URI name);



	/***
	 * 
	 * MAPPINGS
	 * 
	 */
	

	public void addMappingsListener(MappingControllerListener listener);
	
	public void removeMappingsListener(MappingControllerListener listener);

	

	/***************************************************************************
	 * Deletes the mapping with ID id
	 * 
	 * @param datasource_uri
	 * @param mapping_id
	 */
	public void removeMapping(URI datasource_uri, String mapping_id);

	/***************************************************************************
	 * Deletes all the mappings for a given datasource
	 * 
	 * @param datasource_uri
	 */
	public void removeMappings(URI datasource_uri);

	public void duplicateMapping(URI srcuri, String id, String new_id) throws DuplicateMappingException;

	/***************************************************************************
	 * Returns the object of the mapping with id ID for the datasource
	 * source_uri
	 * 
	 * @param source_uri
	 * @param mapping_id
	 * @return
	 */
	public OBDAMappingAxiom getMapping(URI source_uri, String mapping_id);

	/***************************************************************************
	 * Returns all the mappings hold by the controller. Warning. do not modify
	 * this mappings manually, use the controllers methods.
	 * 
	 * @return
	 */
	public Hashtable<URI, ArrayList<OBDAMappingAxiom>> getMappings();

	/***************************************************************************
	 * Returns all the mappings for a given datasource identified by its uri.
	 * 
	 * @param datasource_uri
	 * @return
	 */
	public ArrayList<OBDAMappingAxiom> getMappings(URI datasource_uri);

	//TODO remove
	public String getNextAvailableDuplicateIDforMapping(URI source_uri, String originalid);

	
	//TODO revemo
	public String getNextAvailableMappingID(URI datasource_uri);

	/***************************************************************************
	 * Retrives the position of the mapping identified by mapping_id in the
	 * array of mappings for the given datasource.
	 * 
	 * @param datasource_uri
	 *            The datasource to whom the mapping belongs.
	 * @param mapping_id
	 *            The id of the mapping that is being searched.
	 * @return The position of the mapping in the array OR -1 if the mapping is
	 *         not found.
	 */
	public int indexOfMapping(URI datasource_uri, String mapping_id);

	/***************************************************************************
	 * Inserts a mappings into the mappings for a datasource. If the ID of the
	 * mapping already exits it throws an exception.
	 * 
	 * @param datasource_uri
	 * @param mapping
	 * @throws DuplicateMappingException
	 */
	public void addMapping(URI datasource_uri, OBDAMappingAxiom mapping) throws DuplicateMappingException;
	
	public void addMappings(URI datasource_uri, Collection<OBDAMappingAxiom> mappings) throws DuplicateMappingException;

	
	/***
	 * Removes all mappings
	 */
	public void clearMappings();

	

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
	public void updateTargetQueryMapping(URI datasource_uri, String mapping_id, Query targetQuery);
	
	/***************************************************************************
	 * Updates the indicated mapping and fires the appropiate event.
	 * 
	 * @param datasource_uri
	 * @param mapping_id
	 * @param body
	 */
	public void updateMappingsSourceQuery(URI datasource_uri, String mapping_id, Query sourceQuery);


	public boolean containsMapping(URI datasourceUri, String mappingId);
	
}