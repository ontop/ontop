package it.unibz.krdb.obda.codec;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDALibConstants;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.parser.DatalogProgramParser;
import it.unibz.krdb.obda.parser.DatalogQueryHelper;

import java.util.ArrayList;
import java.util.Collection;

import org.antlr.runtime.RecognitionException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * The DatalogConjunctiveQueryCodec can be used to encode a conjunctive query
 * into XML or to decode a conjunctive query from XML
 */
public class DatalogConjunctiveQueryXMLCodec extends ObjectXMLCodec<CQIE> {

	/**
	 * The tag used to represent a conjunctive query in XML
	 */
	private static final String TAG = "CQ";

	private OBDAModel obdaModel = null;

	DatalogProgramParser datalogParser = new DatalogProgramParser();

	private DatalogQueryHelper queryHelper;

	/**
	 * Default constructor.
	 * 
	 * @param obdaModel
	 *            The OBDA model.
	 */
	public DatalogConjunctiveQueryXMLCodec(OBDAModel obdaModel) {
		this.obdaModel = obdaModel;
		queryHelper = new DatalogQueryHelper(obdaModel.getPrefixManager());

	}

	/**
	 * Decodes the given XML element into an conjunctive query.
	 * 
	 * @throws RecognitionException
	 */
	@Override
	public CQIE decode(Element input) throws RecognitionException {
		String query = input.getAttribute("string");
		return decode(query);
	}

	/**
	 * Encodes the given conjunctive query int XML.
	 */
	@Override
	public Element encode(CQIE hq) throws DOMException {
		Element mappingheadelement = createElement(TAG);
		TargetQeryToTextCodec codec = new TargetQeryToTextCodec(obdaModel);
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
		return TAG;
	}

	/**
	 * Decodes the given String into an conjunctive query.
	 * 
	 * @throws RecognitionException
	 */
	public CQIE decode(String input) throws RecognitionException {
		return parse(input);
	}

	private CQIE parse(String query) throws RecognitionException {
		datalogParser.parse(prepareQuery(query));
		return datalogParser.getRule(0);
	}

	private String prepareQuery(String input) {
		
		StringBuffer bf = new StringBuffer();
		bf.append(input);
//		String query = input;

		String[] atoms = input.split(OBDALibConstants.DATALOG_IMPLY_SYMBOL, 2);
		if (atoms.length == 1) // if no head
		{
			bf.insert(0, OBDALibConstants.DATALOG_IMPLY_SYMBOL );
			bf.insert(0, " ");
			bf.insert(0, queryHelper.getDefaultHead());
//			query = queryHelper.getDefaultHead() + " " + DatalogQueryHelper.DATALOG_IMPLY_SYMBOL + " " + query;
		}
		
		bf.insert(0, queryHelper.getPrefixes());

		// Append the prefixes
//		query = queryHelper.getPrefixes() + query;

		return bf.toString();
//		return query;
	}
}
