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
package inf.unibz.it.obda.model;

import inf.unibz.it.obda.io.DataManager;
import inf.unibz.it.obda.io.PrefixManager;
import inf.unibz.it.obda.io.SimplePrefixManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class APIController {

	protected URI														currentOntologyURI		= null;

	protected DataManager												ioManager				= null;

	protected DatasourcesController										dscontroller			= null;

	private MappingController											mapcontroller			= null;

	protected QueryController											queryController			= null;

	private PrefixManager											prefman					= null;

	protected final Logger												log						= LoggerFactory.getLogger(this.getClass());

	public APIController() {

		dscontroller = new DatasourcesController();
		mapcontroller = new MappingController(dscontroller, this);
		queryController = new QueryController();
		
		setPrefixManager(new SimplePrefixManager());
		ioManager = new DataManager(this);
		
		log.debug("OBDA Lib initialized");

	}

	public QueryController getQueryController() {
		return queryController;
	}

	public DatasourcesController getDatasourcesController() {
		return dscontroller;
	}

	public MappingController getMappingController() {
		return this.mapcontroller;
	}


	public DataManager getIOManager() {
		return this.ioManager;
	}



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

	/***
	 * Gets the set of URI's for the currently loaded ontologies, i.e.,
	 * ontologies for which mappings have been loaded.
	 * 
	 * @return
	 */
	// public abstract Set<URI> getOntologyURIs();

//	/**
//	 * Returns the current entity name renderer
//	 * 
//	 * @return the current entity name renderer
//	 */
//	public EntityNameRenderer getEntityNameRenderer() {
//		return nameRenderer;
//	}

//	/**
//	 * Adds the given ontology uri to the set of already loaded ontologies
//	 * uri's.
//	 * 
//	 * @param ontoUri
//	 */
//
//	public void markAsLoaded(URI ontoUri) {
//		getLoadedOntologies().add(ontoUri.toString());
//	}

//	/**
//	 * Removes the ontolgy identified by the given URI from the set of all
//	 * currently loaded ontologies and all other objects (data sources,
//	 * mappings, etc)
//	 * 
//	 * @param ontoUri
//	 *            the URI of the ontology to remove
//	 */
//	public void unloaded(URI ontoUri) {
//		getLoadedOntologies().remove(ontoUri.toString());
//		HashMap<URI, DataSource> map = dscontroller.getAllSources();
//		Set<URI> set = map.keySet();
//		Iterator<URI> it = set.iterator();
//		boolean controlle = false;
//		Vector<URI> dstoDelete = new Vector<URI>();
//		while (it.hasNext()) {
//			DataSource ds = map.get(it.next());
//			String dsuri = ds.getParameter(RDBMSsourceParameterConstants.ONTOLOGY_URI);
//			if (dsuri.equals(ontoUri.toString())) {
//				dstoDelete.add(ds.getSourceID());
//				controlle = true;
//			}
//		}
//		if (!controlle) {
//			log.error("ERROR: NO data source deleted after an ontology was deleted");
//		}
//		Iterator<URI> it2 = dstoDelete.iterator();
//		while (it2.hasNext()) {
//			dscontroller.removeDataSource(it2.next());
//		}
//	}

//	public void setMapcontroller(MappingController mapcontroller) {
//		this.mapcontroller = mapcontroller;
//	}
//
//	public MappingController getMapcontroller() {
//		return mapcontroller;
//	}

//	public void setLoadedOntologies(HashSet<String> loadedOntologies) {
//		this.loadedOntologies = loadedOntologies;
//	}

//	public HashSet<String> getLoadedOntologies() {
//		return loadedOntologies;
//	}

	public void setPrefixManager(PrefixManager prefman) {
		this.prefman = prefman;
	}

	public PrefixManager getPrefixManager() {
		return prefman;
	}
}
