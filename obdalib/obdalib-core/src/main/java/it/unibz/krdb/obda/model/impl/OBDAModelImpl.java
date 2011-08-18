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
import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.io.SimplePrefixManager;
import it.unibz.krdb.obda.model.DataSource;
import it.unibz.krdb.obda.model.OBDAModelListener;
import it.unibz.krdb.obda.model.MappingController;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.queryanswering.QueryController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OBDAModelImpl implements OBDAModel {

	protected URI					currentOntologyURI	= null;

//	protected OBDAModelImpl	dscontroller		= null;

	private MappingController		mapcontroller		= null;

	protected QueryController		queryController		= null;

	private PrefixManager			prefman				= null;

	protected final Logger			log					= LoggerFactory.getLogger(this.getClass());
	
	
	/***
	 * Datasources
	 */
	
	private final DatasourceXMLCodec							codec				= new DatasourceXMLCodec();

	private HashMap<URI, DataSource>					datasources			= null;

	private ArrayList<OBDAModelListener>	listeners			= null;
	
	OBDADataFactory fac = null;


	protected OBDAModelImpl() {

//		dscontroller = new OBDAModelImpl();
		mapcontroller = new MappingController(this);
		queryController = new QueryController();

		setPrefixManager(new SimplePrefixManager());
		log.debug("OBDA Lib initialized");
		
		fac = OBDADataFactoryImpl.getInstance();
		datasources = new HashMap<URI, DataSource>();
		listeners = new ArrayList<OBDAModelListener>();


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

	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.model.OBDAModel#getMappingController()
	 */
	@Override
	public MappingController getMappingController() {
		return this.mapcontroller;
	}

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
	
	public void addDataSource(DataSource source) {
		datasources.put(source.getSourceID(), source);
		fireDatasourceAdded(source);
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#addDataSource(java.lang.String)
	 */
	public void addDataSource(String srcid) {
		URI dburi = URI.create(srcid);
		DataSource newsource = fac.getDataSource(dburi);
		datasources.put(dburi, newsource);
		fireDatasourceAdded(newsource);
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#addDatasourceControllerListener(it.unibz.krdb.obda.model.DatasourcesControllerListener)
	 */
	public void addDatasourceControllerListener(OBDAModelListener listener) {
		if (listeners.contains(listener))
			return;
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#fireAllDatasourcesDeleted()
	 */
	public void fireAllDatasourcesDeleted() {
		for (OBDAModelListener listener : listeners) {
			listener.alldatasourcesDeleted();
		}
	}


	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#fireDatasourceAdded(it.unibz.krdb.obda.model.DataSource)
	 */
	public void fireDatasourceAdded(DataSource source) {
		for (OBDAModelListener listener : listeners) {
			listener.datasourceAdded(source);
		}
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#fireDatasourceDeleted(it.unibz.krdb.obda.model.DataSource)
	 */
	public void fireDatasourceDeleted(DataSource source) {
		for (OBDAModelListener listener : listeners) {
			listener.datasourceDeleted(source);
		}
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#fireParametersUpdated()
	 */
	public void fireParametersUpdated(){
		for (OBDAModelListener listener : listeners) {
			listener.datasourcParametersUpdated();
		}
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#fireDataSourceNameUpdated(java.net.URI, it.unibz.krdb.obda.model.DataSource)
	 */
	public void fireDataSourceNameUpdated(URI old, DataSource neu){
		for (OBDAModelListener listener : listeners) {
			listener.datasourceUpdated(old.toString(), neu);
		}
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#getAllSources()
	 */
	public List<DataSource> getAllSources() {
		List<DataSource> sources = new LinkedList<DataSource>(datasources.values());
		return Collections.unmodifiableList(sources);
	}


	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#getDataSource(java.net.URI)
	 */
	public DataSource getDataSource(URI name) {
		return datasources.get(name);
	}

	public boolean containsDatasource(URI name) {
	  if (getDataSource(name) != null) {
	    return true;
	  }
	  return false;
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#removeDataSource(java.net.URI)
	 */
	public void removeDataSource(URI id) {

		DataSource source = getDataSource(id);
		datasources.remove(id);
		fireDatasourceDeleted(source);
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#removeDatasourceControllerListener(it.unibz.krdb.obda.model.DatasourcesControllerListener)
	 */
	public void removeDatasourceControllerListener(OBDAModelListener listener) {
		listeners.remove(listener);
	}



	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.model.impl.DatasourcesController#updateDataSource(java.net.URI, it.unibz.krdb.obda.model.DataSource)
	 */
	public void updateDataSource(URI id, DataSource dsd) {
	  datasources.remove(id);
		datasources.put(dsd.getSourceID(), dsd);
		fireDataSourceNameUpdated(id, dsd);
	}
}
