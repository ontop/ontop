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
import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.api.controller.QueryController;
import inf.unibz.it.obda.codec.xml.DatasourceXMLCodec;
import inf.unibz.it.obda.constraints.AbstractConstraintAssertionController;
import inf.unibz.it.obda.dependencies.AbstractDependencyAssertionController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.ucq.parser.exception.QueryParseException;
import inf.unibz.it.utils.io.FileUtils;
import inf.unibz.it.utils.xml.XMLUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

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

	private HashMap<Class<Assertion>, AssertionController<Assertion>>	assertionControllers			= null;

	private HashMap<Class<Assertion>, AssertionXMLCodec<Assertion>>		assertionXMLCodecs				= null;

	/***
	 * The XML Codec used to save/load datasources
	 */
	DatasourceXMLCodec													dsCodec							= new DatasourceXMLCodec();

	private DatasourcesController										dscontroller					= null;

	private MappingController											mapcontroller					= null;

	private QueryController												queryController					= null;

	Logger																log								= LoggerFactory
																												.getLogger(DataManager.class);

	public DataManager(DatasourcesController dscontroller, MappingController mapcontroller, QueryController queryController) {
		this.dscontroller = dscontroller;
		this.mapcontroller = mapcontroller;
		this.queryController = queryController;
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
		// MappingController mmanager = MappingController.getInstance();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.newDocument();
		Element root = doc.createElement("OBDA");
		doc.appendChild(root);
		mapcontroller.dumpMappingsToXML(root);

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
		Element root = doc.createElement("OBDA");
		root.setAttribute(FILE_VERSION_ATTRIBUTE, CURRENT_OBDA_FILE_VERSION_MAJOR + "." + CURRENT_OBDA_FILE_VERSION_MINOR);
		doc.appendChild(root);

		mapcontroller.dumpMappingsToXML(root);
		dscontroller.dumpDatasourcesToXML(root);
		// IDConstraintController constraints_controller =
		// IDConstraintController.getInstance();
		// Element domconstraints = constraints_controller.toDOM(root);
		// root.appendChild(domconstraints);

		Element dom_queries = queryController.toDOM(root);
		root.appendChild(dom_queries);

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
				HashMap<String, DataSource> sources = dscontroller.getAllSources();
				Set<String> ds = sources.keySet();
				Iterator<String> it = ds.iterator();
				while (it.hasNext()) {
					String dsName = it.next();
					Collection<Assertion> assertions = depcon.getAssertionsForDataSource(dsName);
					if (assertions != null && assertions.size() > 0) {
						Element controllerElement = doc.createElement(depcon.getElementTag());
						controllerElement.setAttribute("datasource_uri", dsName);
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
				HashMap<String, DataSource> sources = dscontroller.getAllSources();
				Set<String> ds = sources.keySet();
				Iterator<String> it = ds.iterator();
				while (it.hasNext()) {
					String dsName = it.next();
					Collection<Assertion> assertions = constcon.getAssertionsForDataSource(dsName);
					if (assertions != null && assertions.size() > 0) {
						Element controllerElement = doc.createElement(constcon.getElementTag());
						controllerElement.setAttribute("datasource_uri", dsName);
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

		// if (doc.getDocumentElement() == null) {
		// System.err.println("WARNING: there was no OBDA data to save");
		// return;
		// }

		XMLUtils.saveDocumentToXMLFile(doc, file.toString());
	}

	// /***************************************************************************
	// * Loads the mappings stored in the default file for all datasources
	// */
	// public static void loadMappingsFromFile(File file) {
	// OBDAPluginController controller =
	// OBDAPluginController.getCurrentInstance();
	// Project project = controller.getCurrentProject();
	// String projectname = project.getName();
	// // TODO update to use URIs
	// String projectpath = project.getProjectDirectoryFile().toString();
	//
	// // TODO update to use the default systems folder separator
	// File file = new File(projectpath + File.separator + projectname +
	// ".obda");
	//
	// if (!file.exists()) {
	// return;
	// }
	// if (!file.canRead()) {
	// System.err.print("WARNING: can't read the OBDA file for project " +
	// projectname);
	// }
	// Document doc = null;
	// try {
	//
	// DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	// DocumentBuilder db = dbf.newDocumentBuilder();
	// doc = db.parse(file);
	// doc.getDocumentElement().normalize();
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// return;
	// }
	//		
	// Element root = doc.getDocumentElement(); //OBDA
	// if (root.getNodeName() != "OBDA") {
	// System.err.println("WARNING: obda info file should start with tag
	// <OBDA>");
	// return;
	// }
	//		
	// NodeList children = root.getChildNodes();
	// for (int i = 0; i < children.getLength(); i++) {
	// if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
	// Element node = (Element) children.item(i);
	//
	// if (node.getNodeName().equals("mappings")) {
	// // FOUND MAPPINGS BLOCK
	// MappingController manager = MappingController.getInstance();
	// manager.importMappingsFromXML(node);
	// }
	// }
	// }
	//		
	// System.out.println("OBDAPlugin DataManager: loading data finished.");
	//
	// }

	/***************************************************************************
	 * Returns the path to an file which has the same name as the given file but
	 * .obda extension. Example For input /path1/file.cclk The output is
	 * /path1/file.obda
	 * 
	 */
	public URI getOBDAFile(URI urifile) {
		// File file = new File(urifile);
		if (urifile == null)
			return null;

		// String path = urifile.getParent();
		String fileName = urifile.toString();
		int extensionPos = fileName.lastIndexOf(".");
		URI obdaFileName = null;

		if (extensionPos == -1) {
			obdaFileName = URI.create(fileName + ".obda");
		} else {
			obdaFileName = URI.create(fileName.substring(0, extensionPos) + ".obda");
		}
		// String obdaFullPath = path + File.separator + obdaFileName;

		return obdaFileName;
	}

	/***************************************************************************
	 * Returns the path to an file which has the same name as the given file but
	 * .owl extension. Example For input /path1/file.cclk The output is
	 * /path1/file.owl
	 * 
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
//		System.out.println(obdaFileURI);
		File obdaFile = new File(obdaFileURI);
		if (obdaFile == null) {
			System.err.println("OBDAPluging. OBDA file not found.");
			return;
		}

		// File file = new File(projectpath + File.separator + projectname +
		// ".obda");

		if (!obdaFile.exists()) {
			return;
		}
		if (!obdaFile.canRead()) {
			System.err.print("WARNING: can't read the OBDA file:" + obdaFile.toString());
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
			System.err.println("WARNING: obda info file should start with tag <OBDA>");
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

				if (node.getNodeName().equals("mappings")) {
					// FOUND MAPPINGS BLOCK

					try {
						mapcontroller.importMappingsFromXML(node);
					} catch (QueryParseException e) {
						log.warn(e.getMessage(), e);

					}
				}

				if ((major < 0) && (node.getNodeName().equals("datasource"))) {
					// FOUND Datasources BLOCK
					System.err
							.println("WARNING: Loading a datasource using the old deprecated method. Update your .obda file by saving it again.");
					dscontroller.loadDatasourceFromXML(node);

				}
				if ((major > 0) && (node.getNodeName().equals(dsCodec.getElementTag()))) {
					// FOUND Datasources BLOCK

					DataSource source = dsCodec.decode(node);
					dscontroller.addDataSource(source);
				}

				if (node.getNodeName().equals("IDConstraints")) {
					// System.err.println("WARNING: Ignoring IDConstraints tag");
					// 
					// IDConstraintController idcontroller =
					// IDConstraintController.getInstance();
					// idcontroller.fromDOM(node);
				}

				if (node.getNodeName().equals("SavedQueries")) {
					// FOUND IDConstraints BLOCK
					// OWLModel model =
					// OBDAPluginController.getCurrentInstance().getCurrentOWLModel();

					queryController.fromDOM(node);
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
							dscontroller.setCurrentDataSource(ds);
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
							dscontroller.setCurrentDataSource(ds);
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
//		System.out.println("OBDAPlugin DataManager: loading data finished.");
	}
}
