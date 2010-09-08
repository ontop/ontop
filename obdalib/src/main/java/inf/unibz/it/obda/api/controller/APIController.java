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
package inf.unibz.it.obda.api.controller;

import inf.unibz.it.dl.assertion.Assertion;
import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.obda.api.io.DataManager;
import inf.unibz.it.obda.api.io.EntityNameRenderer;
import inf.unibz.it.obda.api.io.PrefixManager;
import inf.unibz.it.obda.constraints.parser.ConstraintsRenderer;
import inf.unibz.it.obda.dependencies.parser.DependencyAssertionRenderer;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSsourceParameterConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


public abstract class APIController {

//	private static APIController										controllerInstance		= null;

	private static APICoupler											couplerInstance			= null;

	private HashSet<OntologyControllerListener>							ontologyListeners		= null;

	protected URI															currentOntologyURI		= null;

	private HashMap<Class<Assertion>, AssertionController<Assertion>>	assertionControllers	= null;

	private HashMap<Class<Assertion>, AssertionXMLCodec<Assertion>>		assertionXMLCodecs		= null;

	protected DataManager												ioManager				= null;
	
	protected DatasourcesController dscontroller = null;
	
	protected MappingController mapcontroller = null;
	
	protected QueryController queryController = null;
	
	//renders the Dependency assertions from the obda file
	private DependencyAssertionRenderer dependencyRenderer = null;
	private ConstraintsRenderer constraintsRenderer = null;
	
	
	// the entity name renderer provides the name any entity which belongs to a loaded ontology.
	protected EntityNameRenderer nameRenderer = null;
	
	// a set of all currently loaded ontotlogies
	protected HashSet<String> loadedOntologies = null;

	public APIController() {
		
		dscontroller = new DatasourcesController();
		mapcontroller = new MappingController(dscontroller, this);
		queryController = new QueryController();
		assertionControllers = new HashMap<Class<Assertion>, AssertionController<Assertion>>();
		assertionXMLCodecs = new HashMap<Class<Assertion>, AssertionXMLCodec<Assertion>>();
		loadedOntologies = new HashSet<String>();
		ioManager = new DataManager(this, new PrefixManager());
		
		dependencyRenderer = new DependencyAssertionRenderer(this);
		constraintsRenderer = new ConstraintsRenderer(this);
	}
	
	public QueryController getQueryController() {
		return queryController;
	}
	
	public DatasourcesController getDatasourcesController() {
		return dscontroller;
	}
	
	public MappingController getMappingController() {
		return mapcontroller;
	}

	public AssertionController<?> getController(Class<?> assertionClass) {
		return (AssertionController<?>) assertionControllers.get(assertionClass);
	}

	/***************************************************************************
	 * Sets the current APICoupler. An object which is able to interact with the
	 * current ontology API (e.g., OWL-API, Protege-OWL, Neon) and do certain
	 * operations over it. For example, checking wether a named object is a
	 * Property or Concepts, etc.
	 * 
	 * @param coupler
	 */
	public void setCoupler(APICoupler coupler) {
		APIController.couplerInstance = coupler;
		nameRenderer = new EntityNameRenderer(this);
		nameRenderer.setCoupler(coupler);
	}

	/***************************************************************************
	 * Gets the current APICoupler. An object which is able to interact with the
	 * current ontology API (e.g., OWL-API, Protege-OWL, Neon) and do certain
	 * operations over it. For example, checking if a named object is a Property
	 * or Concepts, etc.
	 * 
	 * @param coupler
	 */
	public APICoupler getCoupler() {
		return couplerInstance;
	}

	public DataManager getIOManager() {
		return this.ioManager;
	}

	/***************************************************************************
	 * Registers a new assertion controller. These are used during
	 * saving/loading
	 * 
	 * @param controller
	 */
	public <T extends Assertion> void addAssertionController(Class<T> assertionClass, AssertionController<T> controller,
			AssertionXMLCodec<T> codec) {
		assertionControllers.put((Class<Assertion>) assertionClass, (AssertionController<Assertion>) controller);
		ioManager.addAssertionController(assertionClass, controller, codec);
		assertionXMLCodecs.put((Class<Assertion>) assertionClass, (AssertionXMLCodec<Assertion>) codec);

	}

	//TODO Fix remove assertion controller, API is wrong, should give the controller intance to remove
	/***************************************************************************
	 * Removes the assertion controller which is currently linked to the given
	 * assertionClass
	 * 
	 * @param assertionClass
	 */
	public void removeAssertionController(Class assertionClass) {
		assertionControllers.remove(assertionClass);
		assertionXMLCodecs.remove(assertionClass);
		ioManager.removeAssertionController(assertionClass);
	}
	
	

	public void addOntologyControllerListener(OntologyControllerListener listener) {
		getOntologyControllerListeners().add(listener);
	}

	public Collection<OntologyControllerListener> getOntologyControllerListeners() {
		if (ontologyListeners == null) {
			ontologyListeners = new HashSet<OntologyControllerListener>();
		}
		return ontologyListeners;
	}

	/***
	 * Sets the current ontology URI and loads all .obda data for the current
	 * obda file.
	 * 
	 * @param uri
	 */
	public void setCurrentOntologyURI(URI uri) {
		URI oldURI = currentOntologyURI;
		currentOntologyURI = uri;
//		dscontroller.currentOntologyChanged(uri, oldURI);
//
//		mapcontroller.removeAllMappings();
//		dscontroller.removeAllSources();
//		queryController.removeAllQueriesAndGroups();
//
//		Set<Class<Assertion>> registredAssertions = assertionControllers.keySet();
//		for (Class<Assertion> assertionClass : registredAssertions) {
//			AssertionController<Assertion> controller = assertionControllers.get(assertionClass);
//			controller.clear();
//		}

//		ioManager.loadOBDADataFromFile(ioManager.getOBDAFile(getCurrentOntologyFile()));
		mapcontroller.activeOntologyChanged();
		
	}

	public URI getCurrentOntologyURI() {
		return currentOntologyURI;
	}

	// private void fireCurrentOntologyChanged(URI uri, URI oldURI) {
	// DatasourcesController.getInstance().removeAllSources();
	// for (Iterator<OntologyControllerListener> iterator =
	// ontologyListeners.iterator(); iterator.hasNext();) {
	// OntologyControllerListener type = (OntologyControllerListener)
	// iterator.next();
	// type.currentOntologyChanged(uri, oldURI);
	// }
	// }

	public abstract URI getPhysicalURIOfOntology(URI onto);
	
	public abstract File getCurrentOntologyFile();

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
	 * Gets the set of URI's for the currently loaded ontologies, i.e., ontologies
	 * for which mappings have been loaded.
	 * 
	 * @return
	 */
	public abstract Set<URI> getOntologyURIs();
	
	/**
	 * Returns the current entity name renderer
	 * @return the current entity name renderer
	 */
	public EntityNameRenderer getEntityNameRenderer(){
		return nameRenderer;
	}

	/**
	 * Adds the given ontology uri to the set of already 
	 * loaded ontologies uri's.
	 * 
	 * @param ontoUri
	 */
	
	public void markAsLoaded(URI ontoUri){
		loadedOntologies.add(ontoUri.toString());
	}
	
	
	/**
	 * Removes the ontolgy identified by the given URI from the set of all
	 * currently loaded ontologies and all other objects (data sources, mappings, etc)
	 * 
	 * @param ontoUri the URI of the ontology to remove
	 */
	public void unloaded(URI ontoUri){
		loadedOntologies.remove(ontoUri.toString());
		HashMap<URI, DataSource> map =dscontroller.getAllSources();
		Set<URI> set = map.keySet();
		Iterator<URI> it = set.iterator();
		boolean controlle = false;
		Vector<URI> dstoDelete = new Vector<URI>();
		while(it.hasNext()){
			DataSource ds = map.get(it.next());
			String dsuri = ds.getParameter(RDBMSsourceParameterConstants.ONTOLOGY_URI);
			if(dsuri.equals(ontoUri.toString())){
				dstoDelete.add(ds.getSourceID());
				controlle = true;
			}
		}
		if(!controlle){
			System.err.println("ERROR: NO data source deleted after an ontology was deleted");
		}
		Iterator<URI> it2 = dstoDelete.iterator();
		while(it2.hasNext()){
			dscontroller.removeDataSource(it2.next());
		}		
		couplerInstance.removeOntology(ontoUri);
	}
}
