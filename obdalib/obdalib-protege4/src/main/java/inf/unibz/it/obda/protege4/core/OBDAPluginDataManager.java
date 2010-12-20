package inf.unibz.it.obda.protege4.core;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.io.DataManager;
import inf.unibz.it.obda.api.io.PrefixManager;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.protege.editor.owl.ui.prefix.PrefixMapper;
import org.protege.editor.owl.ui.prefix.PrefixMapperManager;
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
 *
 */

public class OBDAPluginDataManager extends DataManager {

	/**
	 * the current api controller
	 */
	private APIController apic = null;

	/** The logger */
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * The constructor. Creates a new instance of the OBDAPluginDataManager
	 * @param apic the current api controller
	 * @param pref the current prefix manager
	 */
	public OBDAPluginDataManager(APIController apic, PrefixManager pref) {
		super(apic,pref);
		this.apic = apic;
	}

	/**
	 * Load the given obda file. In contrast to the original it looks whether the obda
	 * file defines some prefixes. If so they are loaded into the prefix manager.
	 */
	@Override
	public void loadOBDADataFromURI(URI obdaFileURI) {

		File obdaFile = new File(obdaFileURI);
		if (obdaFile == null) {
			log.error("The OBDA file is not found.");
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
		PrefixMapperManager prefman = PrefixMapperManager.getInstance();
		PrefixMapper mapper = prefman.getMapper();
		NamedNodeMap att = root.getAttributes();
		if(att.getLength()>1){
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
				}else if(name.equals("xmlns:owl2xml")){
					String[] aux = name.split(":");
					prefixManager.addUri(URI.create(value), aux[1]);
				}else if(name.startsWith("xmlns:")){
					String[] aux = name.split(":");
					String s = mapper.getValue(aux[1]);
					if(s!=null){
						prefixManager.addUri(URI.create(value), aux[1]);
					}else{
						fillPrefixManagerWithDefaultValues();
						break;
					}
				}
			}
		}else{
			fillPrefixManagerWithDefaultValues();
		}
		super.loadOBDADataFromURI(obdaFileURI);
	}

	/**
	 * If the obda file doesnot contain any prefix definitions. Some prede
	 * prefixes are loaded into the manager.
	 */
	private void fillPrefixManagerWithDefaultValues(){

		String ontoUrl = apic.getCurrentOntologyURI().toString();
		int i = ontoUrl.lastIndexOf("/");
		String ontoName = ontoUrl.substring(i+1,ontoUrl.length()-4); //-4 because we want to remove the .owl suffix
		prefixManager.addUri(URI.create(CURRENT_OBDA_FILE_VERSION_MAJOR + "." + CURRENT_OBDA_FILE_VERSION_MINOR),"version");
		prefixManager.addUri(URI.create(ontoUrl),"xml:base");
		prefixManager.addUri(URI.create(ontoUrl),"xmlns");
		prefixManager.addUri(URI.create("http://www.w3.org/2006/12/owl2-xml#"),"owl2xml");
		prefixManager.addUri(URI.create(ontoUrl+"#"),ontoName);
		PrefixMapperManager prefman = PrefixMapperManager.getInstance();
		PrefixMapper mapper = prefman.getMapper();
		Set<String> set = mapper.getPrefixes();
		Iterator<String> sit = set.iterator();
		while(sit.hasNext()){
			String key = sit.next();
			if(!(key.equals("dc") || key.equals("dcterms")|| key.equals("dctype")||
					key.equals("swrl")|| key.equals("swrlb")|| key.equals("foaf"))){

				prefixManager.addUri(URI.create(mapper.getValue(key)),key);
			}
		}
	}

}
