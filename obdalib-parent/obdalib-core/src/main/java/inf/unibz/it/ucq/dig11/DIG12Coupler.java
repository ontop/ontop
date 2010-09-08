package inf.unibz.it.ucq.dig11;


import java.util.Vector;

//import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DIG12Coupler {

	public static final String	RESPONSES_BINDINGS_TUPLE		= "tuple";
	
	public static final String	ASKS_QUERY_FUNCTION				= "indfunc";
	public static final String	ASKS_QUERY_RETRIEVE				= "retrieve";
	public static final String	ASKS_QUERY_ONTOLOGY_URI			= "ontologyURI";
	public static final String	ASKS_QUERY_ID					= "id";
	public static final String	ASKS_QUERY_HEAD					= "head";
	public static final String	ASKS_QUERY_BODY					= "body";
	public static final String	ASKS_QUERY_VARIABLE				= "QueryVariable";
	public static final String	ASKS_QUERY_URI					= "URI";
	public static final String	ASKS_QUERY_QAND					= "qand";
	public static final String	ASKS_QUERY_RATOM				= "ratom";
	public static final String	ASKS_QUERY_RQATOM				= "rqatom";
	public static final String	ASKS_QUERY_INDIVIDUAL			= "individual";
	public static final String	ASKS_QUERY_INDVAR				= "indvar";
	public static final String	ASKS_QUERY_NAME					= "name";
	public static final String	ASKS_QUERY_CATOM				= "catom";
	public static final String	ASKS_QUERY_CQATOM				= "cqatom";
	public static final String	ASKS_QUERY_RDF_TYPE				= "rdf:type";

//	static Logger				log								= Logger.getLogger(DIG12Coupler.class);

	public static Document getQueryDocument(Element query, String ontology_uri) {
		Document owner = query.getOwnerDocument();
		Element asks = owner.createElement("asks");
		asks.setAttribute("uri", ontology_uri);

		asks.appendChild(query);

		owner.appendChild(asks);
		return owner;
	}

	/**
	 * @param response_buffer
	 */
	public static void removeFormattingCharacers(StringBuilder response_buffer) {
		while (true) {
			int remove_position = response_buffer.indexOf("\t");
			if (remove_position != -1) {
				response_buffer.deleteCharAt(remove_position);
			} else {
				break;
			}
		}
		while (true) {
			int remove_position = response_buffer.indexOf("\n");
			if (remove_position != -1) {
				response_buffer.deleteCharAt(remove_position);
			} else {
				break;
			}
		}
		while (true) {
			int remove_position = response_buffer.indexOf("\r");
			if (remove_position != -1) {
				response_buffer.deleteCharAt(remove_position);
			} else {
				break;
			}
		}
		response_buffer.trimToSize();
	}

	/***************************************************************************
	 * Parsers the response to a <retrieve returnSQLExpansion="true">
	 * 
	 * query, which hsould be a
	 * 
	 * <reponse> <SQLExpansion string="select x from etc"/> </response>
	 * 
	 * @param response
	 * @return
	 */
	public static String getSQLExpansionFromQuery(Document response) {

		Element head = (Element) response.getElementsByTagName("SQLExpansion").item(0);
		return head.getAttribute("string");

	}

	public static String getExpandedUCQ(Document response) {

		Element head = (Element) response.getElementsByTagName("ExpandedUCQ").item(0);
		return head.getAttribute("string");

	}

	public static boolean getReasoningStatus(Document response) {
		Element head = (Element) response.getElementsByTagName("ReasoningEnabled").item(0);
		return Boolean.valueOf(head.getAttribute("value"));
	}

	/***************************************************************************
	 * Parsers the response to a <retrieve returnSQLExpansion="true">
	 * 
	 * query, which hsould be a
	 * 
	 * <reponse> <SQLExpansion string="select x from etc"/> </response>
	 * 
	 * @param response
	 * @return
	 */
	public static String getExpanedUCQFromQuery(Document response) {

		Element head = (Element) response.getElementsByTagName("ExpandedUCQ").item(0);
		return head.getAttribute("string");

	}

	/***************************************************************************
	 * Auxiliary method, receives a DOM document containing a bindings structure
	 * (response to a Retrieve query) and returns an array list in which the
	 * first element is the list of variables in the answer set and the rest are
	 * the variable bindings for each of the tuples in the answer.
	 * 
	 * @param response
	 * @return
	 */
	public static Vector<Vector<String>> getBindingsFromRetrieveResponse(Document response) {
		Vector<Vector<String>> current_response = new Vector<Vector<String>>();
		Element head = (Element) response.getElementsByTagName(ASKS_QUERY_HEAD).item(0);
		NodeList tuples = response.getElementsByTagName(RESPONSES_BINDINGS_TUPLE);

		Vector<String> head_elements = new Vector<String>();
		NodeList head_vars = head.getChildNodes();
		for (int i = 0; i < head_vars.getLength(); i++) {
			if (head_vars.item(i) instanceof Element) {
				Element current_var = (Element) head_vars.item(i);
				head_elements.add(current_var.getAttribute(ASKS_QUERY_NAME));
			}
		}
		current_response.add(head_elements);
		for (int j = 0; j < tuples.getLength(); j++) {
			Element current_tuple = (Element) tuples.item(j);
			Vector<String> individuals_bindings = new Vector<String>();
			NodeList individuals_elements = current_tuple.getChildNodes();
			for (int i = 0; i < individuals_elements.getLength(); i++) {
				if (individuals_elements.item(i) instanceof Element) {
					Element current_individual = (Element) individuals_elements.item(i);
					individuals_bindings.add(current_individual.getAttribute(ASKS_QUERY_NAME));
				}
			}
			current_response.add(individuals_bindings);

		}
		return current_response;
	}

}
