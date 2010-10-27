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
package inf.unibz.it.obda.api.io;

import inf.unibz.it.dl.assertion.Assertion;
import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.api.controller.QueryControllerEntity;
import inf.unibz.it.obda.api.controller.exception.DuplicateMappingException;
import inf.unibz.it.obda.codec.xml.DatasourceXMLCodec;
import inf.unibz.it.obda.codec.xml.MappingXMLCodec;
import inf.unibz.it.obda.codec.xml.query.XMLReader;
import inf.unibz.it.obda.codec.xml.query.XMLRenderer;
import inf.unibz.it.obda.constraints.AbstractConstraintAssertionController;
import inf.unibz.it.obda.dependencies.AbstractDependencyAssertionController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerGroup;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerQuery;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSOBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSsourceParameterConstants;
import inf.unibz.it.ucq.parser.exception.QueryParseException;
import inf.unibz.it.utils.io.FileUtils;
import inf.unibz.it.utils.xml.XMLUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/*******************************************************************************
 * Coordinates the saving/loading of the data for the plugin
 *
 * @author Mariano Rodriguez
 *
 */
public class DataManager {

	private static final String											FILE_VERSION_ATTRIBUTE			= "version";

	public static int													CURRENT_OBDA_FILE_VERSION_MAJOR	= 1;
	public static int													CURRENT_OBDA_FILE_VERSION_MINOR	= 0;

	protected HashMap<Class<Assertion>, AssertionController<Assertion>>	assertionControllers			= null;

	protected HashMap<Class<Assertion>, AssertionXMLCodec<Assertion>>		assertionXMLCodecs				= null;

	/** The XML codec to save/load data sources. */
	protected DatasourceXMLCodec dsCodec;

	/** The XML codec to save/load mappings. */
	protected MappingXMLCodec mapCodec;

	/** The XML codec to save queries. */
	protected XMLRenderer xmlRenderer;

	/** The XML codec to load queries. */
	protected XMLReader xmlReader;
	
	protected PrefixManager			prefixManager = null;

	protected APIController apic = null;

	protected Element root;

	Logger																log								= LoggerFactory
																												.getLogger(DataManager.class);

	public DataManager(APIController apic, PrefixManager pref) {
		this.apic = apic;
    this.prefixManager = pref;
		dsCodec = new DatasourceXMLCodec();
		mapCodec = new MappingXMLCodec(apic);
		xmlRenderer = new XMLRenderer();
		xmlReader = new XMLReader();
		assertionControllers = new HashMap<Class<Assertion>, AssertionController<Assertion>>();
		assertionXMLCodecs = new HashMap<Class<Assertion>, AssertionXMLCodec<Assertion>>();
	}

	public <T extends Assertion> void addAssertionController(Class<T> assertionClass, AssertionController<T> controller,
			AssertionXMLCodec<T> codec) {
		assertionControllers.put((Class<Assertion>) assertionClass, (AssertionController<Assertion>) controller);
		assertionXMLCodecs.put((Class<Assertion>) assertionClass, (AssertionXMLCodec<Assertion>) codec);
	}

	/***************************************************************************
	 * Removes the assertion controller which is currently linked to the given
	 * assertionClass
	 *
	 * @param assertionClass
	 */
	public void removeAssertionController(Class<Assertion> assertionClass) {
		assertionControllers.remove(assertionClass);
		assertionXMLCodecs.remove(assertionClass);
	}

	public void saveMappingsToFile(File file) throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.newDocument();
		Element root = doc.createElement("OBDA");
		doc.appendChild(root);
		dumpMappingsToXML(apic.getMappingController().getMappings());
	}

	/***************************************************************************
	 * Saves all the OBDA data of the project to the specified file. If
	 * useTempFile is true, the mechanism will first attempt to save the data
	 * into a temporary file. If this is successful it will attempt to replace
	 * the specified file with the newly created temporarely file.
	 *
	 * If useTempFile is false, the procedure will attempt to directly save all
	 * the data to the file.
	 */
	public void saveOBDAData(URI obdaFileURI, boolean useTempFile) throws IOException, ParserConfigurationException {
		File tempFile = null;
		File obdaFile = new File(obdaFileURI);

		if (useTempFile) {
			tempFile = new File(obdaFileURI.getPath() + ".tmp");
			if (tempFile.exists()) {
				boolean result = tempFile.delete();
				if (!result) {
					throw new IOException("Error deleting temporary file: " + tempFile.toString());
				}
			}
		}

		if (useTempFile) {
			saveOBDAData(tempFile.toURI());
		} else {
			saveOBDAData(obdaFileURI);
			return;
		}

		if (useTempFile) {
			if (obdaFile.exists()) {
				boolean result = obdaFile.delete();
				if (!result) {
					throw new IOException("Error deleting default file: " + obdaFileURI.toString());
				}
			}
			try {
				FileUtils.copy(tempFile.toString(), obdaFileURI.toString());
			} catch (IOException e) {
				System.err.println("WARNING: error while copying temp file.");
			}
			tempFile.delete();
		}
	}

	public void saveOBDAData(URI fileuri) throws ParserConfigurationException, FileNotFoundException, IOException {
		File file = new File(fileuri);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.newDocument();

		// Create the document root
		root = doc.createElement("OBDA");
		Set<String> prefs = prefixManager.getPrefixMap().keySet();
		Iterator<String> sit = prefs.iterator();
		while(sit.hasNext()){
			String key = sit.next();
			if(key.equals("version")){
				root.setAttribute(key, prefixManager.getURIForPrefix(key).toString());
			}else if(key.equals("xml:base")){
				root.setAttribute(key, prefixManager.getURIForPrefix(key).toString());
			}else if(key.equals("xmlns")){
				root.setAttribute(key, prefixManager.getURIForPrefix(key).toString());
			}else{
				root.setAttribute("xmlns:"+key, prefixManager.getURIForPrefix(key).toString());
			}
		}
		doc.appendChild(root);

		// Create the Mapping element
		Hashtable<URI, ArrayList<OBDAMappingAxiom>> mappings =
		    apic.getMappingController().getMappings();
		dumpMappingsToXML(mappings);

		// Create the Data Source element
		HashMap<URI, DataSource> datasources =
		    apic.getDatasourcesController().getAllSources();
		dumpDatasourcesToXML(datasources);

		// Create the Query element
		Vector<QueryControllerEntity> queries =
		    apic.getQueryController().getElements();
		dumpQueriesToXML(queries);

		/***********************************************************************
		 * Appending data of the registred controllers
		 */
		Set<Class<Assertion>> assertionClasses = assertionControllers.keySet();
		for (Iterator<Class<Assertion>> assClassIt = assertionClasses.iterator(); assClassIt.hasNext();) {
			Class<Assertion> assertionClass = assClassIt.next();
			AssertionXMLCodec<Assertion> xmlCodec = assertionXMLCodecs.get(assertionClass);
			AssertionController<Assertion> controller = assertionControllers.get(assertionClass);
			if (controller instanceof AbstractDependencyAssertionController) {
				AbstractDependencyAssertionController depcon = (AbstractDependencyAssertionController) controller;
				Set<URI> ds = datasources.keySet();
				Iterator<URI> it = ds.iterator();
				while (it.hasNext()) {
					URI dsName = it.next();
					Collection<Assertion> assertions = depcon.getAssertionsForDataSource(dsName);
					if (assertions != null && assertions.size() > 0) {
						Element controllerElement = doc.createElement(depcon.getElementTag());
						controllerElement.setAttribute("datasource_uri", dsName.toString());
						for (Assertion assertion : assertions) {
							Element assertionElement = xmlCodec.encode(assertion);
							doc.adoptNode(assertionElement);
							controllerElement.appendChild(assertionElement);
						}
						root.appendChild(controllerElement);
					}
				}
			} else if (controller instanceof AbstractConstraintAssertionController) {
				AbstractConstraintAssertionController constcon = (AbstractConstraintAssertionController) controller;
				Set<URI> ds = datasources.keySet();
				Iterator<URI> it = ds.iterator();
				while (it.hasNext()) {
					URI dsName = it.next();
					Collection<Assertion> assertions = constcon.getAssertionsForDataSource(dsName);
					if (assertions != null && assertions.size() > 0) {
						Element controllerElement = doc.createElement(constcon.getElementTag());
						controllerElement.setAttribute("datasource_uri", dsName.toString());
						for (Assertion assertion : assertions) {
							Element assertionElement = xmlCodec.encode(assertion);
							doc.adoptNode(assertionElement);
							controllerElement.appendChild(assertionElement);
						}
						root.appendChild(controllerElement);
					}
				}
			} else {
				Collection<Assertion> assertions = controller.getAssertions();
				if (assertions.isEmpty())
					continue;
				Element controllerElement = doc.createElement(controller.getElementTag());
				for (Assertion assertion : assertions) {
					Element assertionElement = xmlCodec.encode(assertion);
					doc.adoptNode(assertionElement);
					controllerElement.appendChild(assertionElement);
				}
				root.appendChild(controllerElement);
			}
		}
		XMLUtils.saveDocumentToXMLFile(doc, file.toString());
	}

	/***************************************************************************
	 * Returns the path to an file which has the same name as the given file but
	 * .obda extension. Example For input /path1/file.cclk The output is
	 * /path1/file.obda
	 */
	public URI getOBDAFile(URI urifile) {
		if (urifile == null)
			return null;

		String fileName = urifile.toString();
		int extensionPos = fileName.lastIndexOf(".");
		URI obdaFileName = null;

		if (extensionPos == -1) {
			obdaFileName = URI.create(fileName + ".obda");
		} else {
			obdaFileName = URI.create(fileName.substring(0, extensionPos) + ".obda");
		}

		return obdaFileName;
	}

	/***************************************************************************
	 * Returns the path to an file which has the same name as the given file but
	 * .owl extension. Example For input /path1/file.cclk The output is
	 * /path1/file.owl
	 */
	public File getOWLFile(File file) {
		if (file == null)
			return null;
		String path = file.getParent();
		String fileName = file.getName();
		int extensionPos = fileName.lastIndexOf(".");
		String obdaFileName = "";

		if (extensionPos == -1) {
			obdaFileName = fileName + ".owl";
		} else {
			obdaFileName = fileName.substring(0, extensionPos) + ".owl";
		}

		String obdaFullPath = path + File.separator + obdaFileName;
		return new File(obdaFullPath);
	}

	/***************************************************************************
	 * loads ALL OBDA data from a file
	 */
	public void loadOBDADataFromURI(URI obdaFileURI) {
		File obdaFile = new File(obdaFileURI);
		if (obdaFile == null) {
			System.err.println("OBDAPluging. OBDA file not found.");
			return;
		}

		if (!obdaFile.exists()) {
			return;
		}
		if (!obdaFile.canRead()) {
			System.err.print("WARNING: can't read the OBDA file:" + 
			    obdaFile.toString());
		}
		Document doc = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(obdaFile);
			doc.getDocumentElement().normalize();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		Element root = doc.getDocumentElement(); // OBDA
		if (root.getNodeName() != "OBDA") {
			System.err.println("WARNING: obda info file should start with " +
			    "tag <OBDA>");
			return;
		}

		String file_version = root.getAttribute(FILE_VERSION_ATTRIBUTE);
		int major = -1;
		int minor = -1;
		if ((file_version != null) && !file_version.equals("")) {
			String[] split = file_version.split("[.]");
			major = Integer.valueOf(split[0]);
			minor = Integer.valueOf(split[1]);
		}

		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element node = (Element) children.item(i);
				if (node.getNodeName().equals("mappings")) { // Found mapping block
					try {
				    URI source = URI.create(node.getAttribute("sourceuri"));
						importMappingsFromXML(source, node);
					} catch (QueryParseException e) {
						log.warn(e.getMessage(), e);
					}
				}
				if ((major < 0) && (node.getNodeName().equals("datasource"))) {
					// Found old data-source block
					System.err.println("WARNING: Loading a datasource using the old " +
							    "deprecated method. Update your .obda file by saving " +
							    "it again.");
					DataSource source = dsCodec.decode(node);
					apic.getDatasourcesController().addDataSource(source);
				}
				String newDatasourceTag = dsCodec.getElementTag();
				if ((major > 0) && (node.getNodeName().equals(newDatasourceTag))) {
					// Found new data-source block
					DataSource source = dsCodec.decode(node);
					URI uri= apic.getCurrentOntologyURI();
					if (uri != null) {
						source.setParameter(RDBMSsourceParameterConstants.ONTOLOGY_URI,
						                    uri.toString());
					}
					apic.getDatasourcesController().addDataSource(source);
				}
				if (node.getNodeName().equals("IDConstraints")) {
				  // TODO Implement something.
				}
				if (node.getNodeName().equals("SavedQueries")) {
					// Found queries block
				  importQueriesFromXML(node);
				}

				/***************************************************************
				 * Appending data to the registred controllers based on the XML
				 * tags for them.
				 */
				Set<Class<Assertion>> assertionClasses = assertionControllers.keySet();
				for (Iterator<Class<Assertion>> assClassIt = assertionClasses.iterator(); assClassIt.hasNext();) {
					Class<Assertion> assertionClass = assClassIt.next();
					AssertionXMLCodec<Assertion> xmlCodec = assertionXMLCodecs.get(assertionClass);
					AssertionController<Assertion> controller = assertionControllers.get(assertionClass);
					if (controller instanceof AbstractDependencyAssertionController) {
						if (node.getNodeName().equals(controller.getElementTag())) {
							String ds = node.getAttribute("datasource_uri");
							apic.getDatasourcesController().setCurrentDataSource(URI.create(ds));
							NodeList childrenAssertions = node.getElementsByTagName(xmlCodec.getElementTag());
							for (int j = 0; j < childrenAssertions.getLength(); j++) {
								Element assertionElement = (Element) childrenAssertions.item(j);
								try {
									Assertion assertion = xmlCodec.decode(assertionElement);
									if (assertion != null) {
										controller.addAssertion(assertion);
									}
								} catch (Exception e) {
									log.warn("Error loading assertion: {}", e.toString());
									log.debug(e.getMessage(), e);
								}
							}
						}
					} else if (controller instanceof AbstractConstraintAssertionController) {
						if (node.getNodeName().equals(controller.getElementTag())) {
							String ds = node.getAttribute("datasource_uri");
							apic.getDatasourcesController().setCurrentDataSource(URI.create(ds));
							NodeList childrenAssertions = node.getElementsByTagName(xmlCodec.getElementTag());
							for (int j = 0; j < childrenAssertions.getLength(); j++) {
								Element assertionElement = (Element) childrenAssertions.item(j);
								try {
									Assertion assertion = xmlCodec.decode(assertionElement);
									if (assertion != null) {
										controller.addAssertion(assertion);
									}
								} catch (Exception e) {
									log.warn("Error loading assertion: {}", e.toString());
									log.debug(e.getMessage(), e);
								}
							}
						}
					} else {
						if (node.getNodeName().equals(controller.getElementTag())) {
							NodeList childrenAssertions = node.getElementsByTagName(xmlCodec.getElementTag());
							for (int j = 0; j < childrenAssertions.getLength(); j++) {
								Element assertionElement = (Element) childrenAssertions.item(j);
								try {
									Assertion assertion = xmlCodec.decode(assertionElement);
									controller.addAssertion(assertion);
								} catch (Exception e) {
									log.warn("Error loading assertion: {}", e.toString());
									log.debug(e.getMessage(), e);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public PrefixManager getPrefixManager(){
		return prefixManager;
	}

	/**
	 * Save the mapping data as XML elements. The structure of the XML elements 
	 * from the mapping data follows this construction:
	 * <pre>
	 * {@code
	 * <mappings bodyclass=""
	 *           headclass=""
	 *           sourceuri="">
	 *   <mapping id="">
	 *     <CQ string="">
	 *     <SQLQuery string="">
	 *   </mapping>
	 * </mappings>
	 * }
	 * </pre>
	 *
	 * @param mappings the hash table of the mapping data
	 */
	protected void dumpMappingsToXML(
	    Hashtable<URI, ArrayList<OBDAMappingAxiom>> mappings) {
	  Document doc = root.getOwnerDocument();
	  Enumeration<URI> datasourceUris = mappings.keys();
    URI datasourceUri = null;
    while (datasourceUris.hasMoreElements()) {
	    datasourceUri = datasourceUris.nextElement();

      Element mappingGroup = doc.createElement("mappings");
      mappingGroup.setAttribute("sourceuri", datasourceUri.toString());
      mappingGroup.setAttribute("headclass", inf.unibz.it.ucq.domain.ConjunctiveQuery.class.toString());
      mappingGroup.setAttribute("bodyclass", inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery.class.toString());
      root.appendChild(mappingGroup);

      ArrayList<OBDAMappingAxiom> axioms = mappings.get(datasourceUri);
      int size = axioms.size();
      for (int i = 0; i < size; i++) {
        try {
          OBDAMappingAxiom axiom = axioms.get(i);
  	      Element axiomElement = mapCodec.encode(axiom);
  	      doc.adoptNode(axiomElement);
  	      mappingGroup.appendChild(axiomElement);
        } catch (Exception e) {
          log.warn(e.getMessage(), e);
        }
      }
	  }
  }

	/**
	 * Save the data-source data as XML elements. The structure of the XML 
	 * elements from the data-source data follows this construction:
   * <pre>
   * {@code
   * <dataSource databaseDriver=""
   *             databaseName=""
   *             databaseUrl=""
   *             databaseUsername=""
   * />
   * }
   * </pre>
   * 
	 * @param datasources the hash map of the data-source data.
	 */
  protected void dumpDatasourcesToXML(HashMap<URI, DataSource> datasources) {
    Document doc = root.getOwnerDocument();
    Iterator<URI> datasourceUris = datasources.keySet().iterator();
    URI datasourceUri = null;
    while (datasourceUris.hasNext()) {
      datasourceUri = datasourceUris.next();
      DataSource datasource = datasources.get(datasourceUri);

      Element datasourceElement = dsCodec.encode(datasource);
  	  doc.adoptNode(datasourceElement);
  	  root.appendChild(datasourceElement);
  	}
  }

  /**
   * Save the query data as XML elements. The structure of the XML elements
   * from the query data follows this construction:
   * <pre>
   * {@code
   * <SavedQueries>
   *  <QueryGroup>
   *    <Query id="" text="" />
   *    <Query id="" text="" />
   *    <Query id="" text="" />
   *  </QueryGroup>
   * </SavedQueries>
   * }
   * </pre>
   * 
   * @param queries the vector of the query entities.
   */
  protected void dumpQueriesToXML(Vector<QueryControllerEntity> queries) {
    Document doc = root.getOwnerDocument();
    Element savedQueryElement = doc.createElement("SavedQueries");
    for (QueryControllerEntity query : queries) {
      Element queryElement = xmlRenderer.render(savedQueryElement, query);
      savedQueryElement.appendChild(queryElement);
    }
    root.appendChild(savedQueryElement);
  }
  
  /**
   * Import the mapping data from XML elements. Each mapping has a head class, 
   * a body class and a data-source URI. The method first reads the mapping Id
   * and then saves the pair of mapping head and body as a mapping axiom.
   * 
   * @param datasource the data source URI in which the mappings are linked.
   * @param mappingRoot the mapping root in the XML file.
   * @throws QueryParseException
   * @see ConjunctuveQuery
   * @see RDBMSSQLQuery
   * @see RDBMSOBDAMappingAxiom
   */
  protected void importMappingsFromXML(URI datasource, Element mappingRoot) 
      throws QueryParseException {
    NodeList childs = mappingRoot.getChildNodes();
    for (int i = 0; i < childs.getLength(); i++) {
      try {
        Node child = childs.item(i);
        if (!(child instanceof Element)) {
          continue;
        }
        Element mapping = (Element) child;
        RDBMSOBDAMappingAxiom mappingAxiom = 
            (RDBMSOBDAMappingAxiom) mapCodec.decode(mapping);
        if(mappingAxiom == null){
          throw new Exception("Error while parsing the conjunctive query of "+ 
              "the mapping " + mapping.getAttribute("id"));
        }        
        try {
          apic.getMappingController().insertMapping(datasource, mappingAxiom);
        } catch (DuplicateMappingException e) {
          log.warn("duplicate mapping detected while trying to load mappings " +
              "from file. Ignoring it. Datasource URI: " + datasource + " " +
              "Mapping ID: " + mappingAxiom.getId());
        }
      } catch (Exception e) {
        try {
          log.warn("Error loading mapping with id: {}", 
              ((Element) childs.item(i)).getAttribute("id"));
          log.debug(e.getMessage(), e);
        } catch (Exception e2) {
          log.warn("Error loading mapping");
          log.debug(e.getMessage(), e);
        }
      }
    }
  }
  
  /**
   * Import the query data from XML elements. Several queries can be
   * categorized into one group. The method saves all the queries according
   * to this hierarchical structure.
   * 
   * @param queryRoot the query root in the XML file.
   * @see QueryControllerGroup
   * @see QueryControllerQuery
   */
  protected void importQueriesFromXML(Element queryRoot) {
    NodeList childs = queryRoot.getChildNodes();
    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (node instanceof Element) {
        Element element = (Element) node;
        if (element.getNodeName().equals("Query")) {
          QueryControllerQuery query = xmlReader.readQuery(element);
          apic.getQueryController().addQuery(query.getQuery(), query.getID());
        } else if ((element.getNodeName().equals("QueryGroup"))) {
          QueryControllerGroup group = xmlReader.readQueryGroup(element);
          apic.getQueryController().createGroup(group.getID());
          Vector<QueryControllerQuery> queries = group.getQueries();
          for (QueryControllerQuery query : queries) {
            apic.getQueryController().addQuery(query.getQuery(), query.getID(), 
                group.getID());
          }
        }
      }
    }
  }
}
