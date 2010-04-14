package inf.unibz.it.utils.codec;

import inf.unibz.it.dl.assertion.Assertion;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*******************************************************************************
 * A class of Codecs which can translate from arbitrary objects into a DOM XML Element
 * representation. These are intended for input and output of XML OBDA files.
 * Elements return by this codec are created using the internal Document object,
 * through the createElement call. These should be adopted by target documents.
 * 
 * @author Mariano Rodriguez Muro
 * 
 * @param <AssertionClass>
 */
public abstract class ObjectXMLCodec<ObjectClass extends Object> implements IOCodec<Element, ObjectClass>, XMLEncodable {

	DocumentBuilder	db	= null;
	Document		doc	= null;

	public ObjectXMLCodec() {
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		doc = db.newDocument();
	}

	public abstract Element encode(ObjectClass input);

	public abstract ObjectClass decode(Element input);

	protected Element createElement(String tagName) {
		return doc.createElement(tagName);
	}

}
