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

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.QueryControllerEntity;
import inf.unibz.it.obda.api.controller.exception.DuplicateMappingException;
import inf.unibz.it.obda.codec.xml.DatasourceXMLCodec;
import inf.unibz.it.obda.codec.xml.MappingXMLCodec;
import inf.unibz.it.obda.codec.xml.query.XMLReader;
import inf.unibz.it.obda.codec.xml.query.XMLRenderer;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerGroup;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerQuery;
import inf.unibz.it.obda.model.impl.CQIEImpl;
import inf.unibz.it.obda.model.rdbms.impl.RDBMSOBDAMappingAxiom;
import inf.unibz.it.obda.model.rdbms.impl.RDBMSSQLQuery;
import inf.unibz.it.obda.model.rdbms.impl.RDBMSsourceParameterConstants;
import inf.unibz.it.obda.tool.utils.XMLUtils;

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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.query.QueryParseException;

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

	/** The XML codec to save/load data sources. */
	protected DatasourceXMLCodec										dsCodec;

	/** The XML codec to save/load mappings. */
	protected MappingXMLCodec											mapCodec;

	/** The XML codec to save queries. */
	protected XMLRenderer												xmlRenderer;

	/** The XML codec to load queries. */
	protected XMLReader													xmlReader;

	// protected PrefixManager prefixManager = null;

	protected APIController												apic							= null;

	protected Element													root;

	private final Logger												log								= LoggerFactory.getLogger(this
																												.getClass());

	public DataManager(APIController apic) {
		this.apic = apic;

		dsCodec = new DatasourceXMLCodec();
		mapCodec = new MappingXMLCodec(apic);
		xmlRenderer = new XMLRenderer();
		xmlReader = new XMLReader();
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
	public void saveOBDAData(URI obdaFileURI, boolean useTempFile, PrefixManager prefixManager) throws IOException
			 {
		File tempFile = null;
		File obdaFile = new File(obdaFileURI);

		if (useTempFile) {
			tempFile = File.createTempFile(System.currentTimeMillis()+"OBDA", null);
			if (tempFile.exists()) {
				boolean result = tempFile.delete();
				if (!result) {
					throw new IOException("Error deleting temporary file: " + tempFile.toString());
				}
			}
		}

		if (useTempFile) {
			saveOBDAData(tempFile.toURI(), prefixManager);
		} else {
			saveOBDAData(obdaFileURI, prefixManager);
			return;
		}

		if (useTempFile) {
			if (obdaFile.exists()) {
				boolean result = obdaFile.delete();
				if (!result) {
					throw new IOException("Error deleting default file: " + obdaFileURI.toString());
				}
			}
			tempFile.renameTo(obdaFile);
//			tempFile.
//			try {
//				FileUtils.copy(tempFile.getAbsolutePath(), new File(obdaFileURI).getAbsolutePath());
//			} catch (IOException e) {
//				log.error("Error while copying the temporary file: {}", e.getMessage());
//			}
//			tempFile.delete();
		}
	}

	public void saveOBDAData(URI fileuri, PrefixManager prefixManager) throws FileNotFoundException,
			IOException {
		File file = new File(fileuri);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			log.error(e.getMessage());
			log.debug(e.getMessage(), e);
			throw new IOException(e);
		}
		Document doc = db.newDocument();

		// Create the document root
		root = doc.createElement("OBDA");

		/***
		 * Creating namespaces (prefixes)
		 */
		root.setAttribute("version", "1.0");
		if (prefixManager.getDefaultNamespace() != null) {
			root.setAttribute("xmlns", prefixManager.getDefaultNamespace());
			root.setAttribute("xml:base", prefixManager.getDefaultNamespace());
		}

		Map<String, String> prefixes = prefixManager.getPrefixMap();
		Set<String> keys = prefixes.keySet();
		Iterator<String> sit = keys.iterator();
		while (sit.hasNext()) {
			String key = sit.next();
			root.setAttribute("xmlns:" + key, prefixManager.getURIForPrefix(key));

		}
		doc.appendChild(root);

		// Create the Mapping element
		Hashtable<URI, ArrayList<OBDAMappingAxiom>> mappings = apic.getMappingController().getMappings();
		dumpMappingsToXML(mappings);

		// Create the Data Source element
		List<DataSource> datasources = apic.getDatasourcesController().getAllSources();
		dumpDatasourcesToXML(datasources);

		// Create the Query element
		Vector<QueryControllerEntity> queries = apic.getQueryController().getElements();
		dumpQueriesToXML(queries);

		XMLUtils.saveDocumentToXMLFile(doc, prefixes, file.toString());
	}

	/***************************************************************************
	 * loads ALL OBDA data from a file
	 */
	public void loadOBDADataFromURI(URI obdaFileURI, URI currentOntologyURI, PrefixManager prefixManager) throws IOException {
		File obdaFile = new File(obdaFileURI);
		String version;

		if (!obdaFile.exists()) {
			log.error("OBDA file not found: {}", obdaFile.toString());
			throw new IOException("File not found: " + obdaFile.toString());
		}
		if (!obdaFile.canRead()) {
			log.error("Cant read file: {}", obdaFile.toString());
			throw new IOException("File not found: " + obdaFile.toString());
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
			log.error("OBDA info file should start with <OBDA> tag");
			return;
		}

		NamedNodeMap att = root.getAttributes();
		if (att.getLength() > 1) {
			for (int i = 0; i < att.getLength(); i++) {
				Node n = att.item(i);
				String name = n.getNodeName();
				String value = n.getNodeValue();
				if (name.equals("xml:base")) {
					// prefixManager.addUri(value, name);
					prefixManager.setDefaultNamespace(value);
				} else if (name.equals("version")) {
					version = value;
				} else if (name.equals("xmlns")) {
					prefixManager.setDefaultNamespace(value);
				} else if (name.substring(0, 5).equals("xmlns:")) {
					String[] aux = name.split(":");
					prefixManager.addUri(value, aux[1]);
				}
			}
		} else {
			String ontoUrl = currentOntologyURI.toString();
			int i = ontoUrl.lastIndexOf("/");
			String ontoName = ontoUrl.substring(i + 1, ontoUrl.length() - 4); // -4
			// because
			// we
			// want
			// to
			// remove
			// the
			// .owl
			// suffix
			prefixManager.addUri(ontoUrl, "xml:base");
			prefixManager.addUri(ontoUrl, "xmlns");
			prefixManager.addUri(ontoUrl.concat("#"), ontoName);
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
				if (node.getNodeName().equals("mappings")) { // Found mapping
					// block

					URI source = URI.create(node.getAttribute("sourceuri"));
					importMappingsFromXML(source, node);

				}
				if ((major < 0) && (node.getNodeName().equals("datasource"))) {
					// Found old data-source block
					System.err.println("WARNING: Loading a datasource using the old "
							+ "deprecated method. Update your .obda file by saving " + "it again.");
					DataSource source = dsCodec.decode(node);
					apic.getDatasourcesController().addDataSource(source);
				}
				String newDatasourceTag = dsCodec.getElementTag();
				if ((major > 0) && (node.getNodeName().equals(newDatasourceTag))) {
					// Found new data-source block
					DataSource source = dsCodec.decode(node);
					URI uri = URI.create(prefixManager.getDefaultNamespace());
					if (uri != null) {
						source.setParameter(RDBMSsourceParameterConstants.ONTOLOGY_URI, uri.toString());
					}
					apic.getDatasourcesController().addDataSource(source);
				}
				if (node.getNodeName().equals("SavedQueries")) {
					// Found queries block
					importQueriesFromXML(node);
				}


			}
		}
	}

	/**
	 * Save the mapping data as XML elements. The structure of the XML elements
	 * from the mapping data follows this construction:
	 * 
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
	 * @param mappings
	 *            the hash table of the mapping data
	 */
	protected void dumpMappingsToXML(Hashtable<URI, ArrayList<OBDAMappingAxiom>> mappings) {
		Document doc = root.getOwnerDocument();
		Enumeration<URI> datasourceUris = mappings.keys();
		URI datasourceUri = null;
		while (datasourceUris.hasMoreElements()) {
			datasourceUri = datasourceUris.nextElement();

			Element mappingGroup = doc.createElement("mappings");
			mappingGroup.setAttribute("sourceuri", datasourceUri.toString());
			mappingGroup.setAttribute("headclass", CQIEImpl.class.toString());
			mappingGroup.setAttribute("body", RDBMSSQLQuery.class.toString());
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
	 * 
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
	 * @param datasources
	 *            the hash map of the data-source data.
	 */
	protected void dumpDatasourcesToXML(List<DataSource> datasources) {
		Document doc = root.getOwnerDocument();
		Iterator<DataSource> sources = datasources.iterator();
		URI datasourceUri = null;
		while (sources.hasNext()) {
			DataSource datasource = sources.next();

			Element datasourceElement = dsCodec.encode(datasource);
			doc.adoptNode(datasourceElement);
			root.appendChild(datasourceElement);
		}
	}

	/**
	 * Save the query data as XML elements. The structure of the XML elements
	 * from the query data follows this construction:
	 * 
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
	 * @param queries
	 *            the vector of the query entities.
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
	 * @param datasource
	 *            the data source URI in which the mappings are linked.
	 * @param mappingRoot
	 *            the mapping root in the XML file.
	 * @throws QueryParseException
	 * @see ConjunctuveQuery
	 * @see RDBMSSQLQuery
	 * @see RDBMSOBDAMappingAxiom
	 */
	protected void importMappingsFromXML(URI datasource, Element mappingRoot) {
		NodeList childs = mappingRoot.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			try {
				Node child = childs.item(i);
				if (!(child instanceof Element)) {
					continue;
				}
				Element mapping = (Element) child;
				RDBMSOBDAMappingAxiom mappingAxiom = (RDBMSOBDAMappingAxiom) mapCodec.decode(mapping);
				if (mappingAxiom == null) {
					throw new Exception("Error while parsing the conjunctive query of " + "the mapping " + mapping.getAttribute("id"));
				}
				try {
					apic.getMappingController().insertMapping(datasource, mappingAxiom);
				} catch (DuplicateMappingException e) {
					log.warn("duplicate mapping detected while trying to load mappings " + "from file. Ignoring it. Datasource URI: "
							+ datasource + " " + "Mapping ID: " + mappingAxiom.getId());
				}
			} catch (Exception e) {
				try {
					// log.warn("Error loading mapping with id: {}",
					// ((Element) childs.item(i)).getAttribute("id"));
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
	 * categorized into one group. The method saves all the queries according to
	 * this hierarchical structure.
	 * 
	 * @param queryRoot
	 *            the query root in the XML file.
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
						apic.getQueryController().addQuery(query.getQuery(), query.getID(), group.getID());
					}
				}
			}
		}
	}
}
