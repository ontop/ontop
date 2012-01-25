package it.unibz.krdb.obda.reformulation.tests;

import java.io.FileOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class XMLResultWriter {

	private Element root = null;
	private Document doc = null;
	
	public XMLResultWriter (String ontouri) throws ParserConfigurationException{
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		doc = db.newDocument();
		root = doc.createElement("results");
	}
	
	public void addResult(String queryid,String[] h, String[] r){
		
		Element result = doc.createElement("result");
		result.setAttribute("queryid", queryid);
		
		Element head = doc.createElement("head");
		for(int i=0;i<h.length;i++){
			Element var = doc.createElement("variable");
			var.setTextContent(h[i]);
			head.appendChild(var);
		}
		
		Element tuples = doc.createElement("tuples");
		for(int i=0;i<r.length;i++){
			Element tuple = doc.createElement("tuple");
			Element cons = doc.createElement("constant");
			cons.setTextContent(r[i]);
			tuple.appendChild(cons);
			tuples.appendChild(tuple);
		}
		
		result.appendChild(head);
		result.appendChild(tuples);
		root.appendChild(result);
	}
	
	public void saveXMLResults(String fileuri) throws Exception{
		
		FileOutputStream fos = new FileOutputStream(fileuri);
		OutputFormat of = new OutputFormat("XML","ISO-8859-1",true);
		of.setIndent(2);
		of.setIndenting(true);
		XMLSerializer serializer = new XMLSerializer(fos,of);
		serializer.asDOMSerializer();
		serializer.serialize( root );
		fos.close();
	}
}
