package inf.unibz.it.obda.io;

import inf.unibz.it.obda.codec.QueryGroupXMLReader;
import inf.unibz.it.obda.codec.QueryGroupXMLRenderer;
import inf.unibz.it.obda.queryanswering.QueryController;
import inf.unibz.it.obda.queryanswering.QueryControllerEntity;
import inf.unibz.it.obda.queryanswering.QueryControllerGroup;
import inf.unibz.it.obda.queryanswering.QueryControllerQuery;
import inf.unibz.it.obda.utils.XMLUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class QueryStorageManager {

	protected QueryController queryCon = null;
	protected Element root;
	QueryGroupXMLRenderer xmlRenderer;
	QueryGroupXMLReader xmlReader;
	protected Document doc = null;

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	public QueryStorageManager(QueryController queryCon){
		this.queryCon = queryCon;
		xmlRenderer = new QueryGroupXMLRenderer();
		xmlReader = new QueryGroupXMLReader();
	}
	
	public void loadQueries(URI fileUri){
		File obdaFile = new File(fileUri);

		if (!obdaFile.exists()) {
			return;
		}
		if (!obdaFile.canRead()) {
			log.error("WARNING: can't read the OBDA file:" +
			    obdaFile.toString());
		}
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(obdaFile);
			doc.getDocumentElement().normalize();
		} catch (Exception e) {
			log.error(e.getMessage());
			return;
		}
		
		Element root = doc.getDocumentElement(); // OBDA
		if (root.getNodeName() != "OBDA") {
			log.error("OBDA info file should start with <OBDA> tag");
			return;
		}
		
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element node = (Element) children.item(i);
				if (node.getNodeName().equals("SavedQueries")) { 
					importQueriesFromXML(node);
				}
			}
		}
	}
	
	public void saveQueries(URI fileUri){
		
		try {
			File file = new File(fileUri);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.newDocument();
			root = doc.createElement("OBDA");
			doc.appendChild(root);
			
			Vector<QueryControllerEntity> queries =queryCon.getElements();
			dumpQueriesToXML(queries);
			
			XMLUtils.saveDocumentToXMLFile(doc, file);
			
		} catch (DOMException e) {
			log.error(e.getMessage());
		} catch (ParserConfigurationException e) {
			log.error(e.getMessage());
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	protected void importQueriesFromXML(Element queryRoot) {
		NodeList childs = queryRoot.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node node = childs.item(i);
			if (node instanceof Element) {
				Element element = (Element) node;
				if (element.getNodeName().equals("Query")) {
					QueryControllerQuery query = xmlReader.readQuery(element);
					queryCon.addQuery(query.getQuery(), query.getID());
				} else if ((element.getNodeName().equals("QueryGroup"))) {
					QueryControllerGroup group = xmlReader.readQueryGroup(element);
					queryCon.createGroup(group.getID());
					Vector<QueryControllerQuery> queries = group.getQueries();
					for (QueryControllerQuery query : queries) {
						queryCon.addQuery(query.getQuery(), query.getID(),
								group.getID());
					}
				}
			}
		}
	}
	
	protected void dumpQueriesToXML(Vector<QueryControllerEntity> queries) {
	    Element savedQueryElement = doc.createElement("SavedQueries");
	    for (QueryControllerEntity query : queries) {
	      Element queryElement = xmlRenderer.render(savedQueryElement, query);
	      savedQueryElement.appendChild(queryElement);
	    }
	    root.appendChild(savedQueryElement);
	  }
	
}
