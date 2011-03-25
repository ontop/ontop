package inf.unibz.it.obda.owlapi;

import inf.unibz.it.dl.assertion.Assertion;
import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.api.controller.QueryControllerEntity;
import inf.unibz.it.obda.api.io.DataManager;
import inf.unibz.it.obda.api.io.PrefixManager;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.utils.io.FileUtils;
import inf.unibz.it.utils.xml.XMLUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
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

/**
 * The obda plugin data manager is an extension of the original data manager in the
 * obda api. The only difference between this data manager an the original one is
 * the loading of prefixes. It looks whether an obda file has specified some some
 * prefixes and if so they are loaded and administrated by the prefix manager.
 *
 * @author Manfred Gerstgrasser
 */

public class OWLAPIDataManager extends DataManager {

	/**
	 * the current api controller
	 */
	private APIController apic = null;

	/**
	 * The constructor. Creates a new instance of the OBDAPluginDataManager
	 * @param apic the current api controller
	 */
	public OWLAPIDataManager(APIController apic, PrefixManager man) {
		super(apic, man);
		this.apic = apic;
	}

	/** The logger */
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Load the given obda file. In contrast to the original it looks whether the obda
	 * file defines some prefixes. If so they are loaded into the prefix mapper.
	 */
	@Override
	public void loadOBDADataFromURI(URI obdaFileURI) {

		File obdaFile = new File(obdaFileURI);
		if (obdaFile == null) {
			log.error("The OBDA file is not found!");
			return;
		}
		if (!obdaFile.exists()) {
			return;
		}
		if (!obdaFile.canRead()) {
			log.error("Cannot read the OBDA file:" + obdaFile.toString());
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
			log.error("The OBDA info file should start with <OBDA> tag!");
			return;
		}
		NamedNodeMap att = root.getAttributes();
		for(int i=0;i<att.getLength();i++){
			Node n = att.item(i);
			String name = n.getNodeName();
			String value = n.getNodeValue();
			if(name.equals("xml:base")){
				prefixManager.addUri(URI.create(value), name);
			}else if (name.equals("version")){
				prefixManager.addUri(URI.create(value), name);
			}else if (name.equals("xmlns")){
				prefixManager.addUri(URI.create(value), name);
			}else if (value.endsWith(".owl#")){
				String[] aux = name.split(":");
				prefixManager.addUri(URI.create(value), aux[1]);
			}else if(name.startsWith("xmlns:")){
				String[] aux = name.split(":");
				prefixManager.addUri(URI.create(value), aux[1]);
			}
		}

		super.loadOBDADataFromURI(obdaFileURI);
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
	@Override
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
				log.error("Error occurred while copying the temporary file.");
			}
			tempFile.delete();
		}
	}

	/**
	 * Saves all obda data including the prefixes stored by the prefix mapper
	 */
	@Override
	public void saveOBDAData(URI fileuri) throws ParserConfigurationException, FileNotFoundException, IOException {
		File file = new File(fileuri.toString());
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.newDocument();
		root = doc.createElement("OBDA");
		HashMap<String, URI> prefixes = prefixManager.getPrefixMap();
		Set<String> set = prefixes.keySet();
		Iterator<String> sit = set.iterator();
		while(sit.hasNext()){
			String key = sit.next();
			root.setAttribute(key, prefixManager.getPrefixMap().get(key).toString());
		}
		doc.appendChild(root);

    // Create the Mapping element
		Hashtable<URI, ArrayList<OBDAMappingAxiom>> mappings =
        apic.getMappingController().getMappings();
    	dumpMappingsToXML(mappings);
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
		XMLUtils.saveDocumentToXMLFile(doc, prefixes, file);
	}

	/**
	 * Returns the Map containing for each prefix the corresponding onotlogy URI
	 * @return the prefix map
	 */
	public Map<String, URI> getPrefixMap(){
		return prefixManager.getPrefixMap();
	}
}
