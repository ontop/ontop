package it.unibz.krdb.obda.codec;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*******************************************************************************
 * A class of Codecs which can translate from arbitrary objects into a DOM XML
 * Element representation. These are intended for input and output of XML OBDA
 * files. Elements return by this codec are created using the internal Document
 * object, through the createElement call. These should be adopted by target
 * documents.
 * 
 * @author Mariano Rodriguez Muro
 * 
 * @param <AssertionClass>
 */
public abstract class ObjectXMLCodec<ObjectClass extends Object> implements IOCodec<Element, ObjectClass>, XMLEncodable {

	static DocumentBuilder db = null;
	static Document doc = null;

	public ObjectXMLCodec() {
		try {
			if (db == null) {
				db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				doc = db.newDocument();
			}
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}

	}

	public abstract Element encode(ObjectClass input) throws Exception;

	public abstract ObjectClass decode(Element input) throws Exception;

	protected Element createElement(String tagName) throws DOMException {
		return doc.createElement(tagName);
	}

}
