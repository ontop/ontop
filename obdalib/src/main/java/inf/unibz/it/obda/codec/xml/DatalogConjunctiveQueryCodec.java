package inf.unibz.it.obda.codec.xml;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.w3c.dom.Element;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.FunctionTerm;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.parser.exception.QueryParseException;
import inf.unibz.it.utils.codec.ObjectXMLCodec;
import inf.unibz.it.utils.codec.TargetQeryToTextCodec;

/**
 * The DatalogConjunctiveQueryCodec can be used to encode a conjunctive query
 * into XML or to decode a conjunctive query from XML 
 * 
 * @author Manfred Gerstgrasser
 *
 */

public class DatalogConjunctiveQueryCodec extends ObjectXMLCodec<ConjunctiveQuery> {

	/**
	 * The tag used to represent a conjunctive query in XML
	 */
	private static final String	TAG	= "CQ";
	/**
	 * the current api controller
	 */
	APIController apic = null;
	
	public DatalogConjunctiveQueryCodec(APIController apic){
		this.apic = apic;
	}
	
	/**
	 * Decodes the given XML element into an conjunctive query.
	 */
	@Override
	public ConjunctiveQuery decode(Element input) {
		
		String CQstring = input.getAttribute("string");
		ConjunctiveQuery cq=null;
		try {
			cq = new ConjunctiveQuery(CQstring, apic);
		} catch (QueryParseException e1) {
//			throw e1;
			return null;
		}
		return cq;
	}

	/**
	 * Encodes the given conjunctive query int XML.
	 */
	@Override
	public Element encode(ConjunctiveQuery hq) {
		
		Element mappingheadelement = createElement(TAG);
		TargetQeryToTextCodec codec = new TargetQeryToTextCodec(apic);
		mappingheadelement.setAttribute("string", codec.encode(hq));
		return mappingheadelement;
	}

	/**
	 * Returns all attributes used in conjunctive query element.
	 */
	
	@Override
	public Collection<String> getAttributes() {
		ArrayList<String> fixedAttributes = new ArrayList<String>();
		fixedAttributes.add("string");
		return fixedAttributes;
	}

	/**
	 * Returns the tag name for conjunctive queries
	 */
	
	@Override
	public String getElementTag() {
		// TODO Auto-generated method stub
		return TAG;
	}
	
	/**
	 * Decodes the given String into an conjunctive query.
	 */
	public ConjunctiveQuery decode(String input) {
		
		ConjunctiveQuery cq=null;
		try {
			cq = new ConjunctiveQuery(input, apic);
		} catch (QueryParseException e1) {
//			throw e1;
			return null;
		}
		return cq;
	}

}
