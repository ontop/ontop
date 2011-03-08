/***
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
package inf.unibz.it.obda.api.controller;

import inf.unibz.it.obda.codec.xml.DatasourceXMLCodec;
import inf.unibz.it.obda.domain.DataSource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class DatasourcesController implements OntologyControllerListener {

	private final DatasourceXMLCodec							codec				= new DatasourceXMLCodec();

	private HashMap<URI, DataSource>					datasources			= null;

	private ArrayList<DatasourcesControllerListener>	listeners			= null;

	private DataSource									currentdatasource	= null;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public DatasourcesController() {
		datasources = new HashMap<URI, DataSource>();
		listeners = new ArrayList<DatasourcesControllerListener>();
	}

	public synchronized void addDataSource(DataSource source) {
		datasources.put(source.getSourceID(), source);
		fireDatasourceAdded(source);
	}

	public synchronized void addDataSource(String srcid) {
		URI dburi = URI.create(srcid);
		DataSource newsource = new DataSource(dburi);
		datasources.put(dburi, newsource);
		fireDatasourceAdded(newsource);
	}

	public synchronized void addDatasourceControllerListener(DatasourcesControllerListener listener) {
		if (listeners.contains(listener))
			return;
		listeners.add(listener);
	}

	public void fireAllDatasourcesDeleted() {
		for (DatasourcesControllerListener listener : listeners) {
			listener.alldatasourcesDeleted();
		}
	}

	// TODO Remove this method later.
	public void fireCurrentDatasourceChanged(DataSource previousdatasource, DataSource source) {
//		for (DatasourcesControllerListener listener : listeners) {
//			listener.currentDatasourceChange(previousdatasource, source);
//		}
	}

	public void fireDatasourceAdded(DataSource source) {
		for (DatasourcesControllerListener listener : listeners) {
			listener.datasourceAdded(source);
		}
	}

	public void fireDatasourceDeleted(DataSource source) {
		for (DatasourcesControllerListener listener : listeners) {
			listener.datasourceDeleted(source);
		}
	}

	public void fireParametersUpdated(){
		for (DatasourcesControllerListener listener : listeners) {
			listener.datasourcParametersUpdated();
		}
	}

	public void fireDataSourceNameUpdated(URI old, DataSource neu){
		for (DatasourcesControllerListener listener : listeners) {
			listener.datasourceUpdated(old.toString(), neu);
		}
	}

	public HashMap<URI, DataSource> getAllSources() {
		return datasources;
	}

	/***
	 * Gets all sources for Ontology
	 *
	 * @param ontologyURI
	 * @return
	 */
	public Set<DataSource> getDatasources(URI ontologyURI) {
		HashSet<DataSource> ontoSources = new HashSet<DataSource>();
		Collection<DataSource> allSources = datasources.values();
		for (Iterator<DataSource> iterator = allSources.iterator(); iterator.hasNext();) {
			DataSource dataSource = iterator.next();
			if (dataSource.getSourceID().equals(ontologyURI.toString())) {
				ontoSources.add(dataSource);
			}
		}
		return ontoSources;
	}

	// TODO remove this method, no such thing as current datasource, use an
	// outside coordinator if needed for GUI code
	public synchronized DataSource getCurrentDataSource() {
		return currentdatasource;
	}

	public synchronized DataSource getDataSource(URI name) {

		return datasources.get(name);
	}

	/***
	 * Use a DatasourceXML codec instead
	 *
	 * @param sourceelement
	 */
	@Deprecated
	public void loadDatasourceFromXML(Element sourceelement) {
		DatasourceXMLCodec codec = new DatasourceXMLCodec();
		DataSource source = codec.decode(sourceelement);
		addDataSource(source);
	}

	/***
	 * Use a DatasourceXMLCodec instead
	 *
	 * @param strsources
	 * @throws Exception
	 */
	@Deprecated
	public synchronized void loadSourcesFromString(String strsources) throws Exception {
		try {
			datasources = DataSource.decodeDataSources(strsources);
		} catch (Exception e) {
			log.error("Error while parsing the data source");
			throw e;
		}
	}

	public void removeAllSources() {
		while (!datasources.values().isEmpty()) {
			DataSource source = datasources.values().iterator().next();
			datasources.remove(source.getSourceID());
		}

		fireAllDatasourcesDeleted();
	}

	public synchronized void removeDataSource(URI id) {

		DataSource source = getDataSource(id);
		datasources.remove(id);
		fireDatasourceDeleted(source);
	}

	public synchronized void removeDatasourceControllerListener(DatasourcesControllerListener listener) {
		listeners.remove(listener);
	}

	// TODO Remove this method later.
	public synchronized void setCurrentDataSource(URI id) {
//		DataSource previous = currentdatasource;
//		if ((id != null) && (!id.equals(""))) {
//			DataSource ds = datasources.get(id);
//			currentdatasource = ds;
//			fireCurrentDatasourceChanged(previous, currentdatasource);
//		} else {
//			currentdatasource = null;
//			fireCurrentDatasourceChanged(previous, currentdatasource);
//		}
	}

	public synchronized void updateDataSource(URI id, DataSource dsd) {
	  datasources.remove(id);
		datasources.put(dsd.getSourceID(), dsd);
		fireDataSourceNameUpdated(id, dsd);
	}

	public void currentOntologyChanged(URI uri, URI oldURI) {
	  // TODO Try to do a different implementation.
	}

}
