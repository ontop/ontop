package inf.unibz.it.obda.rdbmsgav.codec.dig11;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.dig11.DIG12Coupler;

import java.util.List;

import org.obda.query.domain.Atom;
import org.obda.query.domain.Query;
import org.obda.query.domain.Term;
import org.obda.query.domain.imp.CQIEImpl;
import org.obda.query.domain.imp.ObjectVariableImpl;
import org.obda.query.domain.imp.ValueConstantImpl;
import org.obda.query.domain.imp.VariableImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

//TODO: Refactor to make it a real instance of codec, possibly
//split into different codecs for the elements of the query

public class OBDADIGCodec {

	private static final String	TELLS_MAPPING_SQLQUERY_QUERYATR	= "query";
	private static final String	TELLS_MAPPING_SQLQUERY			= "SQLQuery";
	private static final String	TELLS_MAPPING_DATA_SOURCE_URI	= "dataSourceURI";
	private static final String	TELLS_MAPPING_MAPPING_AXIOM		= "MappingAxiom";
	private static final String	TELLS_MAPPING_ID				= "id";
	public static final String	TELLS_MAPPING_ENABLED			= "enabled";

	private static APIController apic = null;

	private static final Logger log =
		LoggerFactory.getLogger("inf.unibz.it.obda.rdbmsgav.codec.dig11.OBDADIGCodec");

	public static Element getDIG(OBDAMappingAxiom mapping, String datasource_uri, Document parentdoc, APIController apic) {
		Element dig_mapping = null;

		Query source_query = mapping.getSourceQuery();
		Element dig_mappingbody = null;
		Query target_query = mapping.getTargetQuery();
		Element dig_mappinghead = null;

		dig_mappinghead = getDIG(target_query, parentdoc, apic);
		dig_mappingbody = getDIG(source_query, parentdoc, apic);

		Element new_dig_mapping = parentdoc.createElement(TELLS_MAPPING_MAPPING_AXIOM);
		new_dig_mapping.setAttribute(TELLS_MAPPING_DATA_SOURCE_URI, datasource_uri);
		new_dig_mapping.setAttribute(TELLS_MAPPING_ID, mapping.getId());
		// new_dig_mapping.setAttribute(TELLS_MAPPING_ENABLED, "true");
		new_dig_mapping.appendChild(dig_mappinghead);
		new_dig_mapping.appendChild(dig_mappingbody);
		dig_mapping = new_dig_mapping;

		return dig_mapping;
	}

	public static Element getDIG(Query query, Document parentdoc, APIController apic) {
		// TODO Move the SPARQL query translation to this method instead of its
		// own builder
		Element dig_query = null;

		if (query instanceof RDBMSSQLQuery) {
			// <SQLQuery query="SELECT name, lastname FROM R WHERE gpa >=
			// 7.5" arity="2" />
			Element new_query = parentdoc.createElement(TELLS_MAPPING_SQLQUERY);
			new_query.setAttribute(TELLS_MAPPING_SQLQUERY_QUERYATR, query.toString());
			dig_query = new_query;

		} else if (query instanceof CQIEImpl) {
			Element new_dig_query = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_RETRIEVE);

			Element head = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_HEAD);
			Element body = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_BODY);
			Element intersection = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_QAND);

			CQIEImpl cq = (CQIEImpl) query;
			List<Atom> atoms = cq.getBody();
			Atom headAtom = cq.getHead();
			List<Term> head_variables = headAtom.getTerms();

			for (int i = 0; i < atoms.size(); i++) {
				Atom atom = atoms.get(i);
				Element dig_atom = getDIG(atom, parentdoc, apic);
				if (dig_atom != null) {
					intersection.appendChild(dig_atom);
				}
				else {
					log.info("Atom null while trying to render it");
				}
			}
			// building the head with all variables selected

			for (Term variableTerm : head_variables) {
				Element head_term = getDIG(variableTerm, parentdoc);
				head.appendChild(head_term);
			}

			body.appendChild(intersection);
			new_dig_query.appendChild(head);
			new_dig_query.appendChild(body);
			dig_query = new_dig_query;
		}
		// else {
		// throw new Exception("Unsupported query type: " +
		// query.getClass().toString());
		// }

		return dig_query;
	}

	public static Element getDIG(Atom atom, Document parentdoc, APIController apic) {
		Element dig_atom = null;
		int arity = atom.getArity();
		if (arity == 1) {  // concept query atom
			Element concept_query_atom = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_CQATOM);
			String name = apic.getEntityNameRenderer().getPredicateName(atom);
			List<Term> terms = atom.getTerms();
			for (int i = 0; i < terms.size(); i++) {
				Term current_term = terms.get(i);
				Element dig_term = getDIG(current_term, parentdoc);
				concept_query_atom.appendChild(dig_term);
			}
			Element concept_element = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_CATOM);
			concept_element.setAttribute(DIG12Coupler.ASKS_QUERY_NAME, name);
			concept_query_atom.appendChild(concept_element);
			dig_atom = concept_query_atom;
		} else if (arity == 2) {  // binary query atom
			Element role_query_atom = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_RQATOM);
			String name = apic.getEntityNameRenderer().getPredicateName(atom);
			List<Term> terms = atom.getTerms();
			for (int i = 0; i < terms.size(); i++) {
				Term current_term = terms.get(i);
				Element dig_term = getDIG(current_term, parentdoc);
				if (atom != null) {
					role_query_atom.appendChild(dig_term);
				}
				else {
					log.info("Atom null while trying to render it");
				}
			}
			Element role_element = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_RATOM);
			role_element.setAttribute(DIG12Coupler.ASKS_QUERY_NAME, name);
			role_query_atom.appendChild(role_element);
			dig_atom = role_query_atom;
		}

		return dig_atom;
	}

	public static Element getDIG(Term term, Document parentdoc) {
		Element dig_term = null;
		if (term instanceof VariableImpl) {

			Element new_term = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_INDVAR);
			new_term.setAttribute(DIG12Coupler.ASKS_QUERY_NAME, term.getName());
			dig_term = new_term;
		} else if (term instanceof ValueConstantImpl) {
			// throw new Exception("Contants not supported");
			// Element new_term =
			// parentdoc.createElement(DIG12Coupler.ASKS_QUERY_INDIVIDUAL);
			// new_term.setAttribute(DIG12Coupler.ASKS_QUERY_NAME, term.getName());
			// dig_term = new_term;

		} else if (term instanceof ObjectVariableImpl) {

			Element new_term = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_FUNCTION);
			new_term.setAttribute(DIG12Coupler.ASKS_QUERY_NAME, apic.getEntityNameRenderer().getFunctionName((ObjectVariableImpl) term));
			ObjectVariableImpl function_term = (ObjectVariableImpl) term;
			List<Term> parameters = function_term.getTerms();
			for (int i = 0; i < parameters.size(); i++) {
				Element parameter = getDIG(parameters.get(i), parentdoc);
				new_term.appendChild(parameter);
			}
			dig_term = new_term;

		}

		return dig_term;

	}

}
