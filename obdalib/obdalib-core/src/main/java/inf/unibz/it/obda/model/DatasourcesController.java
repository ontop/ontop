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
package inf.unibz.it.obda.model;

import inf.unibz.it.obda.codec.DatasourceXMLCodec;
import inf.unibz.it.obda.model.impl.OBDADataFactoryImpl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasourcesController {

	private final DatasourceXMLCodec							codec				= new DatasourceXMLCodec();

	private HashMap<URI, DataSource>					datasources			= null;

	private ArrayList<DatasourcesControllerListener>	listeners			= null;
	
	OBDADataFactory fac = null;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public DatasourcesController() {
		fac = OBDADataFactoryImpl.getInstance();
		datasources = new HashMap<URI, DataSource>();
		listeners = new ArrayList<DatasourcesControllerListener>();
	}

	public synchronized void addDataSource(DataSource source) {
		datasources.put(source.getSourceID(), source);
		fireDatasourceAdded(source);
	}

	public synchronized void addDataSource(String srcid) {
		URI dburi = URI.create(srcid);
		DataSource newsource = fac.getDataSource(dburi);
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

	public List<DataSource> getAllSources() {
		List<DataSource> sources = new LinkedList<DataSource>(datasources.values());
		return Collections.unmodifiableList(sources);
	}


	public synchronized DataSource getDataSource(URI name) {

		return datasources.get(name);
	}



	public synchronized void removeDataSource(URI id) {

		DataSource source = getDataSource(id);
		datasources.remove(id);
		fireDatasourceDeleted(source);
	}

	public synchronized void removeDatasourceControllerListener(DatasourcesControllerListener listener) {
		listeners.remove(listener);
	}



	public synchronized void updateDataSource(URI id, DataSource dsd) {
	  datasources.remove(id);
		datasources.put(dsd.getSourceID(), dsd);
		fireDataSourceNameUpdated(id, dsd);
	}

	
}
