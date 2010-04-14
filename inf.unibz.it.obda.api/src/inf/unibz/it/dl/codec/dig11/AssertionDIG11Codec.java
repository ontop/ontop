package inf.unibz.it.dl.codec.dig11;

import inf.unibz.it.dl.assertion.Assertion;
import inf.unibz.it.utils.codec.IOCodec;
import inf.unibz.it.utils.codec.XMLEncodable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*******************************************************************************
 * A class of Codecs which can translate from assertions into a DIG XML Element
 * representation. This are to be used to translate assertions into DIG messages.
 * Users of these codec should use the Document.adopt or Document.import methods
 * to get these elements into their DOM documents.
 * 
 * @author Mariano Rodriguez Muro
 * 
 * @param <AssertionClass>
 */
public abstract class AssertionDIG11Codec<AssertionClass extends Assertion> implements IOCodec<Element, AssertionClass>, XMLEncodable {

	DocumentBuilder	db	= null;
	Document		doc	= null;

	public AssertionDIG11Codec() {
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		doc = db.newDocument();
	}

	public abstract Element encode(AssertionClass input);

	public abstract AssertionClass decode(Element input);

	protected Element createElement(String tagName) {
		return doc.createElement(tagName);
	}

}
