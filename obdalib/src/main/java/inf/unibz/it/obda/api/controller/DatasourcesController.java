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
import inf.unibz.it.obda.gui.swing.datasource.DatasourceTreeModel;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.RuntimeErrorException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DatasourcesController implements OntologyControllerListener {

	private static DatasourcesController				instance			= null;

	private DatasourceXMLCodec							codec				= new DatasourceXMLCodec();

	// public synchronized static DatasourcesController getInstance() {
	// if (instance == null) {
	// instance = new DatasourcesController();
	// }
	// return instance;
	// }

	private HashMap<URI, DataSource>					datasources			= null;

	private DatasourceTreeModel							treeModel			= null;

	private ArrayList<DatasourcesControllerListener>	listeners			= null;

	private DataSource									currentdatasource	= null;
	
	// private APIController obdacont = null;

	public DatasourcesController() {

		datasources = new HashMap<URI, DataSource>();
		treeModel = new DatasourceTreeModel();
		listeners = new ArrayList<DatasourcesControllerListener>();
		addDatasourceControllerListener(treeModel);

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

	@Deprecated
	public void dumpDatasourcesToXML(Element root) {
		Iterator<URI> datasource_names = datasources.keySet().iterator();
		while (datasource_names.hasNext()) {
			dumpDatasourceToXML(root, datasource_names.next());
		}
	}

	@Deprecated
	public void dumpDatasourceToXML(Element root, URI datasource_uri) {

		DataSource source = getDataSource(datasources.get(datasource_uri).getSourceID());
		Document doc = root.getOwnerDocument();

		Element domDatasource = codec.encode(source);
		doc.adoptNode(domDatasource);
		root.appendChild(domDatasource);

		// Element datasourceelement = doc.createElement("datasource");
		// datasourceelement.setAttribute("sourcename", datasource_name);
		// datasourceelement.setAttribute("string", source.toString());

		// root.appendChild(domDatasource);
	}

	public void fireAllDatasourcesDeleted() {
		for (DatasourcesControllerListener listener : listeners) {
			listener.alldatasourcesDeleted();
		}
	}

	public void fireCurrentDatasourceChanged(DataSource previousdatasource, DataSource source) {
		for (DatasourcesControllerListener listener : listeners) {
			listener.currentDatasourceChange(previousdatasource, source);
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
			DataSource dataSource = (DataSource) iterator.next();
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

	/***************************************************************************
	 * Returns the tree model for the data sources loaded
	 * 
	 * @return a DataSource object with the current data source, NULL if no data
	 *         sources is currently active.
	 */
	public synchronized DatasourceTreeModel getTreeModel() {
		return treeModel;
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
			System.err.println("WARNING: Error parsing datasources");
			throw e;
		}

		// // TODO: Remove when saving and loading is ok
		// if (datasources == null) {
		// // System.out.println("Sources NULL, creating new source");
		// DataSource new_src = new DataSource("name1");
		// new_src.setUri("src_uri");
		// new_src.setParameter("parmuri1", "value1");
		// datasources = new HashMap<String, DataSource>();
		// datasources.put("name1", new_src);
		// } else {
		// // System.out.println("Sources NOT NULL, printing");
		// // System.out.print(datasources.toString());
		// }
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
		// MappingController mcontroller = MappingController.getInstance();
		// mcontroller.deleteMappings(name);
		fireDatasourceDeleted(source);
	}

	public synchronized void removeDatasourceControllerListener(DatasourcesControllerListener listener) {
		listeners.remove(listener);
	}

	public synchronized void setCurrentDataSource(URI id) {
		DataSource previous = currentdatasource;
		if ((id != null) && (!id.equals(""))) {
			DataSource ds = datasources.get(id);
			currentdatasource = ds;
			fireCurrentDatasourceChanged(previous, currentdatasource);
		} else {
			currentdatasource = null;
			fireCurrentDatasourceChanged(previous, currentdatasource);
		}
	}

	public synchronized void updateDataSource(URI id, DataSource dsd) {
		
		DataSource oldds = datasources.remove(id);
		datasources.put(dsd.getSourceID(), dsd);
		treeModel.datasourceUpdated(id.toString(), dsd);
		fireDataSourceNameUpdated(id, dsd);
	}

	public void currentOntologyChanged(URI uri, URI oldURI) {
		treeModel.currentOntologyChanged(uri);
		// fireAllDatasourcesDeleted();
		// Collection<DataSource> sources = getAllSources().values();
		// for (DataSource dataSource : sources) {
		// fireDatasourceAdded(dataSource);
		// }
	}

}
