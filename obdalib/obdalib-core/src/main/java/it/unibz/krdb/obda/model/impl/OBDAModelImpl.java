/*******************************************************************************
 * Copyright (c) 2008, Mariano Rodriguez-Muro. All rights reserved.
 * 
 * The OBDA-API is licensed under the terms of the Lesser General Public License
 * v.3 (see OBDAAPI_LICENSE.txt for details). The components of this work
 * include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, b)
 * third-party components licensed under terms that may be different from those
 * of the LGPL. Information about such licenses can be found in the file named
 * OBDAAPI_3DPARTY-LICENSES.txt.
 */
package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.codec.DatasourceXMLCodec;
import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.io.SimplePrefixManager;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingListener;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAModelListener;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.queryanswering.QueryController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OBDAModelImpl implements OBDAModel {

	protected URI					currentOntologyURI	= null;

//	protected OBDAModelImpl	dscontroller		= null;

//	private MappingControllerInterface		mapcontroller		= null;

	protected QueryController		queryController		= null;

	private PrefixManager			prefman				= null;

	protected final Logger			log					= LoggerFactory.getLogger(this.getClass());
	
	
	/***
	 * Datasources
	 */
	
	private final DatasourceXMLCodec							codec				= new DatasourceXMLCodec();

	private HashMap<URI, OBDADataSource>					datasources			= null;

	private ArrayList<OBDAModelListener>	sourceslisteners			= null;
	
	OBDADataFactory fac = null;

	
	/***
	 * Mappings
	 */
	
	private ArrayList<OBDAMappingListener>		mappinglisteners	= null;

	private Hashtable<URI, ArrayList<OBDAMappingAxiom>>	mappings	= null;


	protected OBDAModelImpl() {

//		dscontroller = new OBDAModelImpl();
//		mapcontroller = new OBDAModelImpl();
		queryController = new QueryController();

		setPrefixManager(new SimplePrefixManager());
		log.debug("OBDA Lib initialized");
		
		fac = OBDADataFactoryImpl.getInstance();
		datasources = new HashMap<URI, OBDADataSource>();
		sourceslisteners = new ArrayList<OBDAModelListener>();
		
		mappings = new Hashtable<URI, ArrayList<OBDAMappingAxiom>>();
		mappinglisteners = new ArrayList<OBDAMappingListener>();



	}

	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.model.OBDAModel#getQueryController()
	 */
	@Override
	public QueryController getQueryController() {
		return queryController;
	}

//	/* (non-Javadoc)
//	 * @see inf.unibz.it.obda.model.OBDAModel#getDatasourcesController()
//	 */
//	@Override
//	public OBDAModelImpl getDatasourcesController() {
//		return this;
//	}

//	/* (non-Javadoc)
//	 * @see inf.unibz.it.obda.model.OBDAModel#getMappingController()
//	 */
//	/* (non-Javadoc)
//	 * @see it.unibz.krdb.obda.model.impl.MappingControllerInterface#getMappingController()
//	 */
//	@Override
//	public OBDAModelImpl getMappingController() {
//		return this;
//	}

	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.model.OBDAModel#getVersion()
	 */
	@Override
	public String getVersion() {
		try {
			InputStream stream = getClass().getResourceAsStream("/META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(stream);
			Attributes attributes = manifest.getMainAttributes();
			String implementationVersion = attributes.getValue("Implementation-Version");
			return implementationVersion;
		} catch (IOException e) {
			return "";
		}
	}

	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.model.OBDAModel#getBuiltDate()
	 */
	@Override
	public String getBuiltDate() {
		try {
			InputStream stream = getClass().getResourceAsStream("/META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(stream);
			Attributes attributes = manifest.getMainAttributes();
			String builtDate = attributes.getValue("Built-Date");
			return builtDate;
		} catch (IOException e) {
			return "";
		}
	}

	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.model.OBDAModel#getBuiltBy()
	 */
	@Override
	public String getBuiltBy() {
		try {
			InputStream stream = getClass().getResourceAsStream("/META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(stream);
			Attributes attributes = manifest.getMainAttributes();
			String builtBy = attributes.getValue("Built-By");
			return builtBy;
		} catch (IOException e) {
			return "";
		}
	}

	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.model.OBDAModel#setPrefixManager(inf.unibz.it.obda.io.PrefixManager)
	 */
	@Override
	public void setPrefixManager(PrefixManager prefman) {
		this.prefman = prefman;
	}

	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.model.OBDAModel#getPrefixManager()
	 */
	@Override
	public PrefixManager getPrefixManager() {
		return prefman;
	}
	
	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#addDataSource(it.unibz.krdb.obda.model.DataSource)
	 */
	
	public void addSource(OBDADataSource source) {
		datasources.put(source.getSourceID(), source);
		fireSourceAdded(source);
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#addDatasourceControllerListener(it.unibz.krdb.obda.model.DatasourcesControllerListener)
	 */
	public void addSourcesListener(OBDAModelListener listener) {
		if (sourceslisteners.contains(listener))
			return;
		sourceslisteners.add(listener);
	}

//	/* (non-Javadoc)
//	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#fireAllDatasourcesDeleted()
//	 */
//	public void fireAllDatasourcesDeleted() {
//		for (OBDAModelListener listener : sourceslisteners) {
//			listener.alldatasourcesDeleted();
//		}
//	}


	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#fireDatasourceAdded(it.unibz.krdb.obda.model.DataSource)
	 */
	public void fireSourceAdded(OBDADataSource source) {
		for (OBDAModelListener listener : sourceslisteners) {
			listener.datasourceAdded(source);
		}
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#fireDatasourceDeleted(it.unibz.krdb.obda.model.DataSource)
	 */
	public void fireSourceRemoved(OBDADataSource source) {
		for (OBDAModelListener listener : sourceslisteners) {
			listener.datasourceDeleted(source);
		}
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#fireParametersUpdated()
	 */
	public void fireSourceParametersUpdated(){
		for (OBDAModelListener listener : sourceslisteners) {
			listener.datasourcParametersUpdated();
		}
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#fireDataSourceNameUpdated(java.net.URI, it.unibz.krdb.obda.model.DataSource)
	 */
	public void fireSourceNameUpdated(URI old, OBDADataSource neu){
		for (OBDAModelListener listener : sourceslisteners) {
			listener.datasourceUpdated(old.toString(), neu);
		}
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#getAllSources()
	 */
	public List<OBDADataSource> getSources() {
		List<OBDADataSource> sources = new LinkedList<OBDADataSource>(datasources.values());
		return Collections.unmodifiableList(sources);
	}


	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#getDataSource(java.net.URI)
	 */
	public OBDADataSource getSource(URI name) {
		return datasources.get(name);
	}

	public boolean containsSource(URI name) {
	  if (getSource(name) != null) {
	    return true;
	  }
	  return false;
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#removeDataSource(java.net.URI)
	 */
	public void removeSource(URI id) {

		OBDADataSource source = getSource(id);
		datasources.remove(id);
		fireSourceRemoved(source);
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#removeDatasourceControllerListener(it.unibz.krdb.obda.model.DatasourcesControllerListener)
	 */
	public void removeSourcesListener(OBDAModelListener listener) {
		sourceslisteners.remove(listener);
	}



	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#updateDataSource(java.net.URI, it.unibz.krdb.obda.model.DataSource)
	 */
	public void updateSource(URI id, OBDADataSource dsd) {
	  datasources.remove(id);
		datasources.put(dsd.getSourceID(), dsd);
		fireSourceNameUpdated(id, dsd);
	}
	
	
	
	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.MappingControllerInterface#addMappingControllerListener(it.unibz.krdb.obda.model.MappingControllerListener)
	 */
	
	public void addMappingsListener(OBDAMappingListener listener) {
		mappinglisteners.add(listener);
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.MappingControllerInterface#deleteMapping(java.net.URI, java.lang.String)
	 */
	public void removeMapping(URI datasource_uri, String mapping_id) {
		int index = indexOfMapping(datasource_uri, mapping_id);
		if (index == -1) {
			return;
		} else {
			ArrayList<OBDAMappingAxiom> current_mappings = mappings.get(datasource_uri);
			current_mappings.remove(index);
		}
		fireMappingDeleted(datasource_uri, mapping_id);
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.MappingControllerInterface#deleteMappings(java.net.URI)
	 */
	public void removeMappings(URI datasource_uri) {
		ArrayList<OBDAMappingAxiom> mappings = getMappings(datasource_uri);
		while (!mappings.isEmpty()) {
			mappings.remove(0);
		}
		fireAllMappingsRemoved();
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.MappingControllerInterface#duplicateMapping(java.net.URI, java.lang.String, java.lang.String)
	 */
	public void duplicateMapping(URI srcuri, String id, String new_id) throws DuplicateMappingException {
		OBDAMappingAxiom oldmapping = getMapping(srcuri, id);
		OBDAMappingAxiom newmapping = null;
		try {
			newmapping = (OBDAMappingAxiom) oldmapping.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		newmapping.setId(new_id);
		addMapping(srcuri, newmapping);
	}

	private void fireAllMappingsRemoved() {
		for (OBDAMappingListener listener : mappinglisteners) {
			listener.allMappingsRemoved();
		}
	}

	/***************************************************************************
	 * Announces that a mapping has been updated.
	 * 
	 * @param srcuri
	 * @param mapping_id
	 * @param mapping
	 */
	private void fireMappigUpdated(URI srcuri, String mapping_id, OBDAMappingAxiom mapping) {
		for (OBDAMappingListener listener : mappinglisteners) {
			listener.mappingUpdated(srcuri, mapping_id, mapping);
		}
	}

	/**
	 * Announces to the listeners that a mapping was deleted.
	 * 
	 * @param mapping_id
	 */
	private void fireMappingDeleted(URI srcuri, String mapping_id) {
		for (OBDAMappingListener listener : mappinglisteners) {
			listener.mappingDeleted(srcuri, mapping_id);
		}
	}

	/**
	 * Announces to the listeners that a mapping was inserted.
	 * 
	 * @param mapping_id
	 */
	private void fireMappingInserted(URI srcuri, String mapping_id) {
		for (OBDAMappingListener listener : mappinglisteners) {
			listener.mappingInserted(srcuri, mapping_id);
		}
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.MappingControllerInterface#getMapping(java.net.URI, java.lang.String)
	 */
	public OBDAMappingAxiom getMapping(URI source_uri, String mapping_id) {
		int pos = indexOfMapping(source_uri, mapping_id);
		if (pos == -1) {
			return null;
		}
		ArrayList<OBDAMappingAxiom> mappings = getMappings(source_uri);
		return mappings.get(pos);
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.MappingControllerInterface#getMappings()
	 */
	public Hashtable<URI, ArrayList<OBDAMappingAxiom>> getMappings() {
		return mappings;
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.MappingControllerInterface#getMappings(java.net.URI)
	 */
	public ArrayList<OBDAMappingAxiom> getMappings(URI datasource_uri) {
		if (datasource_uri == null)
			return null;
		ArrayList<OBDAMappingAxiom> current_mappings = mappings.get(datasource_uri);
		if (current_mappings == null) {
			initMappingsArray(datasource_uri);
		}
		return mappings.get(datasource_uri);
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.MappingControllerInterface#getNextAvailableDuplicateIDforMapping(java.net.URI, java.lang.String)
	 */
	public String getNextAvailableDuplicateIDforMapping(URI source_uri, String originalid) {
		int new_index = -1;
		for (int index = 0; index < 999999999; index++) {
			if (indexOfMapping(source_uri, originalid + "(" + index + ")") == -1) {
				new_index = index;
				break;
			}
		}
		return originalid + "(" + new_index + ")";
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.MappingControllerInterface#getNextAvailableMappingID(java.net.URI)
	 */
	public String getNextAvailableMappingID(URI datasource_uri) {
		int index = 0;
		for (int i = 0; i < 99999999; i++) {
			index = indexOfMapping(datasource_uri, "M:" + Integer.toHexString(i));
			if (index == -1) {
				index = i;
				break;
			}
		}
		return "M:" + Integer.toHexString(index);
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.MappingControllerInterface#indexOfMapping(java.net.URI, java.lang.String)
	 */
	public int indexOfMapping(URI datasource_uri, String mapping_id) {
		ArrayList<OBDAMappingAxiom> current_mappings = mappings.get(datasource_uri);
		if (current_mappings == null) {
			initMappingsArray(datasource_uri);
			current_mappings = mappings.get(datasource_uri);
		}
		int position = -1;
		for (int i = 0; i < current_mappings.size(); i++) {
			if (current_mappings.get(i).getId().equals(mapping_id)) {
				position = i;
				break;
			}
		}
		return position;
	}

	private void initMappingsArray(URI datasource_uri) {
		mappings.put(datasource_uri, new ArrayList<OBDAMappingAxiom>());
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.MappingControllerInterface#insertMapping(java.net.URI, it.unibz.krdb.obda.model.OBDAMappingAxiom)
	 */
	public void addMapping(URI datasource_uri, OBDAMappingAxiom mapping) throws DuplicateMappingException {
		int index = indexOfMapping(datasource_uri, mapping.getId());
		if (index != -1)
			throw new DuplicateMappingException("ID " + mapping.getId());
		mappings.get(datasource_uri).add(mapping);
		fireMappingInserted(datasource_uri, mapping.getId());
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.MappingControllerInterface#removeAllMappings()
	 */
	public void clearMappings() {
		mappings.clear();
		mappings = new Hashtable<URI, ArrayList<OBDAMappingAxiom>>();
		fireAllMappingsRemoved();
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.MappingControllerInterface#removeMappingControllerListener(it.unibz.krdb.obda.model.MappingControllerListener)
	 */
	public void removeMappingsListener(OBDAMappingListener listener) {
		mappinglisteners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.MappingControllerInterface#updateSourceQueryMapping(java.net.URI, java.lang.String, it.unibz.krdb.obda.model.Query)
	 */
	public void updateMappingsSourceQuery(URI datasource_uri, String mapping_id, OBDAQuery sourceQuery) {
		OBDAMappingAxiom mapping = getMapping(datasource_uri, mapping_id);
		mapping.setSourceQuery(sourceQuery);
		fireMappigUpdated(datasource_uri, mapping.getId(), mapping);
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.MappingControllerInterface#updateMapping(java.net.URI, java.lang.String, java.lang.String)
	 */
	public int updateMapping(URI datasource_uri, String mapping_id, String new_mappingid) {
		OBDAMappingAxiom mapping = getMapping(datasource_uri, mapping_id);

		if (!containsMapping(datasource_uri, new_mappingid)) {
			mapping.setId(new_mappingid);
			fireMappigUpdated(datasource_uri, mapping_id, mapping);
			return 0;
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.MappingControllerInterface#updateTargetQueryMapping(java.net.URI, java.lang.String, it.unibz.krdb.obda.model.Query)
	 */
	public void updateTargetQueryMapping(URI datasource_uri, String mapping_id, OBDAQuery targetQuery) {
		OBDAMappingAxiom mapping = getMapping(datasource_uri, mapping_id);
		if (mapping == null) {
			return;
		}
		mapping.setTargetQuery(targetQuery);
		fireMappigUpdated(datasource_uri, mapping.getId(), mapping);
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.MappingControllerInterface#isMappingIdExisted(java.net.URI, java.lang.String)
	 */
	public boolean containsMapping(URI datasourceUri, String mappingId) {
		if (getMapping(datasourceUri, mappingId) != null) {
			return true;
		}
		return false;
	}

	@Override
	public void addMappings(URI datasource_uri, Collection<OBDAMappingAxiom> mappings) throws DuplicateMappingException {
		for (OBDAMappingAxiom map: mappings) {
			addMapping(datasource_uri, map);
		}
		
	}

	
	
}
